package com.superdroid.test.activity.mediaplayer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity {

    // Views
    ImageView   img;
    TextView    musicTitle;
    ImageView   prevBtn;
    ImageView   playBtn;
    ImageView   nextBtn;
    SeekBar     seekBar;
    TextView    curtime;
    TextView    totalTime;

    // Activity
    Boolean     play = true;
    Integer     currentMusicAlbumId;
    String      currentMusicTitle;
    String      currentMusicFilePath;
    String      currentMusicAlbumPath;

    Integer     arraySize;
    String []   musicArr;
    String []   musicPathArr;
    int []      albumIdArr;
    String []   albumPathArr;
    int         mIdx = 0;

    MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        // MediaPlayer 생성
        mPlayer = new MediaPlayer();

        // View 등록
        img = (ImageView) findViewById(R.id.center_album);
        musicTitle = (TextView) findViewById(R.id.music_title_text);
        prevBtn = (ImageView) findViewById(R.id.previous_button);
        playBtn = (ImageView) findViewById(R.id.play_button);
        nextBtn = (ImageView) findViewById(R.id.next_button);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        curtime = (TextView) findViewById(R.id.curtime_textview);
        totalTime = (TextView) findViewById(R.id.totalTime_textView);

        mPlayer.setOnCompletionListener(mOnComplete);
        mPlayer.setOnErrorListener(mOnError);
        mPlayer.setOnSeekCompleteListener(mOnSeekComplete);

        seekBar.setOnSeekBarChangeListener(mOnSeek);
        mProgressHandler.sendEmptyMessageDelayed(0, 200);

        // 이전 버튼, 다음 버튼 크기 조절
        prevBtn.getLayoutParams().height = 200;
        prevBtn.getLayoutParams().width = 200;

        nextBtn.getLayoutParams().height = 200;
        nextBtn.getLayoutParams().width = 200;

        // 전달된 데이터 받기
        Intent intent = getIntent();

        arraySize = intent.getIntExtra("arraySize", 20);
        currentMusicTitle = intent.getStringExtra("title");
        currentMusicAlbumId = intent.getIntExtra("album_id",0);
        currentMusicAlbumPath = intent.getStringExtra("music_album_path");

        musicArr = new String[arraySize];
        albumIdArr = new int[arraySize];
        albumPathArr = new String[arraySize];
        musicPathArr = new String[arraySize];

        musicArr = intent.getStringArrayExtra("musicArr");
        albumIdArr = intent.getIntArrayExtra("albumIdArr");
        albumPathArr = intent.getStringArrayExtra("albumPathArr");

        // 전달 받은 데이터로 초기화면 설정
        musicTitle.setText(currentMusicTitle + ".mp3");

        Drawable d = ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground);
        if (currentMusicAlbumPath != null)
            d = Drawable.createFromPath(currentMusicAlbumPath);

        img.setImageDrawable(d);

        // 음악들의 경로 저장 + idx 인덱스 설정
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        for (int i = 0 ; i < arraySize ; i++) {
            musicPathArr[i] = file.getAbsolutePath() + "/" + musicArr[i] + ".mp3";
            if ( currentMusicTitle.equals(musicArr[i])) mIdx = i;
        }

        // 선택한 음악을 Media에 load
        if (LoadMedia(mIdx) == false) {
            Toast.makeText(this, "Cannot read file", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    // 액티비티 종료 시 mPlayer 강제 종료
    public void onDestory() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    boolean LoadMedia(int idx) {
        try {
            mPlayer.setDataSource(musicPathArr[idx]);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IllegalStateException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        if (Prepare() == false) {
            return false;
        }

        int dur = mPlayer.getDuration();
        musicTitle.setText(musicArr[idx]);
        seekBar.setMax(dur);
        setTotalTime();

        return true;
    }

    public void setTotalTime() {
        int dur = mPlayer.getDuration();
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
    }

    public void setCurtime() {
        int dur = mPlayer.getCurrentPosition();
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
    }

    boolean Prepare() {
        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void mOnClick(View v) {
        switch(v.getId()) {
            case R.id.play_button:
                if (mPlayer.isPlaying() == false) {
                    mPlayer.start();
                    //playBtn.setText("Pause");
                    playBtn.setImageResource(R.drawable.pause);
                    playBtn.getLayoutParams().height = 250;
                    playBtn.getLayoutParams().width = 250;
                    play = true;
                } else {
                    mPlayer.pause();
                    //playBtn.setText("Play");
                    playBtn.setImageResource(R.drawable.play);
                    playBtn.getLayoutParams().height = 250;
                    playBtn.getLayoutParams().width = 250;
                    play = false;
                }
                break;
            case R.id.previous_button:
            case R.id.next_button:
                boolean wasPlaying = mPlayer.isPlaying();
                if (v.getId() == R.id.previous_button) {
                    mIdx = (mIdx == 0 ? musicArr.length - 1 : mIdx - 1);
                } else {
                    mIdx = (mIdx == musicArr.length - 1 ? 0 : mIdx + 1);
                }
                mPlayer.reset();
                LoadMedia(mIdx);

                if (wasPlaying) {
                    mPlayer.start();
                    //playBtn.setText("Pause");
                    play = true;
                }
                break;
        }
    }

    MediaPlayer.OnCompletionListener mOnComplete = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            mIdx = (mIdx == musicArr.length - 1 ? 0 : mIdx + 1);
            mPlayer.reset();
            LoadMedia(mIdx);
            mPlayer.start();
            play = true;
        }
    };

    MediaPlayer.OnErrorListener mOnError = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            String err = "OnError occured what = " + what + " ,extra= " + extra;
            Log.d("onError", err);
            return false;
        }
    };

    MediaPlayer.OnSeekCompleteListener mOnSeekComplete = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            if (play) {
                mPlayer.start();
            }
        }
    };

    Handler mProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mPlayer == null) return;
            if (mPlayer.isPlaying()) {
                seekBar.setProgress(mPlayer.getCurrentPosition());
                setCurtime();
            }
            mProgressHandler.sendEmptyMessageDelayed(0, 200);
        }
    };

    SeekBar.OnSeekBarChangeListener mOnSeek = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mPlayer.seekTo(progress);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            play = mPlayer.isPlaying();
            if (play) {
                mPlayer.pause();
            }
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}