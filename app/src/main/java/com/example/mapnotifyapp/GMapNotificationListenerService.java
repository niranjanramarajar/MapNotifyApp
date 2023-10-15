package com.example.mapnotifyapp;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import androidx.appcompat.widget.DrawableUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.example.mapnotifyapp.ui.home.HomeViewModel;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMapNotificationListenerService extends NotificationListenerService {

    HomeViewModel homeViewModel = null;



    private int counter = 1;

    public GMapNotificationListenerService() {
        Log.d("GMapNotificationListenerService", "constructor called");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GMapNotificationListenerService", "onCreate called");
        try {
            homeViewModel = new ViewModelProvider(ViewModelStore::new).get(HomeViewModel.class);
        } catch (Exception argException) {
            argException.printStackTrace();
        }
        Log.d("GMapNotificationListenerService", "onCreate completed");
        //HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d("GMapNotificationListenerService", "onListenerConnected: " );
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("GMapNotificationListenerService", "onNotificationPosted: " + sbn);
        String str = sbn.getNotification().tickerText != null ? sbn.getNotification().tickerText.toString() : "none";
        Log.d("GMapNotificationListenerService", "onNotificationPosted1: " + str);
        if (sbn.getPackageName().equals("com.google.android.apps.maps")) {
            Log.d("GMapNotificationListenerService", "onNotificationPosted2: " + str);
            Notification notification = sbn.getNotification();
            Bundle extrasBundle = notification.extras;
            Object obj = extrasBundle.get("android.largeIcon");
            Log.d("GMapNotification", obj.getClass().toString());
            Icon icon = (Icon) obj;
            Context context = getApplicationContext();
            Icon icon1 = notification.getLargeIcon();
            String checksum = ".xx.";
            String direction = "d:";
            Drawable drawable = icon1.loadDrawable(getApplicationContext());
            if ( drawable instanceof BitmapDrawable ) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                byte bmArray[] = getByteArrayFromBitmap(bitmap);
                checksum = checksum(bitmap);
                if ( checksum.startsWith("a")) direction = "Straight";
                if ( checksum.startsWith("2")) direction = "U-turn";
                if ( checksum.startsWith("4")) direction = "Left";
                if ( checksum.startsWith("7")) direction = "Right";
                Log.d("GMapNotificationListener", "Checksum:"+checksum);
            }

            //Bitmap bitmap = icon.loadDrawable(context);

//            Uri uri = icon.getUri();
//            Log.d("GMapNotification", "uri:"+uri.toString());
            //checksum(bitmap);
            SpannableString spannableString = (SpannableString) extrasBundle.get("android.text");
            String notifyStr = spannableString.toString();
            notifyStr = direction+":"+checksum +":" +notifyStr;
            post(notifyStr);
//            homeViewModel.setText(direction+":"+checksum +":" +notifyStr);
            //HomeViewModel.addText(direction+":"+checksum +":" +notifyStr);
        }
    }

    private void post(String message) {
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
        Intent intent = new Intent("your_action_string");
        intent.putExtra("live_text_key", message);
        counter++;
        sendBroadcast(intent);
//            }
//        }, 0, 5000);
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    private String checksum(Bitmap bm) {
        if ( bm != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //bm is the bitmap object
            bm .compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                BigInteger bigInt = new BigInteger(1, md5.digest(byteArray));
                String md5Str = String.format("%032x", bigInt);
                return md5Str;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ".yy.";
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal here
    }


    private String findTurn(String text) {
        Pattern pattern = Pattern.compile("(?<turnIndication>\\w+) turn");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String turnIndication = matcher.group("turnIndication");
            return turnIndication;
        }
        return "";
    }
}