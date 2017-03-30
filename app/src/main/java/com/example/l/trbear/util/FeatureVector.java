package com.example.l.trbear.util;

import android.util.Log;

/**
 * Created by L on 03-Jun-16.
 */
public class FeatureVector {
    private double[] spectrum;
    private double[] featureVector;

    public void findFeatureVector(int indexStart, int numberOfSample, int increments){
        featureVector = new double[numberOfSample];
        int number = 0;
        for(int i = indexStart; number < numberOfSample; i += increments){ // must <=
            featureVector[number] = spectrum[i];
            number++;
           // Log.i("featureVector", "number : " + numberOfSample);
        }
    }

    public void findFeatureVectorAvg(int position){
        featureVector = new double[10];
        featureVector[0] = position/10.0;
        featureVector[1] = Avg(20, 150);    //S1,S2
        featureVector[2] = Avg(70, 130);    //Aortic stenosis 70 – 130
        featureVector[3] = Avg(60, 190);    //Aortic regurgitation 60 – 190
        featureVector[4] = Avg(91, 187);    //Mitral regurgitation 91 – 187
        featureVector[5] = Avg(67, 116);    //Mitral stenosis 67 – 116
        featureVector[6] = Avg(86, 128);    //Pulmonic stenosis 86 – 128
        featureVector[7] = Avg(50, 136);    //Tricuspid regurgitation 50 – 136
        featureVector[8] = Avg(52, 432);    //Ventricular septal Defect 52 - 432
        featureVector[9] = Avg(71, 193);    //Atrial septal defect 71 - 193
    }

    public void findFeatureVectorAvg2(int position){
        featureVector = new double[12];
        featureVector[0] = position/10.0;
        featureVector[1] = Avg(20, 50);
        featureVector[2] = Avg(51, 60);
        featureVector[3] = Avg(61, 67);
        featureVector[4] = Avg(68, 86);
        featureVector[5] = Avg(87, 95);
        featureVector[6] = Avg(96, 150);
        featureVector[7] = Avg(151, 200);
        featureVector[8] = Avg(201, 300);
        featureVector[9] = Avg(301, 400);
        featureVector[10] = Avg(401, 500);
        featureVector[11] = Avg(501, 660);
    }

    private double Avg(int indexStart, int indexStop){
        double sum = 0;
        for(int i = indexStart; i <= indexStop; i++){
            sum += spectrum[i];
        }
        return sum/((indexStop-indexStart)+1);
    }
    public double[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(double[] featureVector) {
        this.featureVector = featureVector;
    }

    public double[] getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(double[] spectrum) {
        this.spectrum = spectrum;
    }
}
