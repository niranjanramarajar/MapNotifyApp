package com.example.mapnotifyapp;


import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import com.example.mapnotifyapp.ui.home.HomeViewModel;

public class GMapNotificationListenerService extends NotificationListenerService {

    public GMapNotificationListenerService() {
        Log.d("GMapNotificationListenerService", "constructor called");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GMapNotificationListenerService", "onCreate called");

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
            SpannableString spannableString = (SpannableString) sbn.getNotification().extras.get("android.text");
            String notifyStr = spannableString.toString();
            HomeViewModel.addText(notifyStr);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal here
    }
}