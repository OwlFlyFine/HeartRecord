package com.example.l.trbear.util;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Created by L on 07-Feb-16.
 */
public class BandPassFilter {
    private final double nyquistFreq;
    private final int order;
    private final double minFrequancy;
    private final double maxFrequancy;
    private final double dcGain;

    public BandPassFilter(int sampleRate, int order, double minFrequancy, double maxFrequancy, double dcGain) {
        nyquistFreq = sampleRate / 2d;
        this.order = order;
        this.minFrequancy = minFrequancy;
        this.maxFrequancy = maxFrequancy;
        this.dcGain = dcGain;

    }

    private double[] generateGain(int bins) {
        double[] gain = new double[bins];
        double binWidth = nyquistFreq / (double)bins;
        double centre = (minFrequancy + maxFrequancy)/2;
        double cutoff = (maxFrequancy - minFrequancy)/2;
        for (int i = 0; i < bins; i++) {
            gain[i] = dcGain/Math.sqrt((1+Math.pow(((double)i*binWidth-centre)/cutoff, 2.0*order)));
        }
        return gain;
    }

    public Short[] applyFilter(Short[] wave) {
        int length = wave.length;
        DoubleFFT_1D fft = new DoubleFFT_1D(length);
        double[] fftSamples = new double[length];
        int bins = fftSamples.length / 2;
        double[] gain = generateGain(bins);
        for (int i = 0; i < length; i++) fftSamples[i] = wave[i];
        fft.realForward(fftSamples);
        for (int i = 0; i < bins; i++) {
            fftSamples[2*i] *= gain[i];
            fftSamples[2*i+1] *= gain[i];
        }
        fft.realInverse(fftSamples, true);
        for (int i = 0; i < length; i++) wave[i] = (short)fftSamples[i];
        return wave;
    }

}

//https://github.com/benelliott/spectrogram-android/blob/master/SpectrogramAndroid/src/uk/co/benjaminelliott/spectrogramandroid/audioproc/filters/BandpassButterworth.java