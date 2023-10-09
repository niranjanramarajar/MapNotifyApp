package com.example.mapnotifyapp.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    public static List<String> textVal = new ArrayList<>();

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        String txt = "This is home fragment in constructor,";
        for (int i = 0; i < (textVal.size()) && i < 3; i++) {
            txt += textVal.get(i) + "\r\n";
        }
        mText.setValue(txt);
        Log.d("MyNotifiyApp", "HomeViewModel const: "+textVal.size());
    }

    public static void addText(String str) {
        if ( textVal.size() > 10) textVal.clear();
        Log.d("MyNotifiyApp", "HomeViewModel add text: "+textVal.size());
        textVal.add(str);
    }

    public LiveData<String> getText() {
        String txt = "This is home fragment,";
        for (int i = 0; i < (textVal.size()) && i < 3; i++) {
            txt += textVal.get(i) + "\r\n";
        }
        mText.setValue(txt);
        Log.d("MyNotifiyApp", "HomeViewModel: "+textVal.size());
        return mText;
    }
}