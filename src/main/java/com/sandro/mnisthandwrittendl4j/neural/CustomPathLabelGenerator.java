/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sandro.mnisthandwrittendl4j.neural;

import java.io.File;
import java.net.URI;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

/**
 *
 * @author Aleksandr_Thchelkuno
 */
public class CustomPathLabelGenerator implements PathLabelGenerator {

    @Override
    public Writable getLabelForPath(String path) {
        String label = String.valueOf(path.split("-", 2)[1].charAt(0));
//        int intLabel = Integer.parseInt(label);
//        return new IntWritable(intLabel);
        return new Text(label);
    }

    @Override
    public Writable getLabelForPath(URI uri) {
        return getLabelForPath(new File(uri).toString());
    }

    @Override
    public boolean inferLabelClasses() {
        return true;
    }

}
