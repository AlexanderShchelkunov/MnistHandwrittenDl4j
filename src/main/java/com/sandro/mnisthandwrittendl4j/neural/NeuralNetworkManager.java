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
import static com.sandro.mnisthandwrittendl4j.Constants.MODEL_FILE_PATH;
import com.sandro.mnisthandwrittendl4j.EmpiricalValues;
import java.io.File;
import java.io.IOException;
import org.datavec.api.records.reader.RecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author AlexandrShchelkunov
 */
public class NeuralNetworkManager {

    public void test(MultiLayerNetwork network, INDArray input, INDArray expectedOutput) {
        // let Evaluation prints stats how often the right output had the highest value
        INDArray output = network.output(input);
        // System.out.println(output);
        System.out.println("Evaluation error rate.");
        Evaluation eval = new Evaluation(Constants.AMOUNT_OF_DIGITS);
        eval.eval(expectedOutput, output);
        System.out.println(eval.stats());
    }

    public void train(MultiLayerNetwork network, DataSet ds) throws IOException {
        // here the actual learning takes place
        System.out.println("Start training.");
        long totalMs = 0;
        for (int i = 0; i < EmpiricalValues.TRAINING_EPOCHS; i++) {
            long start = System.currentTimeMillis();
            network.fit(ds);
            long end = System.currentTimeMillis();
            System.out.printf("Iteration %s is over. Took %s ms.%n", i, end - start);
            totalMs += end - start;
            System.out.printf("Total time spent %s hours %s minutes %s seconds.%n",
                    totalMs / 3600000, (totalMs % 3600000) / 60000, ((totalMs % 3600000) % 60000) / 1000);
            if (i != 0 && i % 500 == 0) {
                System.out.println("Saving intermediate model.");
                ModelSerializer.writeModel(network, MODEL_FILE_PATH, true);
            }
        }
    }

    public void train1(MultiLayerNetwork network, RecordReader recordReader) throws IOException {
        // here the actual learning takes place
        System.out.println("Start training.");
        DataSetIterator iter = new RecordReaderDataSetIterator(recordReader, 2);
        for (int i = 0; i < EmpiricalValues.TRAINING_EPOCHS; i++) {
            System.out.println("Training iteration #" + i);
            network.fit(iter);
//            if (i > 0 && i % 100 == 0) {
            saveModel(network);
//            }
        }
    }

    public void train2(MultiLayerNetwork network, DataSetIterator iterator) throws IOException {
        for (int i = 0; i < EmpiricalValues.TRAINING_EPOCHS; i++) {
            System.out.println("Training iteration #" + (i + 1));
            network.fit(iterator);
            saveModel(network);
        }
    }

    public void saveModel(MultiLayerNetwork network) throws IOException {
        System.out.println("Saving model.");
        ModelSerializer.writeModel(network, MODEL_FILE_PATH, true);
    }

    public MultiLayerNetwork createNetwork() throws IOException {
        File modelFile = new File(Constants.MODEL_FILE_PATH);
        if (modelFile.exists()) {
            MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(modelFile);
            return net;
        }

        // Set up network configuration
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        // Updater and learning rate
        builder.updater(new Sgd(EmpiricalValues.LEARNING_RATE));
        // fixed seed for the random generator, so any run of this program
        // brings the same results - may not work if you do something like ds.shuffle()
        builder.seed(Constants.SEED);
        // init the bias with 0 - empirical value, too
        builder.biasInit(EmpiricalValues.BIAS_INIT);
        // from "http://deeplearning4j.org/architecture": The networks can
        // process the input more quickly and more accurately by ingesting
        // minibatches 5-10 elements at a time in parallel.
        builder.miniBatch(EmpiricalValues.MINI_BATCHES);

        // create a multilayer network with 2 layers (including the output
        // layer, excluding the input payer)
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();

        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
        // 784 input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(Constants.AMOUNT_OF_PIXELS_IN_IMAGE);

        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(EmpiricalValues.HIDDEN_LEVEL_NEURONES_COUNT);
        // put the output through the sigmoid function, to cap the output
        // valuebetween 0 and 1
        hiddenLayerBuilder.activation(Activation.RELU);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 0
        listBuilder.layer(0, hiddenLayerBuilder.build());

        // MCXENT or NEGATIVELOGLIKELIHOOD (both are mathematically equivalent) work ok for this example - this
        // function calculates the error-value (aka 'cost' or 'loss function value'), and quantifies the goodness
        // or badness of a prediction, in a differentiable way
        // For classification (with mutually exclusive classes, like here), use multiclass cross entropy, in conjunction
        // with softmax activation function
        OutputLayer.Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD);
        // must be the same amout as neurons in the layer before
        outputLayerBuilder.nIn(EmpiricalValues.HIDDEN_LEVEL_NEURONES_COUNT);
        // two neurons in this layer
        outputLayerBuilder.nOut(Constants.AMOUNT_OF_DIGITS);
        outputLayerBuilder.activation(Activation.SOFTMAX);
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        outputLayerBuilder.dist(new UniformDistribution(0, 1));
        listBuilder.layer(1, outputLayerBuilder.build());

        // seems to be mandatory
        // according to agibsonccc: You typically only use that with
        // pretrain(true) when you want to do pretrain/finetune without changing
        // the previous layers finetuned weights that's for autoencoders and
        // rbms
//        listBuilder.backprop(true);

        // build and init the network, will check if everything is configured
        // correct
        MultiLayerConfiguration conf = listBuilder.build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        // add an listener which outputs the error every {@link ParameterValues#SCORE_LISTENER_ITERATIONS} parameter updates
        net.setListeners(new ScoreIterationListener(EmpiricalValues.SCORE_LISTENER_ITERATIONS));

        // C&P from LSTMCharModellingExample
        // Print the number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        long totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            long nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }

        System.out.println("Total number of network parameters: " + totalNumParams);
        return net;
    }

    public MultiLayerNetwork createNetwork1() throws IOException {
        int nChannels = 1; // Number of input channels???
        int seed = 123; // ??

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(1e-3))
                .miniBatch(true)
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        //Note that nIn need not be specified in later layers
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(100).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(Constants.AMOUNT_OF_DIGITS)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1)) //See note below
                .build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(EmpiricalValues.SCORE_LISTENER_ITERATIONS)); //Print score every 10 iterations and evaluate on test set every epoch
        return model;
    }
}
