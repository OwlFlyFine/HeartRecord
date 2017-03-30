package com.example.l.trbear.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by L on 21-Jun-16.
 */
public class FeedForward {
    private double[] input;
    private double output;
    private List<double[]> IW;
    private List<double[]> LW;
    private List<double[]> OW;
    private List<double[]> Ib;
    private List<double[]> Lb;
    private List<double[]> Ob;

    public FeedForward(){
        CSV csv = new CSV();
        List<String[]> tampData;
        tampData = csv.read("IW");
        this.IW = convertListStringToListDouble(tampData);
        tampData = csv.read("LW");
        this.LW = convertListStringToListDouble(tampData);
        tampData = csv.read("OW");
        this.OW = convertListStringToListDouble(tampData);
        tampData = csv.read("Ib");
        this.Ib = convertListStringToListDouble(tampData);
        tampData = csv.read("Lb");
        this.Lb = convertListStringToListDouble(tampData);
        tampData = csv.read("Ob");
        this.Ob = convertListStringToListDouble(tampData);
    }

    public void calculate(){
        output = calculateNodeTanSig(calculateNodeLogSig(calculateNodeLogSig(this.input, IW, Ib), LW, Lb), OW, Ob);
    }

    private List<double[]> convertListStringToListDouble(List<String[]> data) {
        List<double[]> listDouble = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            String[] tamp = data.get(i);
            double[] tampDouble = new double[tamp.length];
            for(int j = 0; j < tamp.length; j++){
                tampDouble[j] = Double.parseDouble(tamp[j]);
            }
            listDouble.add(tampDouble);
        }
        return listDouble;
    }

    private double[] calculateNodeLogSig(double[] input, List<double[]> weight, List<double[]> bias){
        double[] outputHidden = new double[weight.size()];
        for(int i = 0; i < weight.size(); i++){
            double[] itemHidden = weight.get(i);
            double sum = 0;
            for(int j = 0; j < itemHidden.length; j++){
                sum = (input[j]*itemHidden[j]) + sum;
            }
            sum += bias.get(i)[0];
            outputHidden[i] = getLogSig(sum);
        }
        return outputHidden;
    }

    private double calculateNodeTanSig(double[] input, List<double[]> weight, List<double[]> bias){
        double outputHidden = 0;
        double[] itemHidden = weight.get(0);
        double sum = 0;
        for(int j = 0; j < itemHidden.length; j++){
            sum = (input[j]*itemHidden[j]) + sum;
        }
        sum += bias.get(0)[0];
        outputHidden = getTanSig(sum);
        return outputHidden;
    }

    private double getLogSig(double val){
        // logsig(n) = 1 / (1 + exp(-n))
        return 1/(1+Math.exp(-val));
    }

    private double getTanSig(double val){
        // tansig(n) = 2/(1+exp(-2*n))-1
        return 2/(1+Math.exp(-2*val))-1;
    }

    public double[] getInput() {
        return input;
    }

    public void setInput(double[] input) {
        this.input = input;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }
}
