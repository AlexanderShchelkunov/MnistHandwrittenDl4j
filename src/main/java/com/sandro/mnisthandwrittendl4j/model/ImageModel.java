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
package com.sandro.mnisthandwrittendl4j.model;

import com.sandro.mnisthandwrittendl4j.Constants;

/**
 *
 * @author AlexandrShchelkunov
 */
public class ImageModel {

    private int digit;
    private final float[] pixelsArray;

    public ImageModel(float[] pixelsArray) {
        if (pixelsArray.length != Constants.AMOUNT_OF_PIXELS_IN_IMAGE) {
            throw new IllegalArgumentException();
        }

        this.pixelsArray = pixelsArray;
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    public int getDigit() {
        return digit;
    }

    public float[] getPixelsArray() {
        return pixelsArray;
    }
}
