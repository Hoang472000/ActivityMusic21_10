package com.out.activitymusic;

import android.content.Context;
import android.content.SharedPreferences;

public class UpdateUI {
    private final String STORAGE = " com.valdioveliu.valdio.audioplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;


    public UpdateUI(Context context) {
        this.context = context;
    }

    public void UpdateTitle(String title) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("title", title);
        editor.apply();
    }

    public String getTitle() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        String title = preferences.getString("title", null);

        return title;
    }

    public void UpdateArtist(String artist) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("artist", artist);
        editor.apply();
    }

    public String getArtist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        String artist = preferences.getString("artist", null);
        return artist;
    }

    public void UpdateAlbum(String album) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("album", album);
        editor.apply();
    }

    public String getAlbum() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        String album = preferences.getString("album", null);
        return album;
    }

    public String getFile() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        String file = preferences.getString("file", null);
        return file;
    }

    public void UpdateFile(String file) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("file", file);
        editor.apply();
    }

    public void UpdateIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("possision", index);
        editor.apply();
    }

    public int getIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        int index = preferences.getInt("possision", 0);
        return index;
    }

    public void UpdateIsPlaying(Boolean isplaying) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isplaying", isplaying);
        editor.apply();
    }

    public Boolean getIsPlaying() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Boolean isplaying = preferences.getBoolean("isplaying", false);
        return isplaying;
    }

    public void UpdateDuration(int Duration) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("Duration", Duration);
        editor.apply();
    }

    public int getDuration() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        int Duration = preferences.getInt("Duration", 0);
        return Duration;
    }

    public void UpdateCurrentPossision(int possision) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("current", possision);
        editor.apply();
    }

    public int getCurrentPossision() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        int possision = preferences.getInt("current", 0);
        return possision;
    }

    public void UpdateStatus(boolean status) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("status", status);
        editor.apply();
    }

    public boolean getStatus() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        boolean status = preferences.getBoolean("status", false);
        return status;
    }
    public void UpdateRepeat(int repeat) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("repeat", repeat);
        editor.apply();
    }

    public Integer getRepeat() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        int repeat = preferences.getInt("repeat", -2);
        return repeat;
    }
    public void UpdateShuffler(Boolean shuffler) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("shuffler", shuffler);
        editor.apply();
    }

    public Boolean getShuffler() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Boolean shuffler = preferences.getBoolean("shuffler", false);
        return shuffler;
    }


}