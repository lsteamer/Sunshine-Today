package com.elmexicano.lsteamer.sunshinetoday;

import android.media.Image;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;


public class Tab1Today extends Fragment{


    protected TextView textTodayDate;
    protected TextView textDegrees;
    protected TextView textMainWeather;
    protected TextView textDescriptionWeather;
    protected TextView textHighLow;
    protected ImageView imageWeather;



    public void populateScreen(String... strings){

        //Declaring the List
        textTodayDate = (TextView) getActivity().findViewById(R.id.weatherdatelocation);
        textDegrees = (TextView) getActivity().findViewById(R.id.textdegrees);
        textMainWeather = (TextView) getActivity().findViewById(R.id.mainweather);
        textDescriptionWeather = (TextView) getActivity().findViewById(R.id.mainweatherdescription);
        textHighLow = (TextView) getActivity().findViewById(R.id.highlow);
        imageWeather = (ImageView) getActivity().findViewById(R.id.imageweather);
        imageWeather.setImageResource(getWeatherImage(Integer.valueOf(strings[5])));
        textTodayDate.setText(strings[0]);
        textDegrees.setText(strings[1]);
        textMainWeather.setText(strings[2]);
        textDescriptionWeather.setText(strings[3]);
        textHighLow.setText(strings[4]);

    }
    public static int getWeatherImage(int weatherId) {

        //Showing different icons if it's nighttime
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (weatherId >= 200 && weatherId <= 232)
            return R.drawable.storm;
        else if (weatherId >= 300 && weatherId <= 321)
            return R.drawable.rain;
        else if (weatherId >= 500 && weatherId <= 504)
            return R.drawable.rain_heavy;
        else if (weatherId == 511)
            return R.drawable.snow;
        else if (weatherId >= 520 && weatherId <= 531)
            return R.drawable.rain;
        else if (weatherId >= 600 && weatherId <= 622)
            return R.drawable.snow;
        else if (weatherId >= 701 && weatherId <= 761)
            return R.drawable.mist;
        else if (weatherId == 761 || weatherId == 781)
            return R.drawable.storm;
        else if (weatherId == 800) {
            //Checking for day/night cycles
            if(hour>=6&&hour<18)
                return R.drawable.clear_day;
            else
                return R.drawable.clear_night;
        } else if (weatherId == 801) {
            if(hour>=6&&hour<=18)
                return R.drawable.light_clouds_day;
            else
                return R.drawable.light_clouds_night;
        } else if (weatherId >= 802 && weatherId <= 804)
            return R.drawable.cloudy;
        return -1;
    }
    public static boolean getSunOrMoon(){

        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.today_forecast, container, false);
        return rootView;
    }
}
