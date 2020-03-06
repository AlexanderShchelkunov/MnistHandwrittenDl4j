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
import com.sandro.mnisthandwrittendl4j.neural.ImageIteratorFactory;
import com.sandro.mnisthandwrittendl4j.neural.NeuralNetworkManager;
import java.util.List;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 *
 * @author AlexandrShchelkunov
 */
public class LearinigLauncher {

    public static void main(String[] args) throws Exception {
        // load taining images
        System.out.println("Here we go again");
        MnistImagesLoader imagesLoader = new MnistImagesLoader();
//        DataSetIterator iterator = createMemoryIterator(imagesLoader);
        DataSetIterator iterator = new ImageIteratorFactory().get(Constants.IMAGES_PATH);

        // train model
        ImageToINDArrayConverter converter = new ImageToINDArrayConverter();
//        DataSet ds = new DataSet(converter.createInput(trainingImages), converter.createExpectedOutput(trainingImages));
        NeuralNetworkManager nnManager = new NeuralNetworkManager();
        MultiLayerNetwork network = nnManager.createNetwork1();

//        RecordReader recordReader = new ImageRecordReader(28, 28, 1, new CustomPathLabelGenerator());
        // Point to data path. 
//        recordReader.initialize(new FileSplit(new File(IMAGES_PATH)));
//        nnManager.train1(network, recordReader);
//        nnManager.train(network, ds);
        nnManager.train2(network, iterator);
        nnManager.saveModel(network);

//        test(imagesLoader, converter, nnManager, network);
        test(network);
    }

    private static DataSetIterator createMemoryIterator(MnistImagesLoader imagesLoader) {
        List<ImageModel> trainingImages = imagesLoader.loadImages(TRAINING_IMAGES_FILE_PATH, TRAINING_LABELS_FILE_PATH, LEARN_SET_SIZE);
        DataSetIterator iterator = new CustomIterator(trainingImages);
        return iterator;
    }

    private static void test(MnistImagesLoader imagesLoader, ImageToINDArrayConverter converter, NeuralNetworkManager nnManager, MultiLayerNetwork network) {
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

    private static void test(MultiLayerNetwork network) {
        DataSetIterator iterator = new ImageIteratorFactory().get(Constants.TEST_IMAGES_PATH);
//        // let Evaluation prints stats how often the right output had the highest value
//        INDArray output = network.output(iterator);
//        // System.out.println(output);
//        System.out.println("Evaluation error rate.");
//        Evaluation eval = new Evaluation(Constants.AMOUNT_OF_DIGITS);
//        eval.eval(iterator.get, output);
//        Evaluation eval = network.evaluate(iterator);
        Evaluation evaluation = network.doEvaluation(iterator, new Evaluation(10))[0];
//        eval.
        System.out.println(evaluation.stats());
    }
}
