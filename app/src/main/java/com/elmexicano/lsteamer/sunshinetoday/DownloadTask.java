package com.elmexicano.lsteamer.sunshinetoday;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//Class that retrieves the source code.
class DownloadTask extends AsyncTask<String, Void, String[]> {


    @Override
    protected String[] doInBackground(String... strings) {

        // Declaring outside the try/catch to close them later
        HttpURLConnection urlCon = null;
        BufferedReader reader = null;


        // Will contain the raw JSON response as a string.
        String [] weatherJSONStr = new String[2];

        String format = "json";
        String appid = "e646b9ad2e82a2f2b6afcf8741f70f96";


        try {

            // Construct the URL for the OpenWeatherMap query

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
            final String LATITUDE_PARAM = "lat";
            final String LONGITUDE_PARAM = "lon";
            final String POSTAL_CODE = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";
            Uri UriU;
            Uri UriW;


            if(strings.length==4){

                UriU = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LATITUDE_PARAM, strings[0])
                        .appendQueryParameter(LONGITUDE_PARAM, strings[1])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, strings[2])
                        .appendQueryParameter(DAYS_PARAM,  strings[3])
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

                UriW = Uri.parse(WEATHER_BASE_URL).buildUpon()
                        .appendQueryParameter(LATITUDE_PARAM, strings[0])
                        .appendQueryParameter(LONGITUDE_PARAM, strings[1])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, strings[2])
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

            }
            else{

                UriU = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(POSTAL_CODE, strings[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, strings[1])
                        .appendQueryParameter(DAYS_PARAM,  strings[2])
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

                UriW = Uri.parse(WEATHER_BASE_URL).buildUpon()
                        .appendQueryParameter(POSTAL_CODE, strings[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, strings[2])
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

            }
            URL [] url = new URL[2];
            //Read the URL
            url[0] = new URL(UriW.toString());
            url[1] = new URL(UriU.toString());

            for(int i=0; i<2; i++ ) {
                //Accessing the URL
                urlCon = (HttpURLConnection) url[i].openConnection();
                urlCon.setRequestMethod("GET");
                urlCon.connect();


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
                weatherJSONStr[i] = buffer.toString();

            }

            return weatherJSONStr;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
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


    }


}