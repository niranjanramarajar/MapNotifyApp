package com.example.mapnotifyapp.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapnotifyapp.MainActivity;
import com.example.mapnotifyapp.ui.home.HomeViewModel;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
        HomeViewModel.textVal.add("added from NotificationViewModel");
    }

    public LiveData<String> getText() {
        return mText;
    }
}