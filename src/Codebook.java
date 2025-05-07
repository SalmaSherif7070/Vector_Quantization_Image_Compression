import java.io.*;
import java.util.*;

// Manages codebook for vector quantization
public class Codebook {
    private final int blockSize, codebookSize;
    private final List<double[]> codebook;
    private final String channel;

    // Initialize codebook parameters
    public Codebook(int blockSize, int codebookSize, String channel) {
        this.blockSize = blockSize;
        this.codebookSize = codebookSize;
        this.codebook = new ArrayList<>();
        this.channel = channel;
    }

    // Load codebook from file if it exists
    public Codebook(int blockSize, int codebookSize, String channel, String file) throws IOException {
        this(blockSize, codebookSize, channel);
        if (new File(file).exists()) loadCodebook(file);
    }

    // Read codebook vectors from file
    private void loadCodebook(String path) throws IOException {
        codebook.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == blockSize * blockSize) {
                    double[] vector = new double[values.length];
                    for (int i = 0; i < values.length; i++) vector[i] = Double.parseDouble(values[i]);
                    codebook.add(vector);
                }
            }
        }
    }

    // Generate codebook
    public void generateCodebook(List<double[]> blocks) {
        if (blocks.isEmpty()) return;
        codebook.add(averageBlock(blocks));
        while (codebook.size() < codebookSize) {
            List<double[]> newCodebook = new ArrayList<>();
            for (double[] vector : codebook) {
                double[] v1 = new double[vector.length], v2 = new double[vector.length];
                for (int i = 0; i < vector.length; i++) {
                    v1[i] = vector[i] * 1.01;
                    v2[i] = vector[i] * 0.99;
                }
                newCodebook.add(v1);
                newCodebook.add(v2);
            }
            codebook.clear();
            codebook.addAll(newCodebook);
            refineCodebook(blocks);
        }
        if (codebook.size() > codebookSize) codebook.subList(codebookSize, codebook.size()).clear();
    }

    // Calculate average block centroid
    private double[] averageBlock(List<double[]> blocks) {
        double[] avg = new double[blockSize * blockSize];
        for (double[] block : blocks) for (int i = 0; i < block.length; i++) avg[i] += block[i];
        for (int i = 0; i < avg.length; i++) avg[i] /= blocks.size();
        return avg;
    }

    // Refine codebook with k-means until stable
    private void refineCodebook(List<double[]> blocks) {
        int iter = 0;
        boolean changed;
        do {
            System.out.println(channel + ": " + (++iter));
            changed = false;
            List<List<double[]>> clusters = new ArrayList<>(Collections.nCopies(codebook.size(), null));
            for (int i = 0; i < codebook.size(); i++) clusters.set(i, new ArrayList<>());
            for (double[] block : blocks) clusters.get(findNearestVector(block)).add(block);
            for (int i = 0; i < codebook.size(); i++) {
                if (!clusters.get(i).isEmpty()) {
                    double[] newVector = averageBlock(clusters.get(i));
                    if (euclideanDistance(newVector, codebook.get(i)) > 0.0001) {
                        codebook.set(i, newVector);
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    // Find index of nearest codebook vector
    public int findNearestVector(double[] block) {
        int nearest = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < codebook.size(); i++) {
            double dist = euclideanDistance(block, codebook.get(i));
            if (dist < minDist) {
                minDist = dist;
                nearest = i;
            }
        }
        return nearest;
    }

    // Compute Euclidean distance between vectors
    private double euclideanDistance(double[] v1, double[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) sum += (v1[i] - v2[i]) * (v1[i] - v2[i]);
        return Math.sqrt(sum);
    }

    // Get codebook vectors
    public List<double[]> getCodebook() {
        return codebook;
    }

    // Verify codebook size
    public boolean isValid() {
        return codebook.size() == codebookSize;
    }

    // Save codebook to file
    public void saveCodebook(String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (double[] vector : codebook)
                writer.write(String.join(",", Arrays.stream(vector).mapToObj(String::valueOf).toArray(String[]::new)) + "\n");
        }
    }
}