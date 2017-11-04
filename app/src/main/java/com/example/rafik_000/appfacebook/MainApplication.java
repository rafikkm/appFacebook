package com.example.rafik_000.appfacebook;


import android.app.Application;
import com.facebook.appevents.AppEventsLogger;


public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppEventsLogger.activateApp(this);
    }
}