/*
 * Copyright 2019 Alexandr Shchelkunov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sandro.mnisthandwrittendl4j.mnist;

import com.sandro.mnisthandwrittendl4j.Constants;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author AlexandrShchelkunov
 */
public class JpegImageConverter {

    private static final int IMAGES_OFFSET = 16;
    private static final int LABELS_OFFSET = 8;

    public void mnistDbToJpegImages(String sourceFile, String sourceLabelsFile, String destFolder) {
        List<String> labels = readImagesLabels(sourceLabelsFile);
        File file = new File(sourceFile);
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(IMAGES_OFFSET);
            int curPixel = 0;
            int color;
            int[] pixels = new int[Constants.AMOUNT_OF_PIXELS_IN_IMAGE];
            int curImage = 0;
            while ((color = fis.read()) != -1) {
                pixels[curPixel] = color;
                curPixel++;
                if (curPixel >= Constants.AMOUNT_OF_PIXELS_IN_IMAGE) {
                    curImage++;
                    if (curImage % 10000 == 0) {
                        System.out.printf("Images parsed %s.%n", curImage);
                    }

                    saveImageToDisk(pixels, curImage, labels.get(curImage - 1), destFolder);
                    pixels = new int[Constants.AMOUNT_OF_PIXELS_IN_IMAGE];
                    curPixel = 0;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<String> readImagesLabels(String sourceLabelsFile) {
        File file = new File(sourceLabelsFile);
        List<String> labels = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(LABELS_OFFSET);
            int label;
            while ((label = fis.read()) != -1) {
                labels.add(String.valueOf(label));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return labels;
    }

    private static void saveImageToDisk(int[] pixels, int curImage, String label, String destFolder) throws IOException {
        BufferedImage img = new BufferedImage(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGTH, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < Constants.IMAGE_HEIGTH; y++) {
            for (int x = 0; x < Constants.IMAGE_WIDTH; x++) {
                int pix = pixels[y * Constants.IMAGE_HEIGTH + x];
                img.setRGB(x, y, getRgbColor(pix));
            }
        }

        File file = new File(destFolder + curImage + "-" + label + "." + Constants.IMAGES_EXTENSION);
        ImageIO.write(img, Constants.IMAGES_EXTENSION, file);
    }

    private static int getRgbColor(int color) {
        int red = color;
        int green = color;
        int blue = color;
        // some magic
        red = (red << 16) & 0x00FF0000;
        green = (green << 8) & 0x0000FF00;
        blue = blue & 0x000000FF;
        int resultColor = 0xFF000000 | red | green | blue;
        return 0xFFFFFF - resultColor; // invert color (I want white background instead of black).
    }
}
