package com.example.mapnotifyapp.ui.notifications;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.example.mapnotifyapp.MainActivity;
import com.example.mapnotifyapp.ui.home.HomeViewModel;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    HomeViewModel homeViewModel;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
        HomeViewModel.textVal.add("added from NotificationViewModel");
        try {
            homeViewModel = new ViewModelProvider(ViewModelStore::new).get(HomeViewModel.class);
            homeViewModel.setText("From notificaiton view model");
        } catch (Exception argException) {
            argException.printStackTrace();
        }
        Log.d("GMapNotificationListenerService", "onCreate completed");
    }

    public LiveData<String> getText() {
        return mText;
    }
}