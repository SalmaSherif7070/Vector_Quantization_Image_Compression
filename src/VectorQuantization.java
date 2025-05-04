import java.io.*;
import java.util.*;

public class VectorQuantization {
    public static void main(String[] args) {
        try {
            System.out.println("Starting VectorQuantization...");

            // Define paths
            String trainPath = "data/input/train/";
            String testPath = "data/input/test/";
            String codebookPath = "data/output/codebooks/";
            String compressedPath = "data/output/compressed/";
            String decompressedPath = "data/output/decompressed/";

            // Create output directories
            System.out.println("Creating output directories...");
            new File(codebookPath).mkdirs();
            new File(compressedPath).mkdirs();
            new File(decompressedPath).mkdirs();

            // Load training images
            System.out.println("Loading training images...");
            List<String> trainImages = new ArrayList<>();
            String[] domains = {"nature", "faces", "animals"};
            for (String domain : domains) {
                String domainCapitalized = domain.substring(0, 1).toUpperCase() + domain.substring(1);
                for (int i = 1; i <= 10; i++) {
                    String imagePath = trainPath + domain + "/" + domainCapitalized + " " + i + ".jpg";
                    trainImages.add(imagePath);
                    System.out.println("Added training image: " + imagePath);
                }
            }

            // Generate codebooks for R, G, B
            System.out.println("Generating codebooks...");
            ImageProcessor processor = new ImageProcessor();
            Codebook redCodebook = new Codebook(2, 256);
            Codebook greenCodebook = new Codebook(2, 256);
            Codebook blueCodebook = new Codebook(2, 256);

            List<double[]> redBlocks = new ArrayList<>();
            List<double[]> greenBlocks = new ArrayList<>();
            List<double[]> blueBlocks = new ArrayList<>();

            for (String imagePath : trainImages) {
                System.out.println("Processing training image: " + imagePath);
                try {
                    int[][][] rgb = processor.loadImage(imagePath);
                    redBlocks.addAll(processor.getBlocks(rgb[0]));
                    greenBlocks.addAll(processor.getBlocks(rgb[1]));
                    blueBlocks.addAll(processor.getBlocks(rgb[2]));
                } catch (IOException e) {
                    System.out.println("Error reading training image: " + imagePath);
                    throw e;
                }
            }

            System.out.println("Generating Red codebook...");
            redCodebook.generateCodebook(redBlocks);
            System.out.println("Generating Green codebook...");
            greenCodebook.generateCodebook(greenBlocks);
            System.out.println("Generating Blue codebook...");
            blueCodebook.generateCodebook(blueBlocks);

            // Save codebooks
            System.out.println("Saving codebooks...");
            redCodebook.saveCodebook(codebookPath + "red_codebook.txt");
            greenCodebook.saveCodebook(codebookPath + "green_codebook.txt");
            blueCodebook.saveCodebook(codebookPath + "blue_codebook.txt");

            // Process test images
            System.out.println("Processing test images...");
            VQCompressor compressor = new VQCompressor();
            QualityMetrics metrics = new QualityMetrics();
            Map<String, List<Double>> mseByDomain = new HashMap<>();
            Map<String, List<Double>> psnrByDomain = new HashMap<>();
            Map<String, List<Double>> crByDomain = new HashMap<>();
            for (String domain : domains) {
                mseByDomain.put(domain, new ArrayList<>());
                psnrByDomain.put(domain, new ArrayList<>());
                crByDomain.put(domain, new ArrayList<>());
            }

            for (String domain : domains) {
                String domainCapitalized = domain.substring(0, 1).toUpperCase() + domain.substring(1);
                for (int i = 1; i <= 5; i++) {
                    String imagePath = testPath + domain + "/" + domainCapitalized + " " + i + ".jpg";
                    String compressedFile = compressedPath + domain + i + ".txt";
                    String decompressedFile = decompressedPath + domain + i + "_decompressed.png";
                    System.out.println("Processing test image: " + imagePath);

                    // Load test image
                    try {
                        int[][][] rgb = processor.loadImage(imagePath);
                        int[][] red = rgb[0], green = rgb[1], blue = rgb[2];

                        // Compress
                        List<double[]> redTestBlocks = processor.getBlocks(red);
                        List<double[]> greenTestBlocks = processor.getBlocks(green);
                        List<double[]> blueTestBlocks = processor.getBlocks(blue);

                        int[][] redLabels = compressor.compressImage(redTestBlocks, redCodebook);
                        int[][] greenLabels = compressor.compressImage(greenTestBlocks, greenCodebook);
                        int[][] blueLabels = compressor.compressImage(blueTestBlocks, blueCodebook);

                        // Save compressed indices
                        compressor.saveCompressedIndices(redLabels, greenLabels, blueLabels, compressedFile);

                        // Decompress
                        int[][] reconRed = compressor.reconstructComponent(redLabels, redCodebook, 1000, 1000);
                        int[][] reconGreen = compressor.reconstructComponent(greenLabels, greenCodebook, 1000, 1000);
                        int[][] reconBlue = compressor.reconstructComponent(blueLabels, blueCodebook, 1000, 1000);

                        // Reconstruct image
                        processor.saveImage(reconRed, reconGreen, reconBlue, decompressedFile);

                        // Calculate metrics
                        double mse = metrics.calculateMSE(rgb, new int[][][]{reconRed, reconGreen, reconBlue});
                        double psnr = metrics.calculatePSNR(mse);
                        double cr = metrics.calculateCompressionRatio(1000, 1000, redLabels.length * redLabels[0].length, 256);

                        mseByDomain.get(domain).add(mse);
                        psnrByDomain.get(domain).add(psnr);
                        crByDomain.get(domain).add(cr);

                        System.out.printf("Test Image: %s %d.jpg, MSE: %.2f, PSNR: %.2f dB, Compression Ratio: %.2f\n",
                                domainCapitalized, i, mse, psnr, cr);
                    } catch (IOException e) {
                        System.out.println("Error processing test image: " + imagePath);
                        throw e;
                    }
                }
            }

            // Print average metrics per domain
            System.out.println("Printing average metrics...");
            for (String domain : domains) {
                double avgMSE = mseByDomain.get(domain).stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double avgPSNR = psnrByDomain.get(domain).stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double avgCR = crByDomain.get(domain).stream().mapToDouble(Double::doubleValue).average().orElse(0);
                System.out.printf("Domain: %s, Avg MSE: %.2f, Avg PSNR: %.2f dB, Avg Compression Ratio: %.2f\n",
                        domain, avgMSE, avgPSNR, avgCR);
            }

            System.out.println("Program completed successfully.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}