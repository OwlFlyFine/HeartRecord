package com.example.l.trbear.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by L on 31-May-16.
 */
public class CSV {

    public void save(String fileName, List<String[]> data) {

        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(baseDir, "AudioRecorder");
        String filePath = file + "/" + fileName + ".csv";
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(filePath));
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> read(String fileName){
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(baseDir, "AudioRecorder");
        String filePath = file + "/" + fileName + ".csv";
        List<String[]> dataRead = new ArrayList<String[]>();
        try {
            CSVReader reader = new CSVReader(new FileReader(filePath));
            dataRead = reader.readAll();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataRead;
    }
}
