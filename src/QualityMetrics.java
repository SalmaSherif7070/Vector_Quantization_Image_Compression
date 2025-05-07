public class QualityMetrics {
    public double calculateMSE(int[][][] original, int[][][] reconstructed) {
        double mse = 0;
        int h = original[0].length, w = original[0][0].length;
        for (int c = 0; c < 3; c++) for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
            mse += Math.pow(original[c][y][x] - reconstructed[c][y][x], 2);
        return mse / (w * h * 3);
    }

    // Calculate Peak Signal-to-Noise Ratio
    public double calculatePSNR(double mse) {
        return mse == 0 ? Double.POSITIVE_INFINITY : 10 * Math.log10(255.0 * 255.0 / mse);
    }

    // Calculate compression ratio
    public double calculateCompressionRatio(int width, int height, int numBlocks, int codebookSize) {
        int originalBits = width * height * 24;
        int compressedBits = numBlocks * (int) Math.ceil(Math.log(codebookSize) / Math.log(2)) * 3;
        return (double) originalBits / compressedBits;
    }
}