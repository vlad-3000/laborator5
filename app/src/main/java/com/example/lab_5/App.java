package com.example.lab_5;

import android.app.Application;

import com.example.lab_5.service.CommonService;
import com.example.lab_5.service.Service;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initializeServices();
    }

    private void initializeServices() {
        CommonService.createInstance(getApplicationContext());
        Service.createInstance(getApplicationContext());
    }
}
