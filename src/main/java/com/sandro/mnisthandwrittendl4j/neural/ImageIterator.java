package com.sandro.mnisthandwrittendl4j.neural;

import com.sandro.mnisthandwrittendl4j.Constants;
import com.sandro.mnisthandwrittendl4j.model.ImageModel;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.api.writable.Writable;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;

public class ImageIterator implements DataSetIterator {

    private Iterator<ImageModel> imagesIterator;

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
//        imagesIterator.
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
        PathLabelGenerator pathLabelGenerator = new PathLabelGenerator() {
            @Override
            public Writable getLabelForPath(String path) {
                return new CustomWritable(Integer.parseInt(path.split("-", 2)[0]));
            }

            @Override
            public Writable getLabelForPath(URI uri) {
                return new CustomWritable(Integer.parseInt(uri.getPath().split("-", 2)[0]));
            }

            @Override
            public boolean inferLabelClasses() {
                return false;
            }
        };

        ImageRecordReader recordReader = new ImageRecordReader(Constants.IMAGE_HEIGTH, Constants.IMAGE_WIDTH, 1, pathLabelGenerator);
        File parentDir = new File(Constants.IMAGES_PATH);
        Random random = new Random();
        FileSplit filesInDir = new FileSplit(parentDir, new String[]{"jpg"}, random);
        BalancedPathFilter pathFilter = new BalancedPathFilter(random, new String[]{"jpg"}, pathLabelGenerator);
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter);
        InputSplit trainData = filesInDirSplit[0];
        try {
            recordReader.initialize(trainData);
        } catch (IOException ex) {

        }
        DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, 10, 1, Constants.AMOUNT_OF_DIGITS);
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);

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
