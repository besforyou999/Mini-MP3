package com.superdroid.test.activity.mediaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<musicData> musicList;
    private HashMap<Integer, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 음악 제목, 음악 앨범 저장할 배열
        musicList = new ArrayList<>();
        map = new HashMap<>();

        // 리스트 뷰, 리스트 뷰 어댑터 객체 생성
        ListView listView;
        ListViewAdapter adapter;

        adapter = new ListViewAdapter();

        listView = (ListView) findViewById(R.id.listview1);
        listView.setAdapter(adapter);

        // 앱 처음 실행 시에는 MediaStore 접근 불가. 권한을 허락하고 앱을 재실행 해주세요
        checkPermission();

        // Music 디렉토리 속 mp3 파일들 읽기
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".mp3");
            }
        };

        File [] files = file.listFiles(filter);
        String [] filePaths = new String[files.length];

        for (int i = 0 ; i < files.length ; i++) filePaths[i] = files[i].getAbsolutePath();

        // Downloads 디렉토리의 음악 파일들 MediaScanner로 scan
        MediaScannerConnection.scanFile(this, filePaths, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.d("scanFile", "Scanned : " + path);
                Log.d("scanFile", "-> : " + uri);
            }
        });

        // MediaStore로 음악 파일 읽기
        readMusicAlbums();
        readAudioFiles();

        // 다음 액티비티에 전달한 배열 생성
        String [] musicArr = new String[musicList.size()];
        Integer [] albumIdArr = new Integer[musicList.size()];
        String [] albumPathArr = new String [musicList.size()];


        // 리스트뷰 어댑터에 목록 아이템 추가
        for (int i = 0 ; i < musicList.size() ; i++) {
            String title = musicList.get(i).getTitle();
            Integer id = musicList.get(i).getAlbum_id();
            String albumPath = map.get(id);

            musicArr[i] = title;
            albumIdArr[i] = id;
            albumPathArr[i] = albumPath;

            Drawable img = ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground);
            musicList.get(i).setMusic_album_path(albumPath);

            if (albumPath != null) {
                img = Drawable.createFromPath(albumPath);
            }
            // 앨범이 없으면 그냥 안드로이드 아이콘 추가
            adapter.addItem(img,title);
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
                       intent.putExtra("title", title);
                       intent.putExtra("album_id", md.getAlbum_id());
                       intent.putExtra("music_album_path", md.getMusic_album_path());
                       intent.putExtra("arraySize", musicList.size());
                       intent.putExtra("musicArr", musicArr);
                       intent.putExtra("albumIdArr", albumIdArr);
                       intent.putExtra("albumPathArr", albumPathArr);
                       intent.setClass(getApplicationContext(), PlayMusicActivity.class);
                       startActivity(intent);
                   }
               }
           }
        });
    }

    private void readAudioFiles() {
        Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        Cursor cursor = getContentResolver().query(externalUri, projection, null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            Log.e("cursor", "cursor null or cursor is empty");
            return;
        }

        do {
            int columnCount = cursor.getColumnCount();
            for (int i = 0 ; i < columnCount ; i++) {
                Log.d("cursor",cursor.getColumnName(i) + " : " + cursor.getString(i));
            }

            musicList.add(new musicData(cursor.getString(0) , Integer.parseInt(cursor.getString(1))));

        } while (cursor.moveToNext());

        cursor.close();
    }

    public void readMusicAlbums() {

        Uri albumsUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String [] albumProjection = new String [] {
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.ALBUM_ID
        };

        Cursor cursor = getContentResolver().query(albumsUri,albumProjection, null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            Log.e("cursor", "album cursor null or album cursor is empty");
            return;
        }

        int albumArt = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
        int albumId = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);

        do {
            int columnCount = cursor.getColumnCount();
            for (int i = 0 ; i < columnCount ; i++) {
                Log.d("cursor",cursor.getColumnName(i) + " : " + cursor.getString(i));
            }

            map.put(Integer.parseInt(cursor.getString(albumId)), cursor.getString(albumArt));

        } while (cursor.moveToNext());

        cursor.close();
    }


    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED){
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.FOREGROUND_SERVICE },
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

}