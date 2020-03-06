package com.sandro.mnisthandwrittendl4j.neural;

import com.sandro.mnisthandwrittendl4j.Constants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Random;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.api.writable.Writable;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class ImageIteratorFactory {

    public DataSetIterator get(String path) {
        PathLabelGenerator pathLabelGenerator = new PathLabelGenerator() {
            @Override
            public Writable getLabelForPath(String path) {
                int number = Integer.parseInt(path.split("-", 2)[1].replace("." + Constants.IMAGES_EXTENSION, ""));
                return new CustomWritable(number);
            }

            @Override
            public Writable getLabelForPath(URI uri) {
                return getLabelForPath(uri.getPath());
            }

            @Override
            public boolean inferLabelClasses() {
                return false;
            }
        };

        ImageRecordReader recordReader = new ImageRecordReader(Constants.IMAGE_HEIGTH, Constants.IMAGE_WIDTH, 1, pathLabelGenerator);
        File parentDir = new File(path);
        Random random = new Random();
        FileSplit filesInDir = new FileSplit(parentDir, new String[]{Constants.IMAGES_EXTENSION}, random);
        BalancedPathFilter pathFilter = new BalancedPathFilter(random, new String[]{Constants.IMAGES_EXTENSION}, pathLabelGenerator);
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter);
        InputSplit trainData = filesInDirSplit[0];
        try {
            recordReader.initialize(trainData);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, 10, 1, Constants.AMOUNT_OF_DIGITS);
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);
        return dataIter;
    }
}
