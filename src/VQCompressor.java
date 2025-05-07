import java.io.*;
import java.util.*;

// Manages image compression and decompression
public class VQCompressor {
    // Compress blocks to codebook indices
    public int[][] compressImage(List<double[]> blocks, Codebook codebook) {
        int numBlocks = blocks.size();
        int w = (int) Math.ceil(Math.sqrt(numBlocks));
        int h = (numBlocks + w - 1) / w;
        int[][] labels = new int[h][w];
        for (int i = 0; i < numBlocks; i++) labels[i / w][i % w] = codebook.findNearestVector(blocks.get(i));
        return labels;
    }

    // Reconstructs an image component from compressed indices
    public int[][] reconstructComponent(int[][] labels, Codebook codebook, int height, int width) {
        int[][] component = new int[height][width];
        List<double[]> vectors = codebook.getCodebook();
        int blockSize = 2;

        for (int blockY = 0; blockY < labels.length; blockY++) {
            for (int blockX = 0; blockX < labels[0].length; blockX++) {
                int label = labels[blockY][blockX];
                if (label >= 0 && label < vectors.size()) {
                    double[] vector = vectors.get(label);
                    for (int i = 0, index = 0; i < blockSize; i++) {
                        for (int j = 0; j < blockSize; j++, index++) {
                            int pixelY = blockY * blockSize + i;
                            int pixelX = blockX * blockSize + j;
                            if (pixelY < height && pixelX < width) {
                                component[pixelY][pixelX] = (int) vector[index];
                            }
                        }
                    }
                }
            }
        }
        return component;
    }

    // Save compressed indices as 8-bit binary strings to file
    public void saveCompressedIndices(int[][] redLabels, int[][] greenLabels, int[][] blueLabels, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String channel : new String[]{"Red", "Green", "Blue"}) {
                writer.write(channel + ":\n");
                int[][] labels = channel.equals("Red") ? redLabels : channel.equals("Green") ? greenLabels : blueLabels;
                for (int[] row : labels) {
                    String[] binaryIndices = new String[row.length];
                    for (int i = 0; i < row.length; i++) {
                        // Convert index to 8-bit binary string
                        String binary = Integer.toBinaryString(row[i]);
                        binaryIndices[i] = String.format("%8s", binary).replace(' ', '0');
                    }
                    writer.write(String.join(",", binaryIndices) + "\n");
                }
            }
        }
    }
}