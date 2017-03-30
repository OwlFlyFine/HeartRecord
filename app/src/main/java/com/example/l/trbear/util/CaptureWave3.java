package com.example.l.trbear.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by L on 12-Feb-16.
 */
public class CaptureWave3 {
    private Short[] wave;
    private Short[] waveBandpass;
    private ArrayList<Short[]> dataList;

    public void process(){
        dataList = new ArrayList<>();
        int indexStart = 0, indexLast = 0;          // index for capture wave
        int countAmp = 0, countWidthAmp = 0;        // count for check max amp of heart
        boolean foundData = false;
        if(waveBandpass.length > 5000){     // check input
            for(int i  = 0; i < waveBandpass.length-1001; i++){
                //The crosses the x axis
                if(waveBandpass[i] >= 0 && waveBandpass[i+1] < 0){
                    // value is negative
                    if(waveBandpass[i+5] < -2000 || waveBandpass[i+15] < -1000){    // check Amp at index
                        indexLast = i-120;
                        foundData = true;
                        countAmp++;
                        countWidthAmp = 0;
                    }
                }
                else if(waveBandpass[i] < 0 && waveBandpass[i+1] >= 0){
                    // value is positive
                    if(waveBandpass[i+5] > 2000  || waveBandpass[i+15] > 1000){     // check Amp at index
                        indexLast = i-120;
                        foundData = true;
                        countAmp++;
                        countWidthAmp = 0;
                    }
                }
                countWidthAmp++;
                if (countWidthAmp >= 60) {
                    countAmp = 0;
                }
                // capture
                if(foundData){
                    foundData = false;
                    if(countAmp == 2){
                        List<Short> listTamp = new ArrayList<>();
                        Log.i("test", "index capture : " + indexStart + " - " + indexLast);
                        listTamp.addAll(Arrays.asList(wave).subList(indexStart, indexLast));
                        Short[] tamp = {};
                        tamp = listTamp.toArray(tamp);
                        Log.i("test", "tamp Length : " + tamp.length);
                        dataList.add(tamp);
                        indexStart = indexLast+1;
                        i += 1500;
                        if(i > waveBandpass.length){
                            break;
                        }
                    }
                }

            }
        }
        findS1();
        jointS1S2();
    }

    private void findS1(){
        Short[] tamp1 = {};
        Short[] tamp2 = {};
        Short[] tamp3 = {};
        Short[] tamp4 = {};
        tamp1 = dataList.get(3);
        tamp2 = dataList.get(4);
        tamp3 = dataList.get(5);
        tamp4 = dataList.get(6);
        ArrayList<Short[]> arrayListDataList = new ArrayList<>();
        if(tamp1.length < tamp2.length && tamp3.length < tamp4.length){
            for(int i = 1; i < dataList.size(); i++){
                arrayListDataList.add(dataList.get(i));
            }
        }
        else{
            for(int i = 2; i < dataList.size(); i++){
                arrayListDataList.add(dataList.get(i));
            }
        }
        dataList.clear();
        dataList = arrayListDataList;
    }

    private void jointS1S2(){
        ArrayList<Short[]> arrayListDataList = new ArrayList<>();
        for(int i = 0; i < dataList.size()-1; i+=2){
            Short[] tamp1 = {};
            Short[] tamp2 = {};
            tamp1 = dataList.get(i);
            tamp2 = dataList.get(i+1);
            Short[] tampJoint = new Short[tamp1.length+tamp2.length];
            int indexTampJoint = 0;
            for(int j = 0; j < tamp1.length; j++){
                tampJoint[indexTampJoint] = tamp1[j];
                indexTampJoint++;
            }
            for(int j = 0; j < tamp2.length; j++){
                tampJoint[indexTampJoint] = tamp2[j];
                indexTampJoint++;
            }
            arrayListDataList.add(tampJoint);
        }
        dataList.clear();
        dataList = arrayListDataList;
    }

    public Short[] getWave() {
        return wave;
    }

    public void setWave(Short[] wave) {
        this.wave = wave;
    }

    public Short[] getWaveBandpass() {
        return waveBandpass;
    }

    public void setWaveBandpass(Short[] waveBandpass) {
        this.waveBandpass = waveBandpass;
    }

    public ArrayList<Short[]> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<Short[]> dataList) {
        this.dataList = dataList;
    }


}
