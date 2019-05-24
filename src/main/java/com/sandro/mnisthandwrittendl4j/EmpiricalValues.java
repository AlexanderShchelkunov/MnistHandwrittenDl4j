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
public class EmpiricalValues {

    /**
     * Defines how often listener prints training results in console.
     */
    public static final int SCORE_LISTENER_ITERATIONS = 10;

    /**
     * Amount of training iterations. E.g. Amount of images (60 000) * 10 000 iterations = 600 000 000 trainings = a lot of time on CPU = makes me cry.
     */
    public static final int TRAINING_ITERATIONS = 1000;

    /**
     * Amount of neurons on the hidden layer. There are some empirically-derived rules-of-thumb, of these, the most commonly relied on is 'the optimal size of
     * the hidden layer is usually between the size of the input and size of the output layers. In sum, for most problems, one could probably get decent
     * performance (even without a second optimization step) by setting the hidden layer configuration using just two rules: (i) number of hidden layers equals
     * one; and (ii) the number of neurons in that layer is the mean of the neurons in the input and output layers.
     */
    public static final int HIDDEN_LEVEL_NEURONES_COUNT = (Constants.AMOUNT_OF_PIXELS_IN_IMAGE + Constants.AMOUNT_OF_DIGITS) / 2;

    /**
     * How fast neural network learns.
     */
    public static final double LEARNING_RATE = 0.1;

    /**
     * Value will be applied to all applicable layers in the network, unless a different value is explicitly set on a given layer. In other words: value is used
     * as the default value, and can be overridden on a per-layer basis.
     */
    public static final double BIAS_INIT = 0;

    /**
     * The networks can process the input more quickly and more accurately by ingesting mini batches 5-10 elements at a time in parallel.
     */
    public static final boolean MINI_BATCHES = false;

    private EmpiricalValues() {
    }
}
