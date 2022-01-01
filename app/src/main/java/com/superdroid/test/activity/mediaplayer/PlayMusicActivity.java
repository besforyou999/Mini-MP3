package com.superdroid.test.activity.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity {

    // Views
    ImageView   albumView;
    TextView    musicTitle;
    ImageView   prevBtn;
    ImageView   playBtn;
    ImageView   nextBtn;
    SeekBar     seekBar;
    TextView    curtime;
    TextView    totalTime;

    // Activity
    Boolean     play = false;
    Boolean     isPaused;
    String      currentMusicTitle;
    String      currentMusicFilePath;

    // Icons
    Bitmap      playIcon;
    Bitmap      prevIcon;
    Bitmap      nextIcon;
    Bitmap      pauseIcon;

    // Broadcast Receiver
    BroadcastReceiver mBroadcastReceiver;

    // Seekbar data
    Integer     seekbarDuration;
    Integer     seekbarCurrentPos;

    // Meta data
    MediaMetadataRetriever       mmr;
    private ArrayList<MusicData> musicList;

    private Integer              musicIdx;

    public static String PLAY_ACTION             = "com.superdroid.test.activity.mainactivity.play";
    public static String PAUSE_ACTION            = "com.superdroid.test.activity.mainactivity.pause";
    public static String START_FOREGROUND_ACTION = "com.superdroid.test.activity.mainactivity.startforeground";
    public static String SEEKBAR_CURRENT_POS     = "com.superdroid.test.activity.mainactivity.seekbarCurrentPos";
    public static String MUSIC_FINISHED          = "com.superdroid.test.activity.mainactivity.musicFinished";
    public static String NOTI_PLAY_BTN_CHANGE    = "com.superdroid.test.activity.mainactivity.notiPlayBtnClick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        // Media meta data
        mmr         = new MediaMetadataRetriever();
        musicList   = new ArrayList<>();
        musicIdx    = 0;

        // View 등록
        albumView   = (ImageView) findViewById(R.id.center_album);
        musicTitle  = (TextView) findViewById(R.id.music_title_text);
        prevBtn     = (ImageView) findViewById(R.id.previous_button);
        playBtn     = (ImageView) findViewById(R.id.play_button);
        nextBtn     = (ImageView) findViewById(R.id.next_button);
        seekBar     = (SeekBar) findViewById(R.id.seekBar);
        curtime     = (TextView) findViewById(R.id.curtime_textview);
        totalTime   = (TextView) findViewById(R.id.totalTime_textView);

        // Create Icon
        playIcon  = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        prevIcon  = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_previous);
        nextIcon  = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_next);
        pauseIcon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause);

        float bmpWidth  = playIcon.getWidth() * 1.5f;
        float bmpHeight = playIcon.getHeight() * 1.5f;

        playIcon  = Bitmap.createScaledBitmap(playIcon, (int)bmpWidth, (int)bmpHeight, true);
        prevIcon  = Bitmap.createScaledBitmap(prevIcon, (int)bmpWidth, (int)bmpHeight, true);
        nextIcon  = Bitmap.createScaledBitmap(nextIcon, (int)bmpWidth, (int)bmpHeight, true);
        pauseIcon = Bitmap.createScaledBitmap(pauseIcon, (int)bmpWidth, (int)bmpHeight, true);

        prevBtn.setImageBitmap(prevIcon);
        playBtn.setImageBitmap(playIcon);
        nextBtn.setImageBitmap(nextIcon);

        // read meta data
        buildSongList();

        // 전달된 데이터 받기
        Intent intent = getIntent();

        currentMusicTitle       = intent.getStringExtra("music_title");
        currentMusicFilePath    = intent.getStringExtra("music_path");
        seekbarDuration         = intent.getIntExtra("music_duration", 0);
        isPaused                = intent.getBooleanExtra("isPaused", true);
        seekbarCurrentPos       = intent.getIntExtra("currentPos", 0);
        musicIdx                = intent.getIntExtra("music_index", 0);

        // 주어진 데이터로 화면 빌드
        buildScreen();

        // seekbar duration, seekbar current pos 설정
        setTotalTime(seekbarDuration);
        setCurtime(seekbarCurrentPos);

        // register receiver 생성
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SEEKBAR_CURRENT_POS);
        intentFilter.addAction(MUSIC_FINISHED);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String flag = intent.getAction();
                if (flag.equals(MUSIC_FINISHED) || flag.equals(SEEKBAR_CURRENT_POS)) {
                    seekbarCurrentPos = intent.getIntExtra("seekbarCurrentPos", 0);
                    setCurtime(seekbarCurrentPos);
                    seekBar.setProgress(seekbarCurrentPos);
                    if (intent.getAction().equals(MUSIC_FINISHED)) {
                        play = false;
                        playBtn.setImageBitmap(playIcon);
                    }
                } else if (flag.equals(NOTI_PLAY_BTN_CHANGE)) {
                    if (intent.getBooleanExtra("change_to_play",true)) {
                        playBtn.setImageBitmap(playIcon);
                    } else
                        playBtn.setImageBitmap(pauseIcon);
                }
            }
        };

        // register receiver 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

        // register seekbar listener
        seekBar.setOnSeekBarChangeListener(mOnSeek);

        Intent playIntent = new Intent(PlayMusicActivity.this, ForegroundService.class);
        playIntent.setAction(START_FOREGROUND_ACTION);
        playIntent.putExtra("music_title", currentMusicTitle);
        playIntent.putExtra("music_path", currentMusicFilePath);
        playIntent.putExtra("music_duration", seekbarDuration);
        startService(playIntent);
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.play_button: {
                if (play == false) {
                    // 서비스에 시작 버튼 눌렀다는 인텐트 발송
                    Intent playIntent = new Intent();
                    playIntent.setAction(PLAY_ACTION);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(playIntent);
                    playBtn.setImageBitmap(pauseIcon);
                    play = true;
                    break;
                } else {
                    // 서비스에 pause 버튼 눌렀다는 인텐트 발송
                    Intent pauseIntent = new Intent();
                    pauseIntent.setAction(PAUSE_ACTION);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pauseIntent);
                    playBtn.setImageBitmap(playIcon);
                    play = false;
                    break;
                }
            }
            case R.id.next_button: {
                musicIdx = (musicIdx == musicList.size() - 1 ? 0 : musicIdx + 1);
                NextPrevBtnClick();
                break;
            }
            case R.id.previous_button: {
                musicIdx = (musicIdx == 0 ? musicList.size() - 1 : musicIdx - 1);
                NextPrevBtnClick();
                break;
            }
        }
    }

    public void NextPrevBtnClick() {
        Log.d("smj", "hi");
        MusicData md = musicList.get(musicIdx);

        currentMusicTitle       = md.getTitle();
        currentMusicFilePath    = md.getPathId();
        seekbarDuration         = md.getDuration();
        Log.d("smj", currentMusicTitle);
        Log.d("smj", currentMusicFilePath);
        Log.d("smj", seekbarDuration.toString());

        buildScreen();

        Intent playIntent = new Intent(PlayMusicActivity.this, ForegroundService.class);
        playIntent.setAction(START_FOREGROUND_ACTION);
        playIntent.putExtra("music_title", currentMusicTitle);
        playIntent.putExtra("music_path", currentMusicFilePath);
        playIntent.putExtra("music_duration", seekbarDuration);
        startService(playIntent);
    }

    public void buildScreen() {
        // 음악 제목 설정
        musicTitle.setText(currentMusicTitle + ".mp3");

        // 앨범 설정
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(currentMusicFilePath);
        byte [] data = mmr.getEmbeddedPicture();

        Drawable img = new BitmapDrawable(getResources(), playIcon);

        if (data != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            img = new BitmapDrawable(getResources(), bm);
        }

        albumView.setImageDrawable(img);

        Point screenSize = getScreenSize(this);

        albumView.getLayoutParams().width  = screenSize.x;
        albumView.getLayoutParams().height = screenSize.x;

        // notification 눌러서 다시 불러진 액티비티인 경우
        // 음악이 재생중이였다 -> play button = pause icon
        // 음악이 일시정지중이였다 - > play button = play icon
        if (isPaused == false) {
            playBtn.setImageBitmap(pauseIcon);
            play = true;
        } else if ( isPaused == true) {
            playBtn.setImageBitmap(playIcon);
            play = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("flowDebug", "onDestroy called in PlayMusicActivity");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void setTotalTime(int dur) {
        dur = dur / 1000;   // msec -> sec
        Integer min = dur / 60;
        Integer sec = dur % 60;
        String minutes = new String();
        String seconds = new String();
        String t = new String();
        if (min < 10)
            minutes = "0";
        minutes += min.toString();
        t = minutes + ":";

        if (sec < 10)
            seconds = "0";
        seconds += sec.toString();
        t += seconds;

        totalTime.setText("/ " + t);
        seekBar.setMax(seekbarDuration);
    }

    public void setCurtime(int dur) {
        dur = dur / 1000;   // msec -> sec
        Integer min = dur / 60;
        Integer sec = dur % 60;
        String minutes = new String();
        String seconds = new String();
        String t = new String();
        if (min < 10)
            minutes = "0";
        minutes += min.toString();
        t = minutes + ":";

        if (sec < 10)
            seconds = "0";
        seconds += sec.toString();
        t += seconds;
        curtime.setText(t);
        seekBar.setProgress(dur);
    }

    SeekBar.OnSeekBarChangeListener mOnSeek = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    };

    public Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay(); // 1번 과정
        Point size = new Point();
        display.getSize(size); // 2번 과정
        return size;
    }

    public void buildSongList() {

        ContentResolver mResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mResolver.query(musicUri, null, null, null, null);

        if (cursor == null) {
            Log.e("cursor", "album cursor null");
            return;
        }
        if (!cursor.moveToFirst()) {
            Log.e("cursor", "cursor moveToFirst error");
            return;
        }

        int musicTitle   = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
        int id           = cursor.getColumnIndex(BaseColumns._ID);
        int artist       = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int duration     = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        do {

            String  MusicTitle  = cursor.getString(musicTitle);
            Long    ID          = cursor.getLong(id);
            String  Artist      = cursor.getString(artist);
            String  pathId      = cursor.getString(column_index);
            Integer Duration    = cursor.getInt(duration);

            byte [] byteArr;
            mmr.setDataSource(pathId);
            Drawable albumArt = new BitmapDrawable(getResources(), playIcon);
            byteArr = mmr.getEmbeddedPicture();

            if (byteArr != null) {
                Bitmap bm = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
                albumArt  = new BitmapDrawable(getResources(), bm);
            }

            musicList.add(new MusicData(MusicTitle, Artist, ID, pathId, Duration, albumArt ));

        } while (cursor.moveToNext());

        return;
    }

}