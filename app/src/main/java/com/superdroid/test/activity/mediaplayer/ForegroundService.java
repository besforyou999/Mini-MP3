package com.superdroid.test.activity.mediaplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

public class ForegroundService extends Service {

    public static String PLAY_ACTION             = "com.superdroid.test.activity.mainactivity.play";
    public static String PAUSE_ACTION            = "com.superdroid.test.activity.mainactivity.pause";
    public static String START_FOREGROUND_ACTION = "com.superdroid.test.activity.mainactivity.startforeground";
    public static String STOP_FOREGROUND_ACTION  = "com.superdroid.test.activity.mainactivity.stopforeground";
    public static String SEEKBAR_CURRENT_POS     = "com.superdroid.test.activity.mainactivity.seekbarCurrentPos";
    public static String SEEK_ACTION             = "com.superdroid.test.activity.mainactivity.seekbarMove";
    public static String MUSIC_FINISHED          = "com.superdroid.test.activity.mainactivity.musicFinished";

    String          CHANNEL_ID      = "Channel One";
    String          NAME            = "Title";
    final int       NOTIFICATION_ID = 1;

    Bitmap                      playIcon;
    Bitmap                      pauseIcon;
    NotificationCompat.Builder  notiBuilder;
    NotificationChannel         channel;
    NotificationManager         notiManager;

    MediaPlayer     mediaPlayer;

    // Currently playing Music data
    Integer     CURRENT_POS;
    Integer     DURATION;
    String      MUSIC_PATH;
    String      MUSIC_TITLE;
    boolean     firstPlay;
    boolean     isPaused;

    String      ORIGIN_MUSIC;

    RemoteViews notificationLayout;
    RemoteViews notificationLayoutExpanded;

    MediaMetadataRetriever  mmr;
    byte []                 data;
    Bitmap                  album;

    BroadcastReceiver       mBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mmr = new MediaMetadataRetriever();
        CURRENT_POS  = 0;
        ORIGIN_MUSIC = null;
        firstPlay    = true;
        isPaused     = true;
        mediaPlayer  = new MediaPlayer();
        mediaPlayer.setOnErrorListener(mOnError);
        mediaPlayer.setOnPreparedListener(mPrepared);
        mediaPlayer.setOnCompletionListener(mComplete);

        playIcon  = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        pauseIcon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause);

        notificationLayout          = new RemoteViews(getPackageName(), R.layout.notification_small);
        notificationLayoutExpanded  = new RemoteViews(getPackageName(), R.layout.notification_large);

        notiBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        notiManager = getSystemService(NotificationManager.class);
        createNotificationChannel();

        // broadcast Receiver 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(STOP_FOREGROUND_ACTION);
        intentFilter.addAction(PAUSE_ACTION);
        intentFilter.addAction(PLAY_ACTION);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String flag = intent.getAction();
                if (flag.equals(STOP_FOREGROUND_ACTION))
                    onDestroy();
                else if (flag.equals(PAUSE_ACTION)) {
                    onPauseBtnPressed();
                } else if (flag.equals(PLAY_ACTION)) {
                    onPlayBtnPressed();
                }
            }
        };
        // register receiver 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public ForegroundService() { }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        MUSIC_PATH  = intent.getStringExtra("music_path");
        MUSIC_TITLE = intent.getStringExtra("music_title");
        DURATION    = intent.getIntExtra("music_duration", 0);
        CURRENT_POS = 0;

        // 처음 노래 재생하는 경우
        if (MUSIC_PATH != null && ORIGIN_MUSIC == null) {
            LoadMedia(MUSIC_PATH);
        } else if (ORIGIN_MUSIC != null) {
            if( ORIGIN_MUSIC.equals(MUSIC_TITLE) == false) { // 이전에 재생한 노래와 다른 노래를 재생하는 경우
                mediaPlayer.reset();
                LoadMedia(MUSIC_PATH);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MUSIC_FINISHED);
                broadcastIntent.putExtra("seekbarCurrentPos", 0);
                LocalBroadcastManager.getInstance(ForegroundService.this).sendBroadcast(broadcastIntent);
            }   // 이전에 재생한 노래와 같은 노래를 재생하는 경우
            else
                LoadMedia(MUSIC_PATH);
        }

        ORIGIN_MUSIC = MUSIC_TITLE;

        // building thumbnail
        if (MUSIC_PATH != null) {
            mmr.setDataSource(MUSIC_PATH);
            data = mmr.getEmbeddedPicture();
            if (data == null)
                album = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
            else
                album = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        if (intent.getAction().equals(START_FOREGROUND_ACTION)) {

            if (isPaused == true)
                notiBuilder.setSmallIcon(android.R.drawable.ic_media_pause);
            else
                notiBuilder.setSmallIcon(android.R.drawable.ic_media_play);

            // album click pending intent
            Intent notificationIntent   = new Intent(this, PlayMusicActivity.class);
            notificationIntent.putExtra("music_path", MUSIC_PATH);
            notificationIntent.putExtra("music_title", MUSIC_TITLE);
            notificationIntent.putExtra("music_duration", DURATION);
            notificationIntent.putExtra("isPaused", isPaused);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // play button click pending intent
            Intent playBtnIntent = new Intent(this, ForegroundService.class);
            playBtnIntent.setAction(PLAY_ACTION);
            playBtnIntent.putExtra("music_path", MUSIC_PATH);
            playBtnIntent.putExtra("music_title", MUSIC_TITLE);
            playBtnIntent.putExtra("music_duration", DURATION);
            playBtnIntent.putExtra("isPaused", isPaused);
            PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playBtnIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationLayout.setOnClickPendingIntent(R.id.noti_play_btn, playPendingIntent);
            notificationLayoutExpanded.setOnClickPendingIntent(R.id.noti_play_btn, playPendingIntent);

            notificationLayout.setOnClickPendingIntent(R.id.noti_music_album, pendingIntent);
            notificationLayoutExpanded.setOnClickPendingIntent(R.id.noti_music_album,pendingIntent);

            notificationLayout.setTextViewText(R.id.noti_music_title, MUSIC_TITLE);
            notificationLayoutExpanded.setTextViewText(R.id.noti_music_title, MUSIC_TITLE);

            notificationLayout.setImageViewBitmap(R.id.noti_music_album, album);
            notificationLayoutExpanded.setImageViewBitmap(R.id.noti_music_album, album);

            startForeground(NOTIFICATION_ID, notiBuilder.build());

        } else if (intent.getAction().equals(PLAY_ACTION)) {

            onPlayBtnPressed();

        } else if (intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
            //onDestroy();

        } else if (intent.getAction().equals(PAUSE_ACTION)) {

            onPauseBtnPressed();

        } else if (intent.getAction().equals(SEEK_ACTION)) {

            Integer progress = intent.getIntExtra("currentProgress",0);
            mediaPlayer.seekTo(progress);
            mediaPlayer.start();
            CURRENT_POS = mediaPlayer.getCurrentPosition();
            notiBuilder.setSmallIcon(android.R.drawable.ic_media_play);
            notiManager.notify(NOTIFICATION_ID, notiBuilder.build());
        }

        return START_STICKY;
    }

    public void onPlayBtnPressed() {

        mediaPlayer.start();
        isPaused = false;
        firstPlay = false;

        // notification play button pause 로 바꾸기
        notificationLayout.setImageViewBitmap(R.id.noti_play_btn, pauseIcon);
        notificationLayoutExpanded.setImageViewBitmap(R.id.noti_play_btn, pauseIcon);

        // pendingintent 바꾸기
        Intent playBtnIntent = new Intent(this, ForegroundService.class);
        playBtnIntent.setAction(PAUSE_ACTION);
        playBtnIntent.putExtra("music_path", MUSIC_PATH);
        playBtnIntent.putExtra("music_title", MUSIC_TITLE);
        playBtnIntent.putExtra("music_duration", DURATION);
        playBtnIntent.putExtra("isPaused", isPaused);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playBtnIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationLayout.setOnClickPendingIntent(R.id.noti_play_btn, playPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.noti_play_btn, playPendingIntent);

        notiBuilder.setSmallIcon(android.R.drawable.ic_media_play);

        notiManager.notify(NOTIFICATION_ID, notiBuilder.build());
    }

    public void onPauseBtnPressed() {
        if (mediaPlayer.isPlaying()) {
            CURRENT_POS = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            isPaused = true;
        }

        // play btn pending intent 바꾸기
        Intent playBtnIntent = new Intent(this, ForegroundService.class);
        playBtnIntent.setAction(PLAY_ACTION);
        playBtnIntent.putExtra("music_path", MUSIC_PATH);
        playBtnIntent.putExtra("music_title", MUSIC_TITLE);
        playBtnIntent.putExtra("music_duration", DURATION);
        playBtnIntent.putExtra("isPaused", isPaused);
        playBtnIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playBtnIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationLayout.setOnClickPendingIntent(R.id.noti_play_btn, playPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.noti_play_btn, playPendingIntent);

        // 이미지 변경
        notificationLayout.setImageViewBitmap(R.id.noti_play_btn, playIcon);
        notificationLayoutExpanded.setImageViewBitmap(R.id.noti_play_btn, playIcon);

        // rebuild notification to send current position for seekbar
        notiBuilder.setSmallIcon(android.R.drawable.ic_media_pause);
        Intent notificationIntent   = new Intent(this, PlayMusicActivity.class);
        notificationIntent.putExtra("music_path", MUSIC_PATH);
        notificationIntent.putExtra("music_title", MUSIC_TITLE);
        notificationIntent.putExtra("music_duration", DURATION);
        notificationIntent.putExtra("isPaused", isPaused);
        notificationIntent.putExtra("currentPos", CURRENT_POS);
        playBtnIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationLayout.setOnClickPendingIntent(R.id.noti_music_album, pendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.noti_music_album,pendingIntent);

        notiManager.notify(NOTIFICATION_ID, notiBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("flowDebug", "onDestroy called in ForegroundService");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        CURRENT_POS = 0;
        isPaused = true;
        notiManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = NAME;
            String description = "Channel first";
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            notiManager.createNotificationChannel(channel);
        }
    }

    boolean LoadMedia(String path) {
        try {
            mediaPlayer.setDataSource(path);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IllegalStateException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        if (Prepare() == false) return false;

        return true;
    }

    boolean Prepare() {
        try {
            mediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    MediaPlayer.OnErrorListener mOnError = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            String err = "OnError occured what = " + what + " ,extra= " + extra;
            Log.d("onError", err);
            return false;
        }
    };

    MediaPlayer.OnPreparedListener mPrepared = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mProgressHandler.sendEmptyMessageDelayed(0, 200);
        }
    };

    Handler mProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying()) {

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(SEEKBAR_CURRENT_POS);
                broadcastIntent.putExtra("isPaused", isPaused);
                broadcastIntent.putExtra("seekbarCurrentPos", mediaPlayer.getCurrentPosition());
                LocalBroadcastManager.getInstance(ForegroundService.this).sendBroadcast(broadcastIntent);
                CURRENT_POS = mediaPlayer.getCurrentPosition();

                Intent notificationIntent   = new Intent(ForegroundService.this, PlayMusicActivity.class);
                notificationIntent.putExtra("music_path", MUSIC_PATH);
                notificationIntent.putExtra("music_title", MUSIC_TITLE);
                notificationIntent.putExtra("music_duration", DURATION);
                notificationIntent.putExtra("isPaused", isPaused);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(ForegroundService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                notificationLayout.setOnClickPendingIntent(R.id.noti_music_album, pendingIntent);
                notificationLayoutExpanded.setOnClickPendingIntent(R.id.noti_music_album,pendingIntent);
            }
            mProgressHandler.sendEmptyMessageDelayed(0,100);
        }
    };

    MediaPlayer.OnCompletionListener mComplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            CURRENT_POS = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            firstPlay = true;
            isPaused = true;

            // seekbar to 0 , and currtime to 00:00
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MUSIC_FINISHED);
            broadcastIntent.putExtra("seekbarCurrentPos", 0);
            LocalBroadcastManager.getInstance(ForegroundService.this).sendBroadcast(broadcastIntent);

            // small icon to pause
            notiBuilder.setSmallIcon(android.R.drawable.ic_media_pause);
            notiManager.notify(NOTIFICATION_ID, notiBuilder.build());
        }
    };
}