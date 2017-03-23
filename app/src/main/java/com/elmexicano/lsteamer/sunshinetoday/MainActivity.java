package com.elmexicano.lsteamer.sunshinetoday;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;



    //Inner class that retrieves the source code.
    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            // Declaring outside the try/catch to close them later
            HttpURLConnection urlCon = null;
            BufferedReader reader = null;


            // Will contain the raw JSON response as a string.
            String weatherJSONStr = null;

            String format = "json";
            String units = "metric";
            String appid = "e646b9ad2e82a2f2b6afcf8741f70f96";
            int numDays = 7;




            try {

                // Construct the URL for the OpenWeatherMap query

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String LATITUDE_PARAM = "lat";
                final String LONGITUDE_PARAM = "lot";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri UriU = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LATITUDE_PARAM, strings[0])
                        .appendQueryParameter(LONGITUDE_PARAM, strings[1])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

                //Read the URL
                URL url = new URL(UriU.toString());



                //Accessing the URL
                urlCon = (HttpURLConnection) url.openConnection();
                urlCon.setRequestMethod("GET");
                urlCon.connect();


                Log.i("Day Forecast","Nope4");

                // Read the input stream into a String
                InputStream inputStream = urlCon.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    weatherJSONStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Adding a newline as buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                weatherJSONStr = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "ERROR";
            } catch (IOException e){
                e.printStackTrace();
                return "ERROR";
            }finally {
                if (urlCon != null) {
                    urlCon.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            return weatherJSONStr;

        }




        private String formatHighLows(double high, double low){
            // For presentation, assume the user only cares about full numbers
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject weatherJSONObj = new JSONObject(result);
                JSONArray weatherJSONArray = weatherJSONObj.getJSONArray("list");


                for (int i = 0; i < weatherJSONArray.length(); i++){

                    String mainTemperature;
                    String description;
                    String highAndLow;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = weatherJSONArray.getJSONObject(i);



                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray("weather").getJSONObject(0);
                    mainTemperature = weatherObject.getString("main");
                    description = weatherObject.getString("description");

                    JSONObject temperatureObject = dayForecast.getJSONObject("temp");

                    double high = temperatureObject.getDouble("max");
                    double low = temperatureObject.getDouble("min");

                    highAndLow = formatHighLows(high, low);

                    Log.i("Day Forecast",mainTemperature+" - "+ highAndLow + " - "+description);

                }




            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //String that catches the result
        String  result="";

        //AsyncTask Class
        DownloadTask task = new DownloadTask();


        try {
            result = task.execute("52.5200","13.4050").get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



    }
}




