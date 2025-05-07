import java.io.*;
import java.util.*;

// Main class for vector quantization compression
public class VectorQuantization {
    public static void main(String[] args) {
        try {
            // Set up directories
            String trainPath = "data/input/train/";
            String testPath = "data/input/test/";
            String codebookPath = "data/output/codebooks/";
            String compressedPath = "data/output/compressed/";
            String decompressedPath = "data/output/decompressed/";

            new File(codebookPath).mkdirs();
            new File(compressedPath).mkdirs();
            new File(decompressedPath).mkdirs();

            // Initialize codebooks
            String[] codebookFiles = {codebookPath + "red_codebook.txt", codebookPath + "green_codebook.txt", codebookPath + "blue_codebook.txt"};
            Codebook redCodebook = new Codebook(2, 256, "Red", codebookFiles[0]);
            Codebook greenCodebook = new Codebook(2, 256, "Green", codebookFiles[1]);
            Codebook blueCodebook = new Codebook(2, 256, "Blue", codebookFiles[2]);

            // Generate codebooks if missing or invalid
            boolean allCodebooksExist = Arrays.stream(codebookFiles).allMatch(f -> new File(f).exists());
            if (!allCodebooksExist || !redCodebook.isValid() || !greenCodebook.isValid() || !blueCodebook.isValid()) {
                // Collect training image paths
                List<String> trainingImagePaths = new ArrayList<>();
                String[] domains = {"nature", "faces", "animals"};
                String[] fileNames = {"Nature", "Faces", "Animals"};
                for (int index = 0; index < domains.length; index++) {
                    String domain = domains[index];
                    String fileName = fileNames[index];
                    for (int i = 1; i <= 10; i++) {
                        trainingImagePaths.add(trainPath + domain + "/" + fileName + " " + i + ".jpg");
                    }
                }

                // Extract 2x2 blocks from training images
                ImageProcessor imageProcessor = new ImageProcessor();
                List<double[]> redBlocks = new ArrayList<>();
                List<double[]> greenBlocks = new ArrayList<>();
                List<double[]> blueBlocks = new ArrayList<>();
                for (String imagePath : trainingImagePaths) {
                    int[][][] rgbChannels = imageProcessor.loadImage(imagePath);
                    redBlocks.addAll(imageProcessor.getBlocks(rgbChannels[0]));
                    greenBlocks.addAll(imageProcessor.getBlocks(rgbChannels[1]));
                    blueBlocks.addAll(imageProcessor.getBlocks(rgbChannels[2]));
                }

                // Generate and save codebooks
                redCodebook.generateCodebook(redBlocks);
                greenCodebook.generateCodebook(greenBlocks);
                blueCodebook.generateCodebook(blueBlocks);
                redCodebook.saveCodebook(codebookFiles[0]);
                greenCodebook.saveCodebook(codebookFiles[1]);
                blueCodebook.saveCodebook(codebookFiles[2]);
            }

            // Process test images
            ImageProcessor processor = new ImageProcessor();
            VQCompressor compressor = new VQCompressor();
            QualityMetrics metrics = new QualityMetrics();
            Map<String, List<Double>> mseByDomain = new HashMap<>();
            Map<String, List<Double>> crByDomain = new HashMap<>();
            String[] domains = {"nature", "faces", "animals"};
            String[] fileNames = {"Nature", "Faces", "Animals"};
            for (int index = 0; index < domains.length; index++) {
                String domain = domains[index];
                String fileName = fileNames[index];
                mseByDomain.put(domain, new ArrayList<>());
                crByDomain.put(domain, new ArrayList<>());
                for (int i = 1; i <= 5; i++) {
                    String path = testPath + domain + "/" + fileName + " " + i + ".jpg";
                    String compFile = compressedPath + domain + i + ".txt";
                    String decompFile = decompressedPath + domain + i + "_decompressed.png";
                    int[][][] rgb = processor.loadImage(path);
                    int[][] red = rgb[0], green = rgb[1], blue = rgb[2];

                    List<double[]> redBlocks = processor.getBlocks(red);
                    List<double[]> greenBlocks = processor.getBlocks(green);
                    List<double[]> blueBlocks = processor.getBlocks(blue);
                    int[][] redLabels = compressor.compressImage(redBlocks, redCodebook);
                    int[][] greenLabels = compressor.compressImage(greenBlocks, greenCodebook);
                    int[][] blueLabels = compressor.compressImage(blueBlocks, blueCodebook);
                    compressor.saveCompressedIndices(redLabels, greenLabels, blueLabels, compFile);

                    int[][] reconRed = compressor.reconstructComponent(redLabels, redCodebook, red.length, red[0].length);
                    int[][] reconGreen = compressor.reconstructComponent(greenLabels, greenCodebook, green.length, green[0].length);
                    int[][] reconBlue = compressor.reconstructComponent(blueLabels, blueCodebook, blue.length, blue[0].length);
                    processor.saveImage(reconRed, reconGreen, reconBlue, decompFile);

                    double mse = metrics.calculateMSE(rgb, new int[][][]{reconRed, reconGreen, reconBlue});
                    double cr = metrics.calculateCompressionRatio(red.length, red[0].length, redLabels.length * redLabels[0].length, 256);
                    mseByDomain.get(domain).add(mse);
                    crByDomain.get(domain).add(cr);
                    System.out.printf("Test Image: %s %d.jpg, MSE: %.2f, Compression Ratio: %.2f\n",
                            fileName, i, mse, cr);
                }
            }

            // Print average metrics per domain
            for (String domain : domains) {
                double avgMSE = mseByDomain.get(domain).stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double avgCR = crByDomain.get(domain).stream().mapToDouble(Double::doubleValue).average().orElse(0);
                System.out.printf("Domain: %s, Avg MSE: %.2f, Avg Compression Ratio: %.2f\n",
                        domain, avgMSE, avgCR);
            }

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }
}