# Vector Quantization Image Compression

## Overview
This project implements vector quantization (VQ) for image compression, processing RGB images by splitting them into 2x2 blocks, quantizing them using codebooks for each channel (Red, Green, Blue), and reconstructing compressed images. The system trains codebooks on a dataset of images from three domains (nature, faces, animals) and evaluates compression quality and efficiency on test images.

## Quality Comparison
The quality of compressed images is assessed using Mean Squared Error (MSE), which measures the average squared difference between pixel values of the original and reconstructed images. Lower MSE indicates better quality (closer to the original). The results for test images are:

- **Nature Domain**:
  - Nature 1.jpg: MSE = 36.85
  - Nature 2.jpg: MSE = 17.06
  - Nature 3.jpg: MSE = 13.45
  - Nature 4.jpg: MSE = 13.42
  - Nature 5.jpg: MSE = 8.32
  - **Average MSE**: 17.82
- **Faces Domain**:
  - Faces 1.jpg: MSE = 7.45
  - Faces 2.jpg: MSE = 6.05
  - Faces 3.jpg: MSE = 6.05
  - Faces 4.jpg: MSE = 6.19
  - Faces 5.jpg: MSE = 19.14
  - **Average MSE**: 8.97
- **Animals Domain**:
  - Animals 1.jpg: MSE = 8.62
  - Animals 2.jpg: MSE = 11.24
  - Animals 3.jpg: MSE = 8.70
  - Animals 4.jpg: MSE = 6.44
  - Animals 5.jpg: MSE = 13.93
  - **Average MSE**: 9.78

### Analysis
- **Faces Domain** has the lowest average MSE (8.97), indicating the best compression quality, likely due to smoother textures or less color variation in facial images.
- **Animals Domain** follows with an average MSE of 9.78, suggesting good quality but slightly more distortion than faces.
- **Nature Domain** has the highest average MSE (17.82), indicating lower quality, possibly due to complex textures or diverse colors in natural scenes.
- Individual image MSE varies significantly (e.g., Nature 1.jpg at 36.85 vs. Faces 2.jpg at 6.05), reflecting differences in image complexity.
- Overall, compressed images retain reasonable quality, with lower MSE values indicating minimal visual distortion, especially for faces and animals.

## Compression Ratio
The compression ratio measures the reduction in data size, calculated as the ratio of original image bits to compressed image bits, assuming no codebook overhead. Each RGB image uses 24 bits per pixel (8 bits per channel). The compressed image represents each 2x2 block with an 8-bit index (for a codebook size of 256), with three indices per block (one per channel).

### Calculation
- **Original Bits**: For an image of width \( W \) and height \( H \), the total bits are:
  \[
  \text{Original Bits} = W \times H \times 24
  \]
- **Compressed Bits**: Each 2x2 block yields one index per channel, and the number of blocks is approximately \( \frac{W}{2} \times \frac{H}{2} \). With 8 bits per index and 3 channels:
  \[
  \text{Compressed Bits} = \left( \frac{W}{2} \times \frac{H}{2} \right) \times 8 \times 3
  \]
- **Compression Ratio**:
  \[
  \text{Compression Ratio} = \frac{\text{Original Bits}}{\text{Compressed Bits}} = \frac{W \times H \times 24}{\left( \frac{W}{2} \times \frac{H}{2} \right) \times 8 \times 3} = \frac{24}{\frac{8 \times 3}{4}} = \frac{24}{6} = 4
  \]
- **Result**: The compression ratio is consistently 4.00 across all test images, as shown in the results, confirming a 4:1 reduction in data size.

### Notes
- The calculation ignores codebook storage (256 vectors of 4 doubles per channel), as specified.
- The fixed ratio of 4.00 assumes uniform block division and no additional metadata.

## How to Run
### Prerequisites
- Java Development Kit (JDK) installed.
- Input images organized in:
  - `data/input/train/{nature,faces,animals}/` (10 images per domain, e.g., `Nature 1.jpg` to `Nature 10.jpg`).
  - `data/input/test/{nature,faces,animals}/` (5 images per domain, e.g., `Nature 1.jpg` to `Nature 5.jpg`).

### Steps
1. **Compile the Code**:
   ```bash
   javac -d bin src/*.java
   ```
   This compiles all Java files in `src/` to the `bin/` directory.

2. **Run the Program**:
   ```bash
   java -cp bin VectorQuantization
   ```
   This executes the main class, performing:
   - Codebook generation (if `data/output/codebooks/{red,green,blue}_codebook.txt` are missing or invalid).
   - Compression and decompression of test images.
   - Output of MSE and compression ratio per image and domain.

### Output
- **Codebooks**: Saved in `data/output/codebooks/` (e.g., `red_codebook.txt`).
- **Compressed Indices**: Saved in `data/output/compressed/` (e.g., `nature1.txt`).
- **Decompressed Images**: Saved in `data/output/decompressed/` (e.g., `nature1_decompressed.png`).
- **Console**: Displays MSE and compression ratio for each test image and average metrics per domain.

### Notes
- If codebooks exist and are valid, the program skips generation and processes test images only.
- To regenerate codebooks, delete `data/output/codebooks/*.txt`.
- Increase JVM memory if needed (e.g., `java -Xmx4g -cp bin VectorQuantization`).

## Conclusion
The VQ compression achieves a consistent 4:1 compression ratio with varying quality across domains. Faces and animals show better quality (lower MSE) than nature images, reflecting differences in image complexity. The program is efficient and reusable, leveraging existing codebooks to minimize redundant computation.