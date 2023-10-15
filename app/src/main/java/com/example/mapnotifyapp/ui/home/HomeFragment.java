package com.example.mapnotifyapp.ui.home;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mapnotifyapp.R;
import com.example.mapnotifyapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        homeViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            updateTextView(textView, text);
        });
        BroadcastReceiver broadcastReceiver = homeViewModel.getBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("your_action_string");
        getContext().registerReceiver(broadcastReceiver, intentFilter);

        return root;
    }

    public void updateTextView(TextView textView, String text) {
        //TextView textView = findViewById(R.id.text_home);
        textView.setText(text);
        textView.invalidate();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}