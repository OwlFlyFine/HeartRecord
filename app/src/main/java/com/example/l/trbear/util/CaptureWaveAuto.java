package com.example.l.trbear.util;

import java.util.ArrayList;

/**
 * Created by BenZDeV on 2/28/2017.
 */

public class CaptureWaveAuto {
    ArrayList<Short> indexWave;
    ArrayList<Short> waveCutAuto;
    int indexStart =0;
    int countBound =10; //check End s2
    protected  Short[] waveBandpass;
    private void process(){
        waveCutAuto = new ArrayList<>();
        waveCutAuto = new ArrayList<>();
        for(int i=0; i<waveBandpass.length;i++){


            if(waveBandpass[i] >=0 && waveBandpass[i+1] < 0){

                if(waveBandpass[i]> 2000){


                }
            }
            else{



            }


        }


    }

    public void setWaveBandPass(Short[] waveBandPass){
        this.waveBandpass = waveBandPass;}
}
