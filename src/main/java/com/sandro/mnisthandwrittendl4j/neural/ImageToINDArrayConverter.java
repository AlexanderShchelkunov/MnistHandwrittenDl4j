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
package com.sandro.mnisthandwrittendl4j.neural;

import com.sandro.mnisthandwrittendl4j.Constants;
import com.sandro.mnisthandwrittendl4j.model.ImageModel;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author AlexandrShchelkunov
 */
public class ImageToINDArrayConverter {

    public INDArray createInput(List<ImageModel> images) {
        INDArray input = Nd4j.zeros(images.size(), Constants.AMOUNT_OF_PIXELS_IN_IMAGE);
        for (int imageCount = 0; imageCount < images.size(); imageCount++) {
            ImageModel image = images.get(imageCount);
//            for (int pixelCount = 0; pixelCount < Constants.AMOUNT_OF_PIXELS_IN_IMAGE; pixelCount++) {
//                float pixelColor = image.getPixelsArray()[pixelCount];
//                input.putScalar(new int[]{imageCount, pixelCount}, pixelColor);
//            }
            input.putRow(imageCount, Nd4j.create(image.getPixelsArray()));
        }

        return input;
    }

    public INDArray createExpectedOutput(List<ImageModel> images) {
        INDArray expectedOutput = Nd4j.zeros(images.size(), Constants.AMOUNT_OF_DIGITS);
        for (int imageCount = 0; imageCount < images.size(); imageCount++) {
            ImageModel image = images.get(imageCount);
            expectedOutput.putScalar(new int[]{imageCount, image.getDigit()}, Constants.NUMERIC_TRUE);
        }

        return expectedOutput;
    }
}
