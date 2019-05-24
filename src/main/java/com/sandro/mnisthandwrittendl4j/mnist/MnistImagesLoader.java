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
import com.sandro.mnisthandwrittendl4j.model.ImageModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AlexandrShchelkunov
 */
public class MnistImagesLoader {

    private static final int IMAGES_OFFSET = 16;
    private static final int LABELS_OFFSET = 8;

    public List<ImageModel> loadImages(String imagesPath, String labelsPath, int amountOfImages) {
        try {
            List<ImageModel> images = readImagesPixels(imagesPath, amountOfImages);
            readImagesLabels(labelsPath, images);
            return images;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readImagesLabels(String filePath, List<ImageModel> allImages) throws IOException {
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(LABELS_OFFSET);
            int imageCount = 0;
            int label;
            while ((label = fis.read()) != -1) {
//            System.out.printf("Image %s label is %s%n", imageCount, label);
                allImages.get(imageCount).setDigit(label);
                imageCount++;
            }
        }
    }

    private static List<ImageModel> readImagesPixels(String filePath, int amountOfImages) throws IOException {
        File file = new File(filePath);
        List<ImageModel> allImages = new ArrayList<>(amountOfImages);
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(IMAGES_OFFSET);
            int curPixel = 0;
            int color;
            float[] pixels = new float[Constants.AMOUNT_OF_PIXELS_IN_IMAGE];
            int curImage = 0;
            while ((color = fis.read()) != -1) {
                pixels[curPixel] = (float) color / Constants.MAX_COLOR_CODE; // normalize all input values from 0 to 1, NNs work better with such numbers
                curPixel++;
                if (curPixel >= Constants.AMOUNT_OF_PIXELS_IN_IMAGE) {
                    curImage++;
                    if (curImage % 10000 == 0) {
                        System.out.printf("Images parsed %s.%n", curImage);
                    }

                    ImageModel image = new ImageModel(pixels);
                    allImages.add(image);
                    pixels = new float[Constants.AMOUNT_OF_PIXELS_IN_IMAGE];
                    curPixel = 0;
                }
            }
        }

        return allImages;
    }
}
