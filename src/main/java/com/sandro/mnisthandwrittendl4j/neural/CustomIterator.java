/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sandro.mnisthandwrittendl4j.neural;

import com.sandro.mnisthandwrittendl4j.Constants;
import com.sandro.mnisthandwrittendl4j.model.ImageModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Aleksandr_Thchelkuno
 */
public class CustomIterator implements DataSetIterator {

    private final List<ImageModel> images;
    private Iterator<ImageModel> imagesIterator;

    public CustomIterator(List<ImageModel> images) {
        this.images = images;
        this.imagesIterator = images.iterator();
    }

    @Override
    public DataSet next() {
        INDArray input = Nd4j.zeros(1, Constants.AMOUNT_OF_PIXELS_IN_IMAGE);
        ImageModel image = imagesIterator.next();
        input.putRow(0, Nd4j.create(image.getPixelsArray()));
        DataSet ds = new DataSet(input, createExpectedOutput(image));
        return ds;
    }

    public INDArray createExpectedOutput(ImageModel image) {
        INDArray expectedOutput = Nd4j.zeros(1, Constants.AMOUNT_OF_DIGITS);
        expectedOutput.putScalar(new int[]{0, image.getDigit()}, Constants.NUMERIC_TRUE);
        return expectedOutput;
    }

    @Override
    public DataSet next(int num) {
        List<ImageModel> trainingImages = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            trainingImages.add(imagesIterator.next());
        }

        DataSet ds = new DataSet(createInput(trainingImages), createExpectedOutput(trainingImages));
        return ds;
    }

    public INDArray createInput(List<ImageModel> images) {
        INDArray input = Nd4j.zeros(images.size(), Constants.AMOUNT_OF_PIXELS_IN_IMAGE);
        for (int imageCount = 0; imageCount < images.size(); imageCount++) {
            ImageModel image = images.get(imageCount);
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

    @Override
    public int inputColumns() {
        return Constants.AMOUNT_OF_PIXELS_IN_IMAGE;
    }

    @Override
    public int totalOutcomes() {
        return Constants.AMOUNT_OF_DIGITS;
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {
        this.imagesIterator = images.iterator();
    }

    @Override
    public int batch() {
        return 10;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {

    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getLabels() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return imagesIterator.hasNext();
    }

}
