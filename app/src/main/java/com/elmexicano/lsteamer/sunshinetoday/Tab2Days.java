package com.elmexicano.lsteamer.sunshinetoday;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Tab2Days extends Fragment{


    protected ListView followingDaysList;

    public void populateList(ArrayList<String> weatherList){

        //Declaring the List
        followingDaysList = (ListView) getActivity().findViewById(R.id.mainList);
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,weatherList);
        followingDaysList.setAdapter(adapt);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.following_days_forecast, container, false);

        return rootView;
    }
}
