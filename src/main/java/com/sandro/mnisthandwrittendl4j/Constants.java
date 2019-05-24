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
package com.sandro.mnisthandwrittendl4j;

/**
 *
 * @author AlexandrShchelkunov
 */
public class Constants {

    public static final String MODEL_FILE_PATH = "./handWriting.zip";
    public static final String TRAINING_IMAGES_FILE_PATH = "./mnist_db/train-images.idx3-ubyte";
    public static final String TRAINING_LABELS_FILE_PATH = "./mnist_db/train-labels.idx1-ubyte";
    public static final String TEST_IMAGES_FILE_PATH = "./mnist_db/t10k-images.idx3-ubyte";
    public static final String TEST_LABELS_FILE_PATH = "./mnist_db/t10k-labels.idx1-ubyte";
    public static final String IMAGES_PATH = "./images/";
    public static final String IMAGES_EXTENSION = "jpeg";

    public static final int IMAGE_WIDTH = 28;
    public static final int IMAGE_HEIGTH = 28;
    public static final int AMOUNT_OF_PIXELS_IN_IMAGE = IMAGE_HEIGTH * IMAGE_WIDTH;
    public static final int LEARN_SET_SIZE = 60000;
    public static final int TEST_SET_SIZE = 10000;
    public static final int MAX_COLOR_CODE = 255;
    public static final int AMOUNT_OF_DIGITS = 10;
    public static final int NUMERIC_TRUE = 1;
    public static final long SEED = 123;

    private Constants() {
    }
}
