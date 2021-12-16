package com.superdroid.test.activity.mediaplayer;

public class musicData  {

    private String title;
    private Integer album_id;
    private String music_album_path;

    public musicData(String t, Integer a) {
        title = t;
        album_id = a;
    }

    public musicData(String t, Integer a,  String aPath) {
        title = t;
        album_id = a;
        music_album_path = aPath;
    }

    public void setAlbum_id(Integer album_id) {
        this.album_id = album_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMusic_album_path(String aPath) {this.music_album_path = aPath;}

    public String getTitle() {
        return title;
    }

    public Integer getAlbum_id() {
        return album_id;
    }


    public String getMusic_album_path() {return music_album_path; }
}
