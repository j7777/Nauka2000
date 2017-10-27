package com.nauka2000.nauka2000.Json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser {
    HttpURLConnection is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {}

    public JSONObject getJSONFromUrl(String url) throws IOException {

        try {
            URL url_con = new URL(url);
            is = (HttpURLConnection) url_con.openConnection();
            is.setReadTimeout(10000);

            is.setDefaultUseCaches(false);
            is.setUseCaches(false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }

            json = sb.toString();

            if (reader != null) {
                reader.close();
            }
            is.disconnect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jObj = new JSONObject(json);
        }
        catch (JSONException e) {
            jObj = null;
        }

        return jObj;
    }
}
