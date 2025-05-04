import java.io.*;
import java.util.*;

public class Codebook {
    private int blockSize;
    private int codebookSize;
    private List<double[]> codebook;

    public Codebook(int blockSize, int codebookSize) {
        this.blockSize = blockSize;
        this.codebookSize = codebookSize;
        this.codebook = new ArrayList<>();
    }

    public void generateCodebook(List<double[]> blocks) {
        System.out.println("Starting generateCodebook with " + blocks.size() + " blocks...");
        if (blocks.isEmpty()) {
            System.out.println("Error: No blocks provided to generate codebook.");
            return;
        }

        // Initialize codebook with random vectors
        System.out.println("Initializing codebook with " + codebookSize + " vectors...");
        Random rand = new Random();
        for (int i = 0; i < codebookSize; i++) {
            double[] vector = new double[blockSize * blockSize];
            for (int j = 0; j < vector.length; j++) {
                vector[j] = rand.nextDouble() * 255;
            }
            codebook.add(vector);
        }

        // K-means clustering
        System.out.println("Starting k-means clustering...");
        int maxIterations = 100;
        for (int iter = 0; iter < maxIterations; iter++) {
            System.out.println("Iteration " + (iter + 1) + "...");
            List<List<double[]>> clusters = new ArrayList<>();
            for (int i = 0; i < codebookSize; i++) {
                clusters.add(new ArrayList<>());
            }

            // Assign blocks to nearest codebook vector
            for (double[] block : blocks) {
                int nearest = findNearestVector(block);
                clusters.get(nearest).add(block);
            }

            // Update codebook vectors
            boolean changed = false;
            for (int i = 0; i < codebookSize; i++) {
                List<double[]> cluster = clusters.get(i);
                if (!cluster.isEmpty()) {
                    double[] newVector = new double[blockSize * blockSize];
                    for (double[] block : cluster) {
                        for (int j = 0; j < newVector.length; j++) {
                            newVector[j] += block[j];
                        }
                    }
                    for (int j = 0; j < newVector.length; j++) {
                        newVector[j] /= cluster.size();
                    }
                    
                    if (!Arrays.equals(codebook.get(i), newVector)) {
                        codebook.set(i, newVector);
                        changed = true;
                    }
                }
            }

            // Does not converge most of the time, so I have put max_iterations to 100
            if (!changed) {
                System.out.println("Converged after " + (iter + 1) + " iterations.");
                break;
            }
        }
        System.out.println("Codebook generation completed.");
    }

    public int findNearestVector(double[] block) {
        int nearest = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < codebook.size(); i++) {
            double dist = 0;
            double[] vector = codebook.get(i);
            for (int j = 0; j < block.length; j++) {
                double diff = block[j] - vector[j];
                dist += diff * diff;
            }
            if (dist < minDist) {
                minDist = dist;
                nearest = i;
            }
        }
        return nearest;
    }

    public List<double[]> getCodebook() {
        return codebook;
    }

    public void saveCodebook(String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (double[] vector : codebook) {
                for (int i = 0; i < vector.length; i++) {
                    writer.write(vector[i] + (i < vector.length - 1 ? "," : ""));
                }
                writer.newLine();
            }
        }
    }
}