package com.superdroid.test.activity.mediaplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class testService extends Service {
    public testService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }



}