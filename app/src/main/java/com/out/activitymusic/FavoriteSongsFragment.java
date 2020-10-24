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

public class FavoriteSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IDataFavoriteAndAllSong {
    private static final int LOADER_ID = 1;
    private ArrayList<Song> mListAllSong;
    private ListAdapter mListAdapter;
    MediaPlaybackFragment mediaPlaybackFragment;
    IDataFavorite iDataFavorite;
    private int id,id_provider;

    public FavoriteSongsFragment(ArrayList arrayList, MediaPlaybackService service, MediaPlaybackFragment mediaPlaybackFragment, IDisplayMediaFragment IDisplayMediaFragment, IDataFavorite iDataFavorite) {
        super(IDisplayMediaFragment, mediaPlaybackFragment);
        this.mListAllSong=arrayList;
        this.mediaPlaybackService = service;
        this.mediaPlaybackFragment = mediaPlaybackFragment;
        this.iDataFavorite=iDataFavorite;
    }

    public FavoriteSongsFragment() {
    }
    public void setLisSong(ArrayList mListAllSong){
        this.mListAllSong=mListAllSong;
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
        CursorLoader cursorLoader = new CursorLoader(getContext(), uriSongs, null, selection, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        ArrayList<Song> mListFavoriteSongs = new ArrayList<>();
        ArrayList<Integer> id=new ArrayList<>();
        Log.d("favoriteSongsFragment", "onLoadFinished: "+mListAllSong);
        if(mListAllSong!=null){
        Song song = null;
        int dem = 1;
        if (data.moveToFirst()) {
            do {
                for (int i = 0; i < mListAllSong.size(); i++) {
                    if (mListAllSong.get(i).getID() == data.getInt(data.getColumnIndex(FavoriteSongsProvider.ID_PROVIDER))) {
                        Log.d("song F", data.getInt(data.getColumnIndex(FavoriteSongsProvider.ID_PROVIDER)) + "//" + mListAllSong.get(i).getID());
                        song = new Song(dem, mListAllSong.get(i).getTitle(), mListAllSong.get(i).getFile(), mListAllSong.get(i).getAlbum(), mListAllSong.get(i).getArtist(), mListAllSong.get(i).getDuration(),true);
                        dem++;
                        id.add(i);
                        mediaPlaybackService.setFavoriteID(id,true);
                        mListFavoriteSongs.add(song);
                    }
                }
            } while (data.moveToNext());
        }}
        else
            mListFavoriteSongs=mediaPlaybackService.getListSong();
        setListSongs(mListFavoriteSongs);
        mediaPlaybackFragment.setListSong(mListFavoriteSongs);
        mediaPlaybackFragment.setService(mediaPlaybackService);
        mListAdapter = new ListAdapter(getContext(), mListFavoriteSongs, this);
        setAdapter(mListAdapter);
        mediaPlaybackFragment.setListFavoriteSong(mListFavoriteSongs);
        iDataFavorite.onClickIDataFavorite(mListFavoriteSongs);
        mediaPlaybackService.setListSong(mListFavoriteSongs);
        setListAdapter(mListAdapter);
        mListAdapter.setService(mediaPlaybackService);
        if (isLandscape()) {
            Log.d("mListFavoriteSongs", "onLoadFinished: "+mListFavoriteSongs);
            Log.d("mListFavoriteSongs", "onLoadFinished: "+mListAllSong);
            setListSongs(mListFavoriteSongs);
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