/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sandro.mnisthandwrittendl4j.neural;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.datavec.api.writable.Writable;
import org.datavec.api.writable.WritableType;

/**
 *
 * @author Aleksandr_Thchelkuno
 */
public class CustomWritable implements Writable {

    private final int label;

    public CustomWritable(int label) {
        this.label = label;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeType(DataOutput out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double toDouble() {
        return label;
    }

    @Override
    public float toFloat() {
        return label;
    }

    @Override
    public int toInt() {
        return label;
    }

    @Override
    public long toLong() {
        return label;
    }

    @Override
    public WritableType getType() {
        return WritableType.Int;
    }

}
