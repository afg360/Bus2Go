package dev.mainhq.schedules.utils;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

//right now only for the main activities
public final class Parser {
    public static InputStream makeInputStream(AppCompatActivity activity, String filepath){
        try{
            return activity.getAssets().open(filepath);
        }
        catch (IOException i){
            Log.d("IO EXCEPTION", "ERROR OPENING FILE");
            activity.finish();
            return null;
        }
    }

    public static Hashtable<String[], String> readTextFileFromAssets(InputStream inputStream) {
        Hashtable<String[], String> list = new Hashtable<>();
        if (inputStream == null) return list;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            //should skip first 5 lines
            while (line != null) {
                //could color depending on express, night or normal
                String[] vals = line.split(",");
                String num = vals[0];
                String name = vals[3];
                name = toParsable(name);
                //map modified string to real string
                list.put(new String[] {num, name}, vals[3]);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            inputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String toParsable(String str){
        str = str.toLowerCase();
        str = str.replace("'", "");
        str = str.replace("-", "");
        str = str.replace(" ", "");
        str = str.replace("/", "");
        str = str.replace("é", "e");
        str = str.replace("è", "e");
        str = str.replace("ê", "e");
        str = str.replace("ç", "c");
        str = str.replace("î", "i");
        str = str.replace("ô", "o");
        str = str.replace("û", "u");
        return str;
    }
}
