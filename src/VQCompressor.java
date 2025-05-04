import java.io.*;
import java.util.*;

public class VQCompressor {
    public int[][] compressImage(List<double[]> blocks, Codebook codebook) {
        int blockSize = 2;
        int width = 1000 / blockSize;
        int height = 1000 / blockSize;
        int[][] labels = new int[height][width];
        int blockIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                labels[y][x] = codebook.findNearestVector(blocks.get(blockIndex++));
            }
        }
        return labels;
    }

    public int[][] reconstructComponent(int[][] labels, Codebook codebook, int width, int height) {
        int blockSize = 2;
        int[][] component = new int[height][width];

        for (int y = 0; y < labels.length; y++) {
            for (int x = 0; x < labels[0].length; x++) {
                double[] vector = codebook.getCodebook().get(labels[y][x]);
                int index = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        component[y * blockSize + i][x * blockSize + j] = (int) vector[index++];
                    }
                }
            }
        }
        return component;
    }

    public void saveCompressedIndices(int[][] redLabels, int[][] greenLabels, int[][] blueLabels, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("Red:\n");
            for (int[] row : redLabels) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(row[i] + (i < row.length - 1 ? "," : ""));
                }
                writer.newLine();
            }
            writer.write("Green:\n");
            for (int[] row : greenLabels) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(row[i] + (i < row.length - 1 ? "," : ""));
                }
                writer.newLine();
            }
            writer.write("Blue:\n");
            for (int[] row : blueLabels) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(row[i] + (i < row.length - 1 ? "," : ""));
                }
                writer.newLine();
            }
        }
    }
}