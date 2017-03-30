package com.example.l.trbear.util;

import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;

/**
 * Created by L on 15-Jan-16.
 */
public class FFT {
    private ArrayList<Short> arrayListShort;
    private int Fs = 8000;
    private int N;
    private double[] spectrum;
    private double[] magnitude = new double[700];             // power spectrum Length 1000

    public FFT(ArrayList<Short> arrayListShort){
        this.arrayListShort = arrayListShort;
    }

    public double[] getSpectrum(){
          /*  Short[] tampShort = {};
        tampShort = (Short[]) arrayListShort.toArray();*/

        Short[] tampArrayShort = new Short[arrayListShort.size()];
        for(int i = 0; i < arrayListShort.size(); i++){
            tampArrayShort[i] = arrayListShort.get(i);
        }
        spectrum = findFFT(convertShortToDouble(tampArrayShort));
        return spectrum;
    }

    public double[] getSpectrum2(double[] data){
        spectrum = findFFT(data);
        return spectrum;
    }

    private double[] convertShortToDouble(Short[] shorts){
        double[] doubles = new double[shorts.length];
        for(int i = 0; i < shorts.length; i++){
            doubles[i] = shorts[i];/// 32768.0;
        }
        return doubles;
    }



    private double[] findFFT(double[] data){
        N = data.length;
        Log.i("fft", "length N : " + N);
        DoubleFFT_1D fft = new DoubleFFT_1D(N);
        double[] dataFFT = new double[N*2];               // fft real + image
        for(int i = 0; i < N; i++){
            dataFFT[2*i] = data[i];
            dataFFT[2*i+1] = 0;
        }
        fft.complexForward(dataFFT);

        Log.i("fft", "length dataFFT : " + dataFFT.length);
        for(int i = 0; i < magnitude.length-1; i++){
            double real = dataFFT[2*i];
            double imag = dataFFT[2*i+1];
            magnitude[i] = Math.sqrt(real*real + imag*imag);
        }

        /*double[] micBufferData = new double[1024];
        final int bytesPerSample = 2; // As it is 16bit PCM
        final double amplification = 100.0; // choose a number as you like
        for (int index = 0, floatIndex = 0; index < 2 - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
            double sample = 0;
            for (int b = 0; b < bytesPerSample; b++) {
                int v = arrayDouble[index + b];
                if (b < bytesPerSample - 1 || bytesPerSample == 1) {
                    v &= 0xFF;
                }
                sample += v << (b * 8);
            }
            double sample32 = amplification * (sample / 32768.0);
            micBufferData[floatIndex] = sample32;
        }
*/
        return magnitude;
    }

    public double getFrequencyFromSpectrum(int index ){
        return index * Fs / N;
    }

    public double getCumulativeFrequency(int index){
        return spectrum[index];
    }

    private void timeDomain(ArrayList<double[]> arrayListDouble){
        double[] tamp = {};
        tamp = arrayListDouble.get(1);

    }

    public ArrayList<Short> getArrayListShort() {
        return arrayListShort;
    }

    public void setArrayListShort(ArrayList<Short> arrayListShort) {
        this.arrayListShort = arrayListShort;
    }
}