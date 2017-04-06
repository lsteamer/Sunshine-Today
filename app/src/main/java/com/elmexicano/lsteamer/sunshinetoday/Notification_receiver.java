package com.elmexicano.lsteamer.sunshinetoday;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * Created by lsteamer on 30/03/2017.
 */

public class Notification_receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Notification Manager to show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String lat = intent.getStringExtra("Latitude");
        String lon = intent.getStringExtra("Longitude");
        String[] textsForNotification;
        textsForNotification = fillInText(lat,lon);


        //Intent that opens the app when the user clicks on it
        Intent repeating_intent = new Intent(context, MainActivity.class);
        //If old activity is running in the background, clear it up.
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Instance of the Pending Intent to show the notification.
        PendingIntent pendingIntent = PendingIntent.getActivity(context,237,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), Tab1Today.getWeatherImage(Integer.valueOf(textsForNotification[2])) );
        //Building the actual notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.sundog_transparent)
                .setLargeIcon(icon)
                .setContentTitle(textsForNotification[0])
                .setContentText(textsForNotification[1])
                .setAutoCancel(true);
                //

        notificationManager.notify(0,builder.build());

    }

    public String[] fillInText(String latitude, String longitude){
        DownloadTask weatherAsyncTask = new DownloadTask();
        MainActivity cleaner = new MainActivity();
        String[] backNotify = new String[3];
        //String to receive the code
        String [] uncleanedJsonCode = new String[2];
        try {
            uncleanedJsonCode = weatherAsyncTask.execute(latitude,longitude,"metric","1").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(uncleanedJsonCode!=null){

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat notificationDateFormat = new SimpleDateFormat("EEE MMM d");

            String[] weatherInfo = cleaner.jsonCleaner(uncleanedJsonCode);
            //Today's date
            backNotify[0] = "Forecast for " + notificationDateFormat.format(cal.getTime());
            //Today's highest
            backNotify[1] = "Today's highest will be "+ weatherInfo[7]+ "\u00B0";
            //Today's image
            backNotify[2] = weatherInfo[5];


        }

        return backNotify;

    }

}
