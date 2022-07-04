package com.app.realtimechat.utils;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityUtil {

    private final AppCompatActivity activity;

    public ActivityUtil(AppCompatActivity appCompatActivity) {
        this.activity = appCompatActivity;
    }

    public void switchActivityWithFlag(Context context, Class tClass) {
        Intent intent = new Intent(context, tClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public void switchActivity(Context context, Class tClass) {
        Intent intent = new Intent(context, tClass);
        activity.startActivity(intent);
    }
}
