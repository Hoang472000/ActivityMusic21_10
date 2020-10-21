package com.out.activitymusic;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.out.activitymusic.interfaces.IDataFavorite;
import com.out.activitymusic.interfaces.IDataFavoriteAndAllSong;
import com.out.activitymusic.interfaces.IDisplayMediaFragment;

import java.util.ArrayList;

import Service.MediaPlaybackService;

public class FavoriteSongsFragmentI extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IDataFavoriteAndAllSong {
    private static final int LOADER_ID = 1;
    private ArrayList<Song> mListAllSong;
    private ListAdapter mListAdapter;
    MediaPlaybackFragment mediaPlaybackFragment;
    IDataFavorite iDataFavorite;
    private int id,id_provider;

    public FavoriteSongsFragmentI(MediaPlaybackService service, MediaPlaybackFragment mediaPlaybackFragment, IDisplayMediaFragment IDisplayMediaFragment, IDataFavorite iDataFavorite) {
        super(IDisplayMediaFragment, mediaPlaybackFragment);
        this.mediaPlaybackService = service;
        this.mediaPlaybackFragment = mediaPlaybackFragment;
        this.iDataFavorite=iDataFavorite;
    }

    public FavoriteSongsFragmentI() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    public void getData(){

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String URL = "content://com.out.activitymusic.database.FavoriteSongsProvider";
        Uri uriSongs = Uri.parse(URL);
        String selection = FavoriteSongsProvider.IS_FAVORITE + "==2";
        return new CursorLoader(getContext(), uriSongs, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        ArrayList<Song> mListFavoriteSongs = new ArrayList<>();
        if (mediaPlaybackService != null) {
            mListAllSong = mediaPlaybackService.getListSong();
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        Song song = null;
        int dem = 0;
        if (data.moveToFirst()) {
            do {
                for (int i = 0; i < mListAllSong.size(); i++) {
                    if (mListAllSong.get(i).getID() == data.getInt(data.getColumnIndex(FavoriteSongsProvider.ID_PROVIDER))) {
                        Log.d("song F", data.getInt(data.getColumnIndex(FavoriteSongsProvider.ID_PROVIDER)) + "//" + mListAllSong.get(i).getID());
                        song = new Song(dem, mListAllSong.get(i).getTitle(), mListAllSong.get(i).getFile(), mListAllSong.get(i).getAlbum(), mListAllSong.get(i).getArtist(), mListAllSong.get(i).getDuration());
                        dem++;
                        mListFavoriteSongs.add(song);
                    }
                }
            } while (data.moveToNext());
        }
        setListSongs(mListFavoriteSongs);
        mediaPlaybackFragment.setListSong(mListFavoriteSongs);
        mediaPlaybackFragment.setService(mediaPlaybackService);
        mListAdapter = new ListAdapter(getContext(), mListFavoriteSongs, this);
        setAdapter(mListAdapter);
        iDataFavorite.onClickIDataFavorite(mListFavoriteSongs);
       // mediaPlaybackService.setListSong(mListAllSong);
        mediaPlaybackService.setListSong(mListFavoriteSongs);
        mediaPlaybackService.setListSongFavorite(mListFavoriteSongs);
        setListAdapter(mListAdapter);
        mListAdapter.setService(mediaPlaybackService);
        if (isLandscape()) {
            setListSongs(mListFavoriteSongs);
            mediaPlaybackService.setListSongFavorite(mListFavoriteSongs);
            mListAdapter.setService(mediaPlaybackService);
            mediaPlaybackFragment.setService(mediaPlaybackService);
            mediaPlaybackFragment.setListSong(mListFavoriteSongs);
            if (mediaPlaybackService != null) mediaPlaybackFragment.updateTime();
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

    @Override
    public void onClickIDataFaboriteAndAllSong(ArrayList mListSong) {
        this.mListAllSong = mListSong;
    }
}