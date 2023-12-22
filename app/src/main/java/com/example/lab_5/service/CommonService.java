package com.example.lab_5.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class CommonService {
    private static CommonService instance;
    public final Context context;
    private Handler mHandler;

    public CommonService(Context context) {
        this.context = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static CommonService getInstance() {
        return instance;
    }

    public static void createInstance(Context context) {
        if (instance == null) instance = new CommonService(context);
    }

    public void showToast(String message) {
        mHandler.post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    public Handler getHandler() {
        return mHandler;
    }
}
