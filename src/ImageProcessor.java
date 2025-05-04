import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {
    public int[][][] loadImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] red = new int[height][width];
        int[][] green = new int[height][width];
        int[][] blue = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                red[y][x] = (rgb >> 16) & 0xFF;
                green[y][x] = (rgb >> 8) & 0xFF;
                blue[y][x] = rgb & 0xFF;
            }
        }
        return new int[][][]{red, green, blue};
    }

    public List<double[]> getBlocks(int[][] component) {
        List<double[]> blocks = new ArrayList<>();
        int height = component.length;
        int width = component[0].length;
        int blockSize = 2;

        for (int y = 0; y < height - blockSize + 1; y += blockSize) {
            for (int x = 0; x < width - blockSize + 1; x += blockSize) {
                double[] block = new double[blockSize * blockSize];
                int index = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        block[index++] = component[y + i][x + j];
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    public void saveImage(int[][] red, int[][] green, int[][] blue, String path) throws IOException {
        int height = red.length;
        int width = red[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = Math.min(255, Math.max(0, red[y][x]));
                int g = Math.min(255, Math.max(0, green[y][x]));
                int b = Math.min(255, Math.max(0, blue[y][x]));
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(image, "png", new File(path));
    }
}