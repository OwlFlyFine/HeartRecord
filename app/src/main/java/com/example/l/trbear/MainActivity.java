package com.example.l.trbear;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.l.trbear.util.BandPassFilter;
import com.example.l.trbear.util.CSV;
import com.example.l.trbear.util.CaptureWave3;
import com.example.l.trbear.util.FFT;
import com.example.l.trbear.util.FeatureVector;
import com.example.l.trbear.util.FeedForward;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.opencsv.CSVWriter;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // .wav
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    //Properties (MIC)
    private AudioTask audioTask;
    protected AudioRecord audioRecord;
    protected int buffersizebytes;
    protected short[] buffer; //+-32767
    protected int RECORDER_SAMPLERATE = 8000; //samp per sec 8000, 11025, 22050 44100 or 48000

    // timer
    private int min = 0, sec = 0;
    private TimerTask mTimerTask;
    private final Handler handler = new Handler();
    private Timer t = new Timer();

    // Chart
    private LineChart chart;
    private LineChart chart2;
    private boolean isSelectSuccess;
    private boolean isReadyCut;
    private boolean isViewChart;
    private Highlight[] tampHighlight = new Highlight[2];

    //view
    private TextView textMin, textSec;
    private EditText editTextFileName;

    //variable
    private ArrayList<String> keepData;
    private ArrayList<Short> wave;
    private ArrayList<Short> waveBandPass;
    private ArrayList<Short> waveCutedShort;
    private ArrayList<Short> waveCutedShortBandPass;
    private ArrayList<Short[]> waveAutoCut;
    private double[] waveMagnitude;
    private int[] xValuesOfMagnitude;
    private String typeOfHeartHisease = "N";
    private String positionHeart = "1";
    short[] wave2;
    List<String[]> dataWaveWrite;
    double[] tempDou;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initSpinner();
        initChart();
        textMin = (TextView) findViewById(R.id.textMin);
        textSec = (TextView) findViewById(R.id.textSec);
        editTextFileName = (EditText) findViewById(R.id.editText);

        setButtonHandlers();
        enableButtons(false);
        enableButton(R.id.btnSave, false);

        keepData = new ArrayList<>();
        wave = new ArrayList<>();
        waveBandPass = new ArrayList<>();
        waveCutedShort = new ArrayList<>();
        waveCutedShortBandPass = new ArrayList<>();
        waveAutoCut = new ArrayList<>();
        dataWaveWrite = new ArrayList<>();
        isSelectSuccess = true;
        isReadyCut = false;
        isViewChart = false;
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
        if (audioTask != null) {
            audioTask.cancel(true);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

/*    private void initSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setEnabled(true);
        final String[] dataSample = new String[]{"N", "AS", "AR", "MS", "MR", "PS", "PR", "TS", "TR", "ASD", "USD"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataSample);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                typeOfHeartHisease = dataSample[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner2.setEnabled(true);
        final String[] dataSample2 = new String[]{"1", "2", "3", "4"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataSample2);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                positionHeart = dataSample2[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }*/

    private void initChart() {
        // in this example, a LineChart is initialized from xml
        chart = (LineChart) findViewById(R.id.chart);
        chart.setTouchEnabled(false);
        // chart.setHighlightFullBarEnabled(true);
        chart.setScaleEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setScaleMinima(2f, 1f);      // set visible some line
        //chart.setScaleX(1f);
        chart.setDragEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setMaxVisibleValueCount(10);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setAxisMinimum(-32768);
        chart.getAxisLeft().setAxisMaximum(32768);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().disableGridDashedLine();
        chart.getAxisRight().disableGridDashedLine();
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (isSelectSuccess) {
                    tampHighlight[1] = h;
                    isSelectSuccess = false;
                } else {
                    // Highlight hh = new Highlight(e.getXIndex(), e.getVal(), h.getXIndex(), -1);
                    chart.highlightValues(new Highlight[]{tampHighlight[0], h});
                    tampHighlight[1] = tampHighlight[0];
                    tampHighlight[0] = h;

                    Log.i("chartLog", "hh : " + h);
                    Log.i("chartLog", "Data : " + tampHighlight[0].getDataSetIndex());
                    isReadyCut = true;

                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
 /*       chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                if (isSelectSuccess) {
                    tampHighlight[0] = h;
                    isSelectSuccess = false;
                } else {

                   // Highlight hh = new Highlight(e.getXIndex(), e.getVal(), h.getXIndex(), -1);
                    chart.highlightValues(new Highlight[]{tampHighlight[0], h});
                    tampHighlight[1] = tampHighlight[0];
                    tampHighlight[0] = h;
                    Log.i("chartLog", "hh : " + h);
                    isReadyCut = true;
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });*/

        chart2 = (LineChart) findViewById(R.id.chart2);
        chart2.setDrawGridBackground(false);

        // no description text
        chart2.getDescription().setEnabled(false);

        // enable touch gestures
        chart2.setTouchEnabled(true);

        // enable scaling and dragging
        chart2.setDragEnabled(true);
        chart2.setScaleEnabled(true);


        // if disabled, scaling can be done on x- and y-axis separately
        chart2.setPinchZoom(false);


        chart2.getAxisRight().setEnabled(false);


        // dont forget to refresh the drawing
        chart2.invalidate();

    }

    private void applyChart() {
        Short[] tampWave = new Short[wave.size()];
        for (int i = 0; i < tampWave.length; i++) {
            tampWave[i] = wave.get(i);
        }

        BandPassFilter lowPassFilter = new BandPassFilter(RECORDER_SAMPLERATE, 1, 20, 660, 1);
        Short[] samplesShort = lowPassFilter.applyFilter(tampWave);
        waveBandPass.clear();
        for (int i = 0; i < samplesShort.length; i++) {
            waveBandPass.add(samplesShort[i]);
        }

        Short[] tampWaveBandPass = new Short[waveBandPass.size()];
        ArrayList<Entry> yVals_wave = new ArrayList<Entry>();
        for (int i = 0; i < tampWaveBandPass.length; i++) {
            yVals_wave.add(new Entry(i, (float) waveBandPass.get(i)));


        }


        LineDataSet setComp1 = new LineDataSet(yVals_wave, "Heart wave");
        setComp1.setColor(Color.RED);
        setComp1.setDrawCircles(false);
        setComp1.setDrawValues(false);
        setComp1.setHighLightColor(Color.BLUE);
        setComp1.setHighlightEnabled(true);
        setComp1.setDrawHorizontalHighlightIndicator(false);
        // use the interface ILineDataSet
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        // set data line chart
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate(); // refresh

        isSelectSuccess = true;
        chart.setTouchEnabled(true);
    }

    private void applyChart2() {


        Short[] tampWaveBandPass = new Short[waveCutedShort.size()];
        ArrayList<Entry> yVals_wave2 = new ArrayList<Entry>();
        for (int i = 0; i < tampWaveBandPass.length; i++) {
            yVals_wave2.add(new Entry(i, (float) waveCutedShort.get(i)));


        }

  /*      chart2.getAxisLeft().setAxisMinValue(-32768);
        chart2.getAxisLeft().setAxisMaxValue(32768);*/
        LineDataSet setComp1 = new LineDataSet(yVals_wave2, "Heart wave cut");
        setComp1.setColor(Color.BLUE);
        setComp1.setDrawCircles(false);
        setComp1.setDrawValues(false);
        setComp1.setHighlightEnabled(false);
        // use the interface ILineDataSet
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        // set data line chart
        LineData data = new LineData(dataSets);
        chart2.setData(data);
        chart2.invalidate(); // refresh
    }

    private void applyChart3() {
     /*   ArrayList<Entry> valComp = new ArrayList<Entry>();
        for (int i = 0; i < waveMagnitude.length; i++) {
            float val = (float) waveMagnitude[i];
            Entry c1e1 = new Entry(val, i);
            valComp.add(c1e1);
         //   Log.i("LogChart", "val : " + val);
        }*/
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Entry> valComp = new ArrayList<Entry>();
        Short[] tempWave = new Short[waveCutedShortBandPass.size()];
        int size = tempWave.length;
        double[] temp_data = new double[size * 2];

        temp_data[1] = size / 2;
        for (int i = 0; i < size; i++) {

            //real[i] = (double) data[i] / 32768.0; // signed 16 bit
            //image[i] = 0.0;

            temp_data[2 * i] = (double) waveCutedShortBandPass.get(i) / 32768.0;
            temp_data[2 * i + 1] = 0;


        }
        DoubleFFT_1D fft = new DoubleFFT_1D(size);
        fft.complexForward(temp_data);
        int new_size = size;
        short[] data_temp = new short[new_size];
        short max_amp = 0;
        int num = 0;
        for (int i = 0; i < new_size; i += 2) {

            data_temp[i] = (short) (Math.sqrt(temp_data[i] * temp_data[i] + temp_data[i + 1] * temp_data[i + 1]) * 100);
            //yVals.add(new Entry(i * 0.001f, (float) data_temp[i]));
            yVals.add(new Entry(i * 0.001f, (float) data_temp[i]));

            if (max_amp < data_temp[i]) {
                max_amp = data_temp[i];
                num = i;
            }
        }
        LineDataSet setComp1 = new LineDataSet(yVals, "Heart wave cut");
        setComp1.setColor(Color.BLUE);
        setComp1.setDrawCircles(false);
        setComp1.setDrawValues(true);
        setComp1.setHighlightEnabled(false);
        // use the interface ILineDataSet
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        // set data line chart
        LineData data = new LineData(dataSets);
        chart2.setData(data);
        chart2.invalidate(); // refresh
    }

    private ArrayList<String> getXValue(int size) {
        //    Log.i("LogChart", "size : " + size);
        //   Log.i("LogChart", "size/ : " + size/10);
        ArrayList<String> xVal = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            xVal.add("");
        }
        return xVal;
    }

    private ArrayList<String> getXValues(int size) {
        ArrayList<String> xVal = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            xVal.add(String.valueOf(xValuesOfMagnitude[i]));
        }
        return xVal;
    }

    private ArrayList<Entry> generateData(ArrayList<Short> w) {
        ArrayList<Entry> valComp1 = new ArrayList<Entry>();
        for (int i = 0; i < w.size(); i++) {
            float val = w.get(i).floatValue();//327.68f;
            Entry c1e1 = new Entry(val, i);
            valComp1.add(c1e1);
            //     Log.i("LogChart", "val : " + val);
        }
        return valComp1;
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnSave)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnCut)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnChangeView)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
                case R.id.btnSave: {
                    enableButton(R.id.btnSave, false);
                    saveFile();
                    break;
                }
                case R.id.btnCut: {
                    enableButton(R.id.btnSave, true);
                    buttonCutWave();
                    break;
                }
                case R.id.btnChangeView: {
                    changeDataChart();
                    break;
                }
            }
        }
    };

    private void startRecording() {
        LineData set = chart.getData();
        if (set != null) {
            chart.clearValues();
            chart2.clearValues();
        }
        isReadyCut = false;
        setTextTime();
        chart.setTouchEnabled(false);
        wave.clear();
        keepData.clear();
        // run task
        audioTask = new AudioTask();
        audioTask.execute(this);
        // doTimerTask();
        Toast.makeText(this, "Start recording..", Toast.LENGTH_LONG).show();
    }

    private void stopRecording() {


        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (audioTask != null) {
            audioTask.cancel(true);
        }
        applyChart();
    }

    private void buttonCutWave() {
        if (isReadyCut) {
            // cut and check length data
            if (cutWave2()) {
                FFT fftWaveCutedShortBandPass = new FFT(waveCutedShortBandPass);
                FeatureVector featureVector2 = new FeatureVector();
                featureVector2.setSpectrum(fftWaveCutedShortBandPass.getSpectrum());
                featureVector2.findFeatureVectorAvg2(Integer.valueOf(positionHeart)); //feature vector


                //   double[] data1 = new double[]{0.003827602, 0.006287645, 0.043565358, 0.242179607, 0.029108419, 0.103627221, 0.09522751, 0.144695099, 0.14628326, 0.085519821, 0.235048008, 0.496087111, 0.200423609, 0.151246718, 0.075483062};
                //   double[] data2 = new double[]{0.000604074, 0.043655254, 0.228221078, 0.026367583, 0.020691966, 0.007628462, 0.005229935, 0.018588777, 0.017543253, 0.013890355, 0.007776723, 0.053176396, 0.042134997, 0.065470193, 0.064608894};
                //   double[] data3 = new double[]{0.002982035, 0.079900446, 0.19305466, 0.072079009, 0.037927447, 0.024916451, 0.071124492, 0.018548009, 0.047714851, 0.030160255, 0.003671876, 0.009977229, 0.008581745, 0.008367802, 0.00722878};
                //   double[] data4 = new double[]{0.001017908, 0.050678655, 0.134268897, 0.035498205, 0.008612505, 0.006579735, 0.004729655, 0.011637093, 0.017687283, 0.034506442, 0.002431917, 0.006659027, 0.00540373, 0.005001755, 0.003018295};
                //   double[] data5 = new double[]{0.100000000000000, 0.00289108600000000, 0.00577381900000000,0.0863968210000000,0.0490512570000000, 0.0529441840000000, 0.121829179000000, 0.0598340320000000, 0.175522808000000, 0.286309047000000, 0.512937769000000, 0.0782132340000000, 0.292643859000000, 0.0750746360000000, 0.118033072000000, 0.0706012870000000, 0.0347462090000000, 0.0698293950000000, 0.0601506260000000, 0.0904705290000000, 0.0876000770000000, 0.00685482700000000, 0.0746820960000000, 0.0408484210000000, 0.0229458400000000, 0.0535629680000000, 0.0387812860000000, 0.0153622470000000, 0.0152784400000000, 0.0234922740000000, 0.0123146120000000, 0.0262485450000000};



                /*double[] featureVector = new double[32];
                featureVector[0] = Double.valueOf(positionHeart)/10.0;*/

                double[] featureVector = featureVector2.getFeatureVector();
                for (int i = 1; i < featureVector.length; i++) {
                    //   featureVector[i] = featureVector2.getFeatureVector()[i];
                    featureVector[i] = featureVector[i] / 4000000;
                    featureVector[i] = Math.round(featureVector[i] * 10000.0) / 10000.0;
                    if (featureVector[i] >= 1) {  // check value more then 1
                        featureVector[i] = 0.9999;
                    }
                    Log.i("feature", i + " val " + featureVector[i]);
                }
               /* for(int i = 0; i < featureVector.length; i++){
                    featureVector[i] = featureVector[i]/20000000;
                    featureVector[i] = Math.round(featureVector[i]*10000.0)/10000.0;
                }*/
                FeedForward feedForward = new FeedForward();
                feedForward.setInput(featureVector);
                feedForward.calculate();
                double result = Math.round(feedForward.getOutput() * 10000.0) / 10000.0;
                Log.i("Result", "result : " + result);
                if (result == 0.166) {
                    Toast.makeText(this, "AR " + String.valueOf(result), Toast.LENGTH_LONG).show();
                } else if (result == 0.332) {
                    Toast.makeText(this, "ASD " + String.valueOf(result), Toast.LENGTH_LONG).show();
                } else if (result == 0.498) {
                    Toast.makeText(this, "TR " + String.valueOf(result), Toast.LENGTH_LONG).show();
                } else if (result == 0.664) {
                    Toast.makeText(this, "MR " + String.valueOf(result), Toast.LENGTH_LONG).show();
                } else if (result == 0.83) {
                    Toast.makeText(this, "MS " + String.valueOf(result), Toast.LENGTH_LONG).show();
                } else if (result == 0.996) {
                    Toast.makeText(this, "N " + String.valueOf(result), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, String.valueOf(result), Toast.LENGTH_LONG).show();
                }
                Log.i("FeedForward", "Output : " + result);
                Toast.makeText(this, getString(R.string.cut_wave), Toast.LENGTH_LONG).show();
            }

        }

    }

    private void saveFile() {
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(baseDir, "AudioRecorder");
        String filePath = file + "/" + editTextFileName.getText().toString() + ".csv";


        CSVWriter writer = null;
        try {
            Log.d("check", "write");
            writer = new CSVWriter(new FileWriter(filePath));
            writer.writeAll(dataWaveWrite);
            writer.close();
            Toast.makeText(this, "บันทึกไฟล์เสร็จเรียบร้อย", Toast.LENGTH_SHORT).show();
            editTextFileName.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveRecord() {
        setTextTime();
        List<String[]> waveFromCSV; // tamp data read
        String[] data;
        CSV csv = new CSV();
        // save wave not filter
      /*  waveFromCSV = csv.read("Wave");
        data = convertArrayDoubleToArrayStringForWriteCSV(String.valueOf(editTextFileName.getText()), typeOfHeartHisease, positionHeart, convertArrayListShortToArrayDouble(waveCutedShort));
        // add new row of csv
        waveFromCSV.add(data);
        csv.save("Wave", waveFromCSV);*/

        // save wave filter
        //waveFromCSV.clear();
        waveFromCSV = csv.read("WaveFilter");
        data = convertArrayDoubleToArrayStringForWriteCSV(String.valueOf(editTextFileName.getText()), typeOfHeartHisease, positionHeart, convertArrayListShortToArrayDouble(waveCutedShortBandPass));
        // add new row of csv
        waveFromCSV.add(data);
        csv.save("WaveFilter", waveFromCSV);

        // save featureVector of spectrum not filter
       /* waveFromCSV.clear();
        waveFromCSV = csv.read("FeatureVector");
        FFT fftWaveCutedShort = new FFT(waveCutedShort);
        FeatureVector featureVector = new FeatureVector();
        featureVector.setSpectrum(fftWaveCutedShort.getSpectrum());
        featureVector.findFeatureVector(18, 16, 16); // 40 Hz - 667 Hz 16 is number of feature vector
        data = convertArrayDoubleToArrayStringForWriteCSV(String.valueOf(editTextFileName.getText()), typeOfHeartHisease, positionHeart, featureVector.getFeatureVector());
        waveFromCSV.add(data);
        csv.save("FeatureVector", waveFromCSV);*/

        // save featureVector of spectrum not filter
        waveFromCSV.clear();
        waveFromCSV = csv.read("FeatureVectorFilter");
        FFT fftWaveCutedShortBandPass = new FFT(waveCutedShortBandPass);
        FeatureVector featureVector2 = new FeatureVector();
        featureVector2.setSpectrum(fftWaveCutedShortBandPass.getSpectrum());
        featureVector2.findFeatureVectorAvg2(Integer.valueOf(positionHeart)); //feature vector
        double[] featureVector = featureVector2.getFeatureVector();
        for (int i = 1; i < featureVector.length; i++) {
            featureVector[i] = featureVector[i] / 4000000;
            featureVector[i] = Math.round(featureVector[i] * 10000.0) / 10000.0;
            if (featureVector[i] >= 1) {  // check value more then 1
                featureVector[i] = 0.9999;
            }
            Log.i("feature", i + " val " + featureVector[i]);
        }

        data = convertArrayDoubleToArrayStringForWriteCSV(String.valueOf(editTextFileName.getText()), typeOfHeartHisease, String.valueOf(Double.valueOf(positionHeart) / 10.0), featureVector);
        waveFromCSV.add(data);
        csv.save("FeatureVectorFilter", waveFromCSV);

        Toast.makeText(this, getString(R.string.save_file), Toast.LENGTH_LONG).show();
    }

    private double[] convertArrayListShortToArrayDouble(ArrayList<Short> data) {
        double[] tamp = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            tamp[i] = data.get(i).doubleValue();
        }
        return tamp;
    }

    private String[] convertArrayDoubleToArrayStringForWriteCSV(String name, String type, String position, double[] value) {
        String[] tamp = new String[value.length + 2];
        tamp[0] = name;
        tamp[1] = type;
        // tamp[2] = position;
        for (int i = 2; i < tamp.length; i++) {
            tamp[i] = String.valueOf(value[i - 2]);
        }
        return tamp;
    }


    private String[] convertArrayDoubleToArrayStringForWriteCSV2(double[] value) {
        String[] tamp = new String[value.length];

        // tamp[2] = position;
        for (int i = 2; i < tamp.length; i++) {
            tamp[i] = String.valueOf(value[i]);
        }
        return tamp;
    }

    private void changeDataChart() {
        if (isReadyCut && waveMagnitude.length != 0 && !waveCutedShort.isEmpty()) {
            if (isViewChart) {
                applyChart2();
                isViewChart = false;
            } else {
                applyChart3();
                isViewChart = true;
            }
        }
    }

   /* private boolean cutWave(){
        waveCutedShort.clear();
        waveCutedShortBandPass.clear();
        Log.i("wave", "cut index : " + tampHighlight[0].getX() + " - " + tampHighlight[1].getX());
        if(tampHighlight[0].getX() < tampHighlight[1].getX()){
            for(int i = (int) tampHighlight[0].getX(); i <= tampHighlight[1].getX(); i++){
                waveCutedShort.add(wave.get(i));
                waveCutedShortBandPass.add(waveBandPass.get(i));
            }
        }
        else {
            for(int i = (int) tampHighlight[1].getX(); i <= tampHighlight[0].getX(); i++){
                waveCutedShort.add(wave.get(i));
                waveCutedShortBandPass.add(waveBandPass.get(i));
            }
        }
        if(waveCutedShort.size() >= 1000 && waveCutedShort.size() < 56000){
            applyChart2();
            fftArrayListShort();
            //testFFT();
            isViewChart = false;
        }
        else{
            Log.d("CheckData", "Size WaveCut :" + waveCutedShort.size());
            Toast.makeText(this, getString(R.string.fail_cut_wave), Toast.LENGTH_LONG).show();
            return false;
        }

         //Auto cut wave
        Short[] waveData = {}; // arrayListShortArrayToArray.getShorts();
        waveData = wave.toArray(waveData);
        CaptureWave3 captureWave3 = new CaptureWave3();
        captureWave3.setWave(waveData);
        //   captureWave3.setWaveBandpass(waveData);
        captureWave3.setWaveBandpass(waveData);
        captureWave3.process();
        waveAutoCut.clear();
        waveAutoCut = captureWave3.getDataList();
        convertListArrayShortToArrayStringAndSave(waveAutoCut);*//*

        return true;
    }*/


    private boolean cutWave2() {
        waveCutedShort.clear();
        waveCutedShortBandPass.clear();
        int h2 = (int) tampHighlight[0].getX();
        h2 = h2 + 5000;
        String tempData;
        Log.i("wave", "cut index : " + tampHighlight[0].getX() + " - " + tampHighlight[1].getX());
        if (tampHighlight[0].getX() < tampHighlight[1].getX()) {
            for (int i = (int) tampHighlight[0].getX(); i <= tampHighlight[1].getX(); i++) {
                waveCutedShort.add(wave.get(i));
                waveCutedShortBandPass.add(waveBandPass.get(i));
                tempData = Short.toString(waveBandPass.get(i));
                dataWaveWrite.add(new String[]{tempData});
            }
        } else {
            for (int i = (int) tampHighlight[1].getX(); i <= tampHighlight[0].getX(); i++) {
                waveCutedShort.add(wave.get(i));
                waveCutedShortBandPass.add(waveBandPass.get(i));
                tempData = Short.toString(waveBandPass.get(i));
                dataWaveWrite.add(new String[]{tempData});
            }
        }
        if (waveCutedShort.size() >= 1000 && waveCutedShort.size() < 56000) {

            applyChart2();

            //saveFile();
//            String[] a1 = new  String[waveCutedShortBandPass.size()];
//            tempDou = convertArrayListShortToArrayDouble(waveCutedShortBandPass);
//            a1 = convertArrayDoubleToArrayStringForWriteCSV2(tempDou);
//            saveAudioCut(a1);
            fftArrayListShort();
            //testFFT();
            isViewChart = false;
        } else {
            Log.d("CheckData", "Size WaveCut :" + waveCutedShort.size());
            Toast.makeText(this, getString(R.string.fail_cut_wave), Toast.LENGTH_LONG).show();
            return false;
        }







       /* // Auto cut wave
        Short[] waveData = {}; // arrayListShortArrayToArray.getShorts();
        waveData = wave.toArray(waveData);
        CaptureWave3 captureWave3 = new CaptureWave3();
        captureWave3.setWave(waveData);
         captureWave3.setWaveBandpass(waveData);
        captureWave3.setWaveBandpass(waveData);
        captureWave3.process();
        waveAutoCut.clear();
        waveAutoCut = captureWave3.getDataList();
        convertListArrayShortToArrayStringAndSave(waveAutoCut);*/

        return true;
    }


/*
    private void saveFile(){

        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(baseDir, "AudioRecorder");
        String filePath = file + "/" + "test" + ".csv";
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(filePath));

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i=0; i<20;i++){

            writer.writeNext(new String[waveCutedShortBandPass.get(i)]);
        }

        try {
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private  void saveAudioCut(ArrayList<Short[]> waveBand){

        CSV csv = new CSV();
        List<String[]> waveFromCSV = new ArrayList<>(); // tamp data
        Log.i("Wave auto", "List" + waveBand);
        for(int i = 0; i < waveBand.size(); i++){
            String[] result = new String[waveBand.get(i).length];
            for(int j = 0; j < waveBand.get(i).length; j++){
                result[j] = (waveBand.get(i))[j].toString();
            }
            waveFromCSV.add(result);
        }
        //save
        csv.save("WaveAutoCut", waveFromCSV);



    }
*/


    private void convertListArrayShortToArrayStringAndSave(ArrayList<Short[]> wave) {
        CSV csv = new CSV();
        List<String[]> waveFromCSV = new ArrayList<>(); // tamp data
        Log.i("Wave auto", "List" + wave);
        for (int i = 0; i < wave.size(); i++) {
            String[] result = new String[wave.get(i).length];
            for (int j = 0; j < wave.get(i).length; j++) {
                result[j] = (wave.get(i))[j].toString();
            }
            waveFromCSV.add(result);
        }
        //save
        csv.save("WaveAutoCut", waveFromCSV);
    }

    private void fftArrayListShort() {
        FFT fft = new FFT(waveCutedShort);
        waveMagnitude = fft.getSpectrum();
        xValuesOfMagnitude = new int[waveMagnitude.length];
        for (int i = 0; i < waveMagnitude.length; i++) {
            //   Log.i("frequency","frequency " + i + " - " + fft.getFrequencyFromSpectrum(i) + " : " + fft.getCumulativeFrequency(i));
            xValuesOfMagnitude[i] = (int) fft.getFrequencyFromSpectrum(i);
        }

/*
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        Short[] y =new Short[waveCutedShort.size()];
        int size = y.length;
        double[] temp_data = new double[size*2];
        temp_data[1] = size/2;

        for (int i = 0; i < size; i++) {

            //real[i] = (double) data[i] / 32768.0; // signed 16 bit
            //image[i] = 0.0;

            temp_data[2*i] = (double) waveCutedShort.get(i) / 32768.0;
            temp_data[2*i+1] = 0;


        }

        DoubleFFT_1D fft = new DoubleFFT_1D(size);
        fft.complexForward(temp_data);
        int new_size = size ;
        short[] data_temp = new short[new_size];
        short max_amp = 0;
        int num = 0;
        for(int i = 0 ; i < new_size ;  i += 2)
        {

            data_temp[i] = (short)  (Math.sqrt(temp_data[i] * temp_data[i] + temp_data[i + 1] * temp_data[i + 1])*100);
            yVals.add(new Entry(i * 0.001f, (int) data_temp[i]));
            if(max_amp < data_temp[i])
            {
                max_amp = data_temp[i];
                num = i;
            }
        }
*/


    }


    // Sub class
    protected class AudioTask extends AsyncTask<Context, Integer, String> {
        protected int mSamplesRead; //how many samples read
        protected int mSamplesRead2; //how many samples read
        protected int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        protected int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        private FileOutputStream os = null;
        private int countDataNextRow;
        private int countAllData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            buffersizebytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, channelConfiguration, audioEncoding); //4096 on ion
            buffer = new short[buffersizebytes];
            countDataNextRow = 0;
            countAllData = 0;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, channelConfiguration, audioEncoding, buffersizebytes * 2); //constructor
            doTimerTask();
        }

        @Override
        protected String doInBackground(Context... params) {
            audioRecord.startRecording();
            wave2 = new short[buffersizebytes];

            while (true) {
                if (isCancelled()) {
                    return "stopRecord";
                }
                try {
                    // Read 16 bit
                    mSamplesRead = audioRecord.read(buffer, 0, buffersizebytes); //buffer
                    mSamplesRead2 = audioRecord.read(wave2, 0, buffersizebytes); //buffer

                    // write .csv and show graph
                    if (AudioRecord.ERROR_INVALID_OPERATION != mSamplesRead) {
                        //arrayListShort.add(buffer);
                        //  Log.i("task", "buffer length : " + buffer.length);
                        for (int i = 0; i < buffersizebytes - 1; i++) {
                            // get data for app

                            wave.add(buffer[i]);
                            //Log.i("Data","buffer[i] : "   + buffer[i]);
                            // check data full
                            if (countAllData < 580000) {
                                // keep data for csv
                                keepData.add(String.valueOf(buffer[i]));
                                // new row csv
                                if (countDataNextRow == 16000) {
                                    keepData.add("\n");
                                    countDataNextRow = 0;
                                }
                                countDataNextRow++;
                            }
                            countAllData++;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (null != audioRecord) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            stopTask();
        }

        public void doTimerTask() {
            mTimerTask = new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            sec++;
                            if (sec == 60) {
                                min++;
                                textMin.setText(String.valueOf(min));
                                sec = 0;
                            }
                            if (sec < 10) {
                                textSec.setText("0" + String.valueOf(sec));
                            } else {
                                textSec.setText(String.valueOf(sec));
                            }
                        }
                    });
                }
            };

            // public void schedule (TimerTask task, long delay, long period)
            t.schedule(mTimerTask, 0, 1000);  //
        }

        public void stopTask() {
            if (mTimerTask != null) {
                mTimerTask.cancel();
            }
        }
    }



    private void setTextTime() {
        textMin.setText("0");
        textSec.setText("00");
        min = 0;
        sec = 0;
    }
}
