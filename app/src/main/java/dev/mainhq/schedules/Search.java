package dev.mainhq.schedules;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.String;
import java.lang.Character;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Search extends AppCompatActivity {
    String query;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.query = extras.getString("query");
            if (this.query != null){
                Log.i("Query submitted to other activity", this.query);
                //parse/manipulate the query
                QueryType type = getType(this.query);
                String routesFile = "stm_data_info/routes.txt";
                ArrayList<String[]> busInfo = readTextFileFromAssets(routesFile);
                Log.d("File content", busInfo.toString());
                switch(type){
                    //if query is in first part of the actual name\
                    //also consider!
                    case NUM_ONLY:
                        boolean found = false;
                        for (String[] arr : busInfo){
                            if (arr[0].equals(this.query)) {
                                Log.i("FOUND VAL", arr[1]);
                                found = true;
                            }
                            if (found) break;
                        }
                        if (!found)
                            Log.e("Mistype", "coudlnt find res for query");
                        break;
                    case ALPHA_ONLY:
                        break;
                    case MIXED:
                        break;
                    case UNKNOWN:
                        break;
                    default:
                        //throw an exception
                        break;
                }
            }
            else
                Log.e("Error", "An error occured retrieving query");
        }
        else
            Log.i("Query", "None");
    }

    private ArrayList<String[]> readTextFileFromAssets(String fileName) {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            InputStream inputStream = this.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            //should skip first 5 lines
            while (line != null) {
                //could color depending on express, night or normal
                String[] vals = line.split(",");
                String num = vals[0];
                String name = vals[3];
                name = name.replace("'", "");
                name = name.replace("-", "");
                name = name.replace(" ", "");
                name = name.replace("é", "e");
                name = name.replace("è", "e");
                name = name.replace("ê", "e");
                name = name.replace("ç", "c");
                name = name.replace("î", "i");
                name = name.replace("ô", "o");
                name = name.replace("û", "u");
                list.add(new String[] {num, name});
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

    private QueryType getType(String str){
        //process str first
        str = str.replace("'", "");
        str = str.replace("-", "");
        str = str.replace(" ", "");
        str = str.replace("é", "e");
        str = str.replace("è", "e");
        str = str.replace("ê", "e");
        str = str.replace("ç", "c");
        str = str.replace("î", "i");
        str = str.replace("ô", "o");
        str = str.replace("û", "u");

        //we could also make like a dictionary that stores common mistakes
        //for typical typos when searching?
        boolean isDigitOnly = true;
        boolean isAlphaOnly = true;
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i)))
                isDigitOnly = false;
            else if (!Character.isAlphabetic(str.charAt(i)))
                isAlphaOnly = false;
        }
        return (isDigitOnly && isAlphaOnly) ? QueryType.UNKNOWN:
                        (isDigitOnly) ? QueryType.NUM_ONLY:
                                (isAlphaOnly) ? QueryType.ALPHA_ONLY:
                                        QueryType.MIXED;
    }
    private enum QueryType{
        NUM_ONLY,ALPHA_ONLY,MIXED,UNKNOWN
    }
}
