package com.superdroid.test.activity.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {

    public static String MAIN_ACTION = "com.superdroid.test.activity.mediaplayer.MainActivity";
    public static String PLAY_ACTION = "com.superdroid.test.activity.mediaplayer.Play";
    public static String NEXT_PLAY_ACTION = "com.superdroid.test.activity.mediaplayer.nextplay";
    public static String START_FOREGROUND_ACTION = "com.superdroid.test.activity.mediaplayer.startforeground";
    public static String STOP_FOREGROUND_ACTION = "com.superdroid.test.activity.mediaplayer.stopforeground";


    String CHANNEL_ID = "Channel One";
    String name = "Title";
    final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        Intent intent = new Intent(this, PlayMusicActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setContentTitle("Count Service")
                                .setContentText("Running Count Service")
                                .setSmallIcon(R.drawable.play)
                                .setContentIntent(pIntent)
                                .build();

        startForeground(1234, noti);
    }

    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if (intent.getAction().equals(START_FOREGROUND_ACTION)) {
            /*
            Intent notificationIntent = new Intent(this, PlayMusicActivity.class);
            notificationIntent.setAction(MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent playIntent = new Intent(this, ForegroundService.class);
            playIntent.setAction(PLAY_ACTION);
            PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, 0);

            Intent nplayIntent = new Intent(this, ForegroundService.class);
            playIntent.setAction(NEXT_PLAY_ACTION);
            PendingIntent nplayPendingIntent = PendingIntent.getService(this, 0, nplayIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
            notiBuilder.setContentTitle("My Music Player");
            notiBuilder.setTicker("My Music Player");
            notiBuilder.setContentText("My Music");
            //notiBuilder.setSmallIcon(android.R.drawable.ic_media_play);
            //notiBuilder.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false));
            notiBuilder.setContentIntent(pendingIntent);
            //notiBuilder.setOngoing(true);
            //notiBuilder.addAction(android.R.drawable.ic_media_play, "Play", playPendingIntent);
            //notiBuilder.addAction(android.R.drawable.ic_media_play, "Next", nplayPendingIntent);

            startForeground(1234, notiBuilder.build());

            */

        } else if (intent.getAction().equals(PLAY_ACTION)) {


        } else if (intent.getAction().equals(NEXT_PLAY_ACTION)) {


        } else if (intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }


}