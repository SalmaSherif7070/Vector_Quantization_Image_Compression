import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

// Handles image loading, block extraction, and saving
public class ImageProcessor {
    // Load RGB image from file
    public int[][][] loadImage(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        int w = img.getWidth();
        int h = img.getHeight();
        int[][] red = new int[h][w];
        int[][] green = new int[h][w];
        int[][] blue = new int[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int rgb = img.getRGB(x, y);
            red[y][x] = (rgb >> 16) & 0xFF;
            green[y][x] = (rgb >> 8) & 0xFF;
            blue[y][x] = rgb & 0xFF;
        }
        return new int[][][]{red, green, blue};
    }

    // Extract 2x2 blocks from image component
    public List<double[]> getBlocks(int[][] component) {
        List<double[]> blocks = new ArrayList<>();
        int h = component.length;
        int w = component[0].length;
        int BlockSize = 2;
        for (int y = 0; y <= h - BlockSize; y += BlockSize){
            for (int x = 0; x <= w - BlockSize; x += BlockSize) {
                double[] block = new double[BlockSize * BlockSize];
                for (int i = 0, idx = 0; i < BlockSize; i++){
                    for (int j = 0; j < BlockSize; j++) block[idx++] = component[y + i][x + j];
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    // Save RGB image to file
    public void saveImage(int[][] red, int[][] green, int[][] blue, String path) throws IOException {
        int h = red.length, w = red[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int r = Math.min(255, Math.max(0, red[y][x]));
            int g = Math.min(255, Math.max(0, green[y][x]));
            int b = Math.min(255, Math.max(0, blue[y][x]));
            img.setRGB(x, y, (r << 16) | (g << 8) | b);
        }
        ImageIO.write(img, "png", new File(path));
    }
}