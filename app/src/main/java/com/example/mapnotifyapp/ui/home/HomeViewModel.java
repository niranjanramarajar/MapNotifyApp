package com.example.mapnotifyapp.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    public static List<String> textVal = new ArrayList<>();

    private String displayText = "Initial text";

    private BroadcastReceiver broadcastReceiver;

    public BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }

    public HomeViewModel() {
        mText = new MutableLiveData<>();
//        String txt = "";
//        for (int i = 0; i < (textVal.size()) && i < 3; i++) {
//            txt += textVal.get(i) + "\r\n";
//        }
        mText.setValue(displayText);
        Log.d("MyNotifiyApp", "HomeViewModel const: "+textVal.size());
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String liveText = intent.getStringExtra("live_text_key");
                mText.setValue(liveText);
            }
        };
    }

    public static void addText(String str) {
        if ( textVal.size() > 3) textVal.clear();
        Log.d("MyNotifiyApp", "HomeViewModel add text: "+textVal.size());
        textVal.add(str);
    }

    public void setText(String str) {
        Log.d("MyNotifiyApp", "HomeViewModel set text: "+textVal.size());
        Log.d("MyNotifiyApp", "HomeViewModel set text: "+str);
        displayText = str;
        mText.setValue(displayText);
    }

    public LiveData<String> getText() {
//        String txt = "This is home fragment,";
//        int i = Math.min(3, textVal.size());
//        for (; i > 0 ; i--) {
//            txt += textVal.get(i-1) + "\r\n";
//        }
//        mText.setValue(txt);
//        Log.d("MyNotifiyApp", "HomeViewModel: "+textVal.size());
        return mText;
    }
}