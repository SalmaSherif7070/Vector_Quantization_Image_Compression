public class QualityMetrics {
    public double calculateMSE(int[][][] original, int[][][] reconstructed) {
        double mse = 0;
        int height = original[0].length;
        int width = original[0][0].length;
        int numComponents = 3; // R, G, B

        for (int c = 0; c < numComponents; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double diff = original[c][y][x] - reconstructed[c][y][x];
                    mse += diff * diff;
                }
            }
        }
        return mse / (width * height * numComponents);
    }

    public double calculatePSNR(double mse) {
        if (mse == 0) return Double.POSITIVE_INFINITY;
        double maxPixelValue = 255.0;
        return 10 * Math.log10((maxPixelValue * maxPixelValue) / mse);
    }

    public double calculateCompressionRatio(int width, int height, int numBlocks, int codebookSize) {
        int originalBits = width * height * 8 * 3; // 8 bits per pixel, 3 components
        int compressedBits = numBlocks * (int) Math.ceil(Math.log(codebookSize) / Math.log(2)) * 3;
        return (double) originalBits / compressedBits;
    }
}