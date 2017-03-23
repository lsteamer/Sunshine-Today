package com.elmexicano.lsteamer.sunshinetoday;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Created by lsteamer on 23/03/2017.
 */

public class GPSTool {

    String locationProvider = LocationManager.GPS_PROVIDER;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public GPSTool(Context context){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.turnOnGPS(context);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Location change

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(locationProvider,0,0, locationListener);
    }

    public Location getLocation(){
        return locationManager.getLastKnownLocation(locationProvider);
    }

    public void startGPSUpdate(){
        locationManager.requestLocationUpdates(locationProvider,0,0, locationListener);
    }

    public void stopGPSUpdate(){
        locationManager.removeUpdates(locationListener);
    }

    public void turnOnGPS(Context context){
        boolean isEnabled = locationManager.isProviderEnabled(locationProvider);
        if(!isEnabled){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        }
    }

}
