package com.out.activitymusic;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.out.activitymusic.database.FavoriteSongsProvider;
import com.out.activitymusic.interfaces.IDataFavoriteAndAllSong;
import com.out.activitymusic.interfaces.IDataFragment;
import com.out.activitymusic.interfaces.IDisplayMediaFragment;

import java.util.ArrayList;

import Service.MediaPlaybackService;

import static android.content.Context.MODE_PRIVATE;


public class AllSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SHARED_PREFERENCES_NAME = "1";
    private ListAdapter mListAdapter;
    private SharedPreferences mSharePreferences;
    ArrayList<Song> mListSongs;
    IDataFragment IDataFragment;
    IDataFavoriteAndAllSong IDataFavoriteAndAllSong;
    IDisplayMediaFragment IDisplayMediaFragment;
    MediaPlaybackFragment mediaPlaybackFragment;
    MediaPlaybackService mediaPlaybackService;
    FavoriteSongsProvider favoriteSongsProvider;

    private Boolean IsBoolean = false;
    private static final int LOADER_UI_EVENT = 1;

    public void setBoolean(Boolean aBoolean) {
        IsBoolean = aBoolean;
    }

    public Boolean getIsBoolean() {
        return IsBoolean;
    }

    public void setService(MediaPlaybackService service) {
        this.mediaPlaybackService = service;
        super.mediaPlaybackService = service;
    }

    public AllSongsFragment(IDataFragment IDataFragment, IDisplayMediaFragment IDisplayMediaFragment, MediaPlaybackFragment mediaPlaybackFragment) {
        super(IDisplayMediaFragment, mediaPlaybackFragment);
        this.IDataFragment = IDataFragment;
        this.IDisplayMediaFragment = IDisplayMediaFragment;
        this.mediaPlaybackFragment = mediaPlaybackFragment;
    }

    public void setAllSong(IDataFavoriteAndAllSong IDataFavoriteAndAllSong) {
        this.IDataFavoriteAndAllSong = IDataFavoriteAndAllSong;
    }

    public AllSongsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LoaderManager.getInstance(this).initLoader(LOADER_UI_EVENT, null, this);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        CursorLoader cursorLoader = new CursorLoader(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        return cursorLoader;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mSharePreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mListSongs = new ArrayList<>();
        boolean isCreate = mSharePreferences.getBoolean("create_db", false);
        int id = 0;
        String title = "";
        String file = "";
        String album = "";
        String artist = "";
        String duration = "";
        favoriteSongsProvider = new FavoriteSongsProvider();
        Song song = new Song(id, title, file, album, artist, duration, false);
        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            do {
                id++;
                song.setID(id);
                song.setTitle(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                song.setFile(data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA)));
                song.setAlbum(data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                song.setArtist(data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                song.setDuration(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                title = song.getTitle();
                file = song.getFile();
                album = song.getAlbum();
                artist = song.getArtist();
                duration = song.getDuration();
                Log.d("mediaPlaybackService1", "onLoadFinished:service " + mediaPlaybackService);

                mListSongs.add(new Song(id, title, file, album, artist, duration, false));

                if (isCreate == false) {
                    ContentValues values = new ContentValues();
                    values.put(FavoriteSongsProvider.ID_PROVIDER, id);
                    values.put(FavoriteSongsProvider.IS_FAVORITE, 0);
                    values.put(FavoriteSongsProvider.COUNT_OF_PLAY, 0);
                    Uri uri = getActivity().getContentResolver().insert(FavoriteSongsProvider.CONTENT_URI, values);
                    mSharePreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharePreferences.edit();
                    editor.putBoolean("create_db", true);
                    editor.commit();
                }
            } while (data.moveToNext());
        }
        if(mediaPlaybackService!=null){
            for(int j=0;j<mListSongs.size();j++){
            for(int i=0;i<mediaPlaybackService.getfavoriteID().size();i++){
                if(j==mediaPlaybackService.getfavoriteID().get(i)){

                }
            }}
        }
        FavoriteSongsFragment favoriteSongsFragment = new FavoriteSongsFragment();
        favoriteSongsFragment.setLisSong(mListSong);/////////////////////////////////////.............
        setListSongs(mListSongs);
        Log.d("HoangCVmediaf", "onLoadFinished: " + mediaPlaybackFragment);
        mListAdapter = new ListAdapter(getContext(), mListSongs, this);
        mediaPlaybackFragment.setListSong(mListSongs);
        IDataFragment.onclickIData(mListSongs);
        // IDataFavoriteAndAllSong.onClickIDataFaboriteAndAllSong(mListSongs);
        mediaPlaybackFragment.setService(mediaPlaybackService);
        setAdapter(mListAdapter);
        setListAdapter(mListAdapter);
        mListAdapter.setService(mediaPlaybackService);
        if (mediaPlaybackService != null) mediaPlaybackService.setListSong(mListSongs);
        if (isLandscape()) {

            Log.d("favoriteSongsFragment", "onLoadFinished: " + mListSongs);
            favoriteSongsFragment.setLisSong(mListSong);
            setListSongs(mListSongs);
            mListAdapter.setService(mediaPlaybackService);
            mediaPlaybackFragment.setService(mediaPlaybackService);
            mediaPlaybackFragment.setListSong(mListSongs);
            mediaPlaybackFragment.updateTime();
            // IDataFavoriteAndAllSong.onClickIDataFaboriteAndAllSong(mListSongs);
            if (mediaPlaybackService != null)
                mediaPlaybackService.setListSong(mListSongs);
            //khi xoay truyen duoc listsong sang service
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public boolean isLandscape() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return true;
        else return false;
    }

}




