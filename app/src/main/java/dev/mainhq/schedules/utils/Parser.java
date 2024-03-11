package dev.mainhq.schedules.utils;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;

//right now only for the main activities
public final class Parser {
    private Parser(){}
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
        str = str.replace("'", "")
                    .replace("-", "")
                    .replace(" ", "")
                    .replace("/", "")
                    .replace("é", "e")
                    .replace("è", "e")
                    .replace("ê", "e")
                    .replace("ç", "c")
                    .replace("î", "i")
                    .replace("ô", "o")
                    .replace("û", "u");
        return str;
    }
    @Nullable
    public static ArrayList<String[]> setup(AppCompatActivity activity, DataType dataType, String query){
        String filepath;
        if (Objects.requireNonNull(dataType) == DataType.busList) {
            filepath = "stm_data_info/routes.txt";
            InputStream inputStream = Parser.makeInputStream(activity, filepath);
            Hashtable<String[], String> busInfo = Parser.readTextFileFromAssets(inputStream);
            if (query != null) {
                QueryType type = getType(query);
                Set<String[]> busInfoKeys = busInfo.keySet();
                ArrayList<String[]> list = new ArrayList<>(0);
                switch (type) {
                    //if query is in first part of the actual name\
                    //also consider!
                    case NUM_ONLY:
                        boolean found = false;
                        for (String[] arr : busInfoKeys) {
                            if (arr[0].contains(query)) {
                                Log.d("FOUND VAL", Objects.requireNonNull(busInfo.get(arr))
                                        + "\nBus: " + arr[0]);
                                list.add(new String[]{arr[0], busInfo.get(arr)});
                                found = true;
                            }
                        }
                        if (!found) Log.e("Mistype", "coudlnt find res for query");
                        break;
                    case ALPHA_ONLY:
                        boolean pres = false;
                        for (String[] arr : busInfoKeys) {
                            //for now print all possible combinations
                            //must also work if mispell (using dicts?, regex?)
                            if (arr[1].contains(query)) {
                                Log.d("Query substring", Objects.requireNonNull(busInfo.get(arr))
                                        + "\nBus: " + arr[0]);
                                list.add(new String[]{arr[0], busInfo.get(arr)});
                                pres = true;
                            }
                        }
                        if (!pres) Log.d("Substring Error", "No substring found");
                        break;
                    case MIXED:
                        //TODO
                        break;
                    case UNKNOWN:
                        if (!query.equals("")) {
                            Log.e("UNKNOWN TYPE ERROR", "Cannot process that data...");
                            activity.finish();
                        }
                        else Log.d("Empty", "The string is empty. Do nothing");
                        break;
                    default:
                        //throw an exception
                        break;
                }
                return (list.size() < 1) ? null : list;
            } else {
                Log.e("Error", "An error occurred retrieving query");
                return null;
            }
        }
        return null;
    }
    private static QueryType getType(String str){
        //process str first
        str = Parser.toParsable(str);
        //we could also make like a dictionary that stores common mistakes
        //for typical typos when searching?
        boolean isDigitOnly = true;
        boolean isAlphaOnly = true;
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))) isDigitOnly = false;
            else if (!Character.isAlphabetic(str.charAt(i))) isAlphaOnly = false;
        }
        return (isDigitOnly && isAlphaOnly) ? QueryType.UNKNOWN:
                (isDigitOnly) ? QueryType.NUM_ONLY:
                        (isAlphaOnly) ? QueryType.ALPHA_ONLY:
                                QueryType.MIXED;
    }
    private enum QueryType{
        NUM_ONLY,ALPHA_ONLY,MIXED,UNKNOWN
    }
    public enum DataType{
        busList
    }
}
