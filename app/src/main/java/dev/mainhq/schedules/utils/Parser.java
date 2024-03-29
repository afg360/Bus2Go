package dev.mainhq.schedules.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlinx.coroutines.CoroutineStart;

//right now only for the main activities
//todo may use db operations instead
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
    public static ArrayList<RouteData> readBusTripsFromAssets(@NonNull InputStream inputStream, @NonNull String busNum){
        //separate the input stream in smaller chunks
        int numThreads = Runtime.getRuntime().availableProcessors();

        try{
            Pattern busNumPattern = Pattern.compile("^" + busNum + ",");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            //rough average of lines per buses
            //to be more precise, we could check that if bus num is express, check for ~100s,
            //if not, then check for thousands
            //original capacity: 1233
            ArrayList<RouteData> arr = new ArrayList<>(50);
            String line = bufferedReader.readLine();
            while (line != null){
                Matcher matcher = busNumPattern.matcher(line);
                if (matcher.find()) {
                    //parse the line to get all the tokens
                    String[] cols = line.split(",");
                    RouteData dataToAdd;
                    if (cols[6].equals("1")) dataToAdd = new RouteData(Short.parseShort(cols[0]), cols[1], cols[2],
                                cols[3], true);
                    else dataToAdd = new RouteData(Short.parseShort(cols[0]), cols[1],
                            cols[2], cols[3], false);
                    arr.add(dataToAdd);
                }
                line = bufferedReader.readLine();
            }
            //Log.d("DATA FOUND", arr.toString());
            return arr;
        }
        catch (IOException e){
            //todo
            e.printStackTrace();
        }
        return null;
    }

    public static Hashtable<String[], String> readBusRoutesFromAssets(InputStream inputStream) {
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
        str = str.toLowerCase()
                .replace("'", "")
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
    public static ArrayList<String[]> setupBusRoutes(AppCompatActivity activity, DataType dataType, String query){
        String filepath;
        if (Objects.requireNonNull(dataType) == DataType.busList) {
            filepath = "stm_data_info/routes.txt";
            InputStream inputStream = Parser.makeInputStream(activity, filepath);
            Hashtable<String[], String> busInfo = Parser.readBusRoutesFromAssets(inputStream);
            if (query != null) {
                QueryType type = getType(query);
                Set<String[]> busInfoKeys = busInfo.keySet();
                ArrayList<String[]> list = new ArrayList<>(0);
                switch (type) {
                    //if query is in first part of the actual name\
                    //also consider!
                    //use database operations instead
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
                            if (arr[1].toLowerCase().contains(query.toLowerCase())) {
                                //Log.d("Query substring", Objects.requireNonNull(busInfo.get(arr))
                                  //      + "\nBus: " + arr[0]);
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
