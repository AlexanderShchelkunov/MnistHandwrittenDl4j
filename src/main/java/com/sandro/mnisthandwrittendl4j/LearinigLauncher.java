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

import com.sandro.mnisthandwrittendl4j.mnist.MnistImagesLoader;
import static com.sandro.mnisthandwrittendl4j.Constants.*;

import com.sandro.mnisthandwrittendl4j.model.ImageModel;
import com.sandro.mnisthandwrittendl4j.neural.ImageToINDArrayConverter;
import com.sandro.mnisthandwrittendl4j.neural.CustomIterator;
import com.sandro.mnisthandwrittendl4j.neural.NeuralNetworkManager;
import java.util.List;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 *
 * @author AlexandrShchelkunov
 */
public class LearinigLauncher {

    public static void main(String[] args) throws Exception {
        // load taining images
        MnistImagesLoader imagesLoader = new MnistImagesLoader();
        List<ImageModel> trainingImages = imagesLoader.loadImages(TRAINING_IMAGES_FILE_PATH, TRAINING_LABELS_FILE_PATH, LEARN_SET_SIZE);

        // train model
        ImageToINDArrayConverter converter = new ImageToINDArrayConverter();
//        DataSet ds = new DataSet(converter.createInput(trainingImages), converter.createExpectedOutput(trainingImages));
        DataSetIterator iterator = new CustomIterator(trainingImages);
        NeuralNetworkManager nnManager = new NeuralNetworkManager();
        MultiLayerNetwork network = nnManager.createNetwork();

//        RecordReader recordReader = new ImageRecordReader(28, 28, 1, new CustomPathLabelGenerator());
        // Point to data path. 
//        recordReader.initialize(new FileSplit(new File(IMAGES_PATH)));
//        nnManager.train1(network, recordReader);
//        nnManager.train(network, ds);
        nnManager.train2(network, iterator);
        nnManager.saveModel(network);

        // load test images
        System.out.println("Loading test images.");
        List<ImageModel> testImages = imagesLoader.loadImages(TEST_IMAGES_FILE_PATH, TEST_LABELS_FILE_PATH, TEST_SET_SIZE);

        // test model
        System.out.println("creating output for test images.");
        INDArray input = converter.createInput(testImages);
        INDArray expectedOutput = converter.createExpectedOutput(testImages);
        nnManager.test(network, input, expectedOutput);

//        DataSetIterator iter = new RecordReaderDataSetIterator(recordReader, 784, 10);
    }
}
