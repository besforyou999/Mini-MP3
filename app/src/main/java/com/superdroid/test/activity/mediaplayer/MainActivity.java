package com.superdroid.test.activity.mediaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String STOP_FOREGROUND_ACTION  = "com.superdroid.test.activity.mainactivity.stopforeground";

    private ArrayList<musicData> musicList;

    // 앨범 생성용
    Bitmap                  androidIcon;
    MediaMetadataRetriever  mmr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 음악 제목, 음악 앨범 저장할 배열
        musicList   = new ArrayList<>();

        // 음악 앨범 생성용
        mmr         = new MediaMetadataRetriever();
        androidIcon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);

        // 리스트 뷰, 리스트 뷰 어댑터 객체 생성
        ListView listView;
        ListViewAdapter adapter;

        adapter = new ListViewAdapter();

        listView = (ListView) findViewById(R.id.listview1);
        listView.setAdapter(adapter);

        // 앱 처음 실행 시에는 MediaStore 접근 불가. 권한을 허락하고 앱을 재실행 해주세요
        checkPermission();

        // mp3 파일들 읽기
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".mp3");
            }
        };

        File [] files = file.listFiles(filter);
        String [] filePaths = new String[files.length];

        for (int i = 0 ; i < files.length ; i++) {
            filePaths[i] = files[i].getAbsolutePath();
            Log.d("filePaths", filePaths[i]);
        }

        // Downloads 디렉토리의 음악 파일들 MediaScanner로 scan
        MediaScannerConnection.scanFile(this, filePaths, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.d("scanFile", "Scanned : " + path);
                Log.d("scanFile", "-> : " + uri);
            }
        });

        // MediaStore로 음악 파일 읽기
        buildSongList();

        // 리스트뷰 어댑터에 목록 아이템 추가
        for (int i = 0 ; i < musicList.size() ; i++) {
            musicData md = musicList.get(i);
            adapter.addItem(md.getDrawable(),md.getTitle());
        }

        // 리스트뷰 아이템 클릭 핸들러 추가
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
               ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);
               String title = item.getMusicTitle();
               for (int i = 0 ; i < musicList.size() ; i++) {
                   musicData md = musicList.get(i);
                   if (md.getTitle().equals(title)) {
                       Intent intent = new Intent();
                       intent.putExtra("music_title", title);
                       intent.putExtra("music_path", md.getPathId());
                       intent.putExtra("music_duration", md.getDuration());
                       intent.setClass(getApplicationContext(), PlayMusicActivity.class);
                       startActivity(intent);
                   }
               }
           }
        });
    }

    public void buildSongList() {

        ContentResolver mResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mResolver.query(musicUri, null, null, null, null);

        if (cursor == null) { Log.e("cursor", "album cursor null");
            return;
        }

        if (!cursor.moveToFirst()) { Log.e("cursor", "cursor moveToFirst error");
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
            Drawable albumArt = new BitmapDrawable(getResources(), androidIcon);
            byteArr = mmr.getEmbeddedPicture();

            if (byteArr != null) {
                Bitmap bm = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
                albumArt  = new BitmapDrawable(getResources(), bm);
            }

            musicList.add(new musicData(MusicTitle, Artist, ID, pathId, Duration, albumArt ));

        } while (cursor.moveToNext());

        return;
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED){
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE ,
                                Manifest.permission.FOREGROUND_SERVICE },
                        1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED ){ // do nothing
        }
        else {  // 앱 실행 위해 권한 반드시 필요
            checkPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("flowDebug", "onDestroy called in MainActivity");

        Intent  destroyService = new Intent();
        destroyService.setAction(STOP_FOREGROUND_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(destroyService);

    }
}