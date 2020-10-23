package com.out.activitymusic;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.out.activitymusic.database.FavoriteSongsProvider;
import com.out.activitymusic.interfaces.IDisplayMediaFragment;
import com.out.activitymusic.interfaces.IItemClickListener;

import java.io.IOException;
import java.util.ArrayList;

import Service.MediaPlaybackService;


public class BaseSongListFragment extends Fragment implements IItemClickListener, SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private static final String SHARED_PREFERENCES_NAME = "1";
    private ListAdapter mListAdapter;
    private String mURL = "content://com.out.activitymusic.database.FavoriteSongsProvider";
    private Uri mURISong = Uri.parse(mURL);
    private RecyclerView mRecyclerView;
    private Song song;
    public RelativeLayout mRelativeLayout;
    TextView mNameSong;
    TextView mArtist;
    ImageView mPicture;
    ArrayList<Song> mListSong;
    MediaPlaybackService mediaPlaybackService;
    MediaPlaybackFragment mediaPlaybackFragment;
    private boolean Ischeck = false;
    private IDisplayMediaFragment IDisplayMediaFragment;
    private ImageView mPlayPause;
    private ImageView mMusicPop;
    UpdateUI UpdateUI;
    int index;
    private View mInflater;

    public void setListAdapter(ListAdapter adapter) {
        this.mListAdapter = adapter;
    }

    public void setListSongs(ArrayList<Song> listSongs) {
        this.mListSong = listSongs;
        Log.d("mListSong1234", "setListSongs:base "+ mListSong);
    }


    public void setService(MediaPlaybackService service) {
        this.mediaPlaybackService = service;
    }

    public BaseSongListFragment(IDisplayMediaFragment IDisplayMediaFragment, MediaPlaybackFragment mediaPlaybackFragment) {
        this.IDisplayMediaFragment = IDisplayMediaFragment;
        this.mediaPlaybackFragment = mediaPlaybackFragment;
    }

    public BaseSongListFragment() {
    }

    public void setAdapter(ListAdapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("nhungltk12", "onCreateView: ");
        mInflater = inflater.inflate(R.layout.allsongsfragment, container, false);
        init();
        //Bkav Nhungltk: doan nay nghia la sao
        ((MainActivity) getActivity()).setiConnectActivityAndBaseSong(new MainActivity.IConnectActivityAndBaseSong() {
            @Override
            public void connectActivityAndBaseSong() {
                if (((MainActivity) getActivity()).mediaPlaybackService != null) {
                    Log.d("nhungltkk", "onCreateView: " + "not null");
                    mediaPlaybackService = ((MainActivity) getActivity()).mediaPlaybackService;
                    mediaPlaybackFragment.setService(mediaPlaybackService);
                }
            }
        });

        UpdateUI = new UpdateUI(getContext());
        firstUpdate();
        clickRelaytiveLayout();
        onClickPause();

        if (mediaPlaybackService != null) {
            if (isLandscape()) {
                mediaPlaybackFragment.setService(mediaPlaybackService);
            }
        }
        updateUI();
        return mInflater;
    }
    public void init(){
        mRecyclerView = mInflater.findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRelativeLayout = mInflater.findViewById(R.id.bottom);
        mPlayPause = mInflater.findViewById(R.id.play_pause);
        mNameSong = mInflater.findViewById(R.id.name_song);
        mArtist = mInflater.findViewById(R.id.artist);
        mPicture = mInflater.findViewById(R.id.picture);
        mMusicPop = mInflater.findViewById(R.id.music_pop);
        mRecyclerView.setHasFixedSize(true);
    }
    public void firstUpdate(){
        index = UpdateUI.getIndex();
        if (UpdateUI.getAlbum() != null) {
            mNameSong.setText(UpdateUI.getTitle());
            mArtist.setText(UpdateUI.getArtist());
            byte[] songArt = getAlbumArt(UpdateUI.getFile());
            Glide.with(mInflater.getContext()).asBitmap()
                    .load(songArt)
                    .error(R.drawable.default_cover_art)
                    .into(mPicture);
        }
    }
    public void clickRelaytiveLayout(){
        if (isLandscape()) {
            mRelativeLayout.setVisibility(View.GONE);
            //khi xoay service null
        } else {
            if(Ischeck){//chua truyen duoc bien Ischeck
                Log.d("Ischeck", "clickRelaytiveLayout: "+Ischeck);
                mRelativeLayout.setVisibility(View.VISIBLE);
                mRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaPlaybackService.setListSong(mListSong);
                        mediaPlaybackFragment.setService(mediaPlaybackService);
                        IDisplayMediaFragment.onclickIDisplay(song);
                    }
                });}
            else {   mRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaPlaybackService.setListSong(mListSong);
                    mediaPlaybackFragment.setService(mediaPlaybackService);
                    IDisplayMediaFragment.onclickIDisplay(mListSong.get(UpdateUI.getIndex()));
                }
            });}

        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onClickIItem(Song song) {
        this.song = song;
        if (isLandscape()) {
            mediaPlaybackFragment.setService(mediaPlaybackService);
            mediaPlaybackFragment.setListSong(mListSong);
            mediaPlaybackFragment.getText(song);
            mediaPlaybackService.setmMediaPlaybackFragment(mediaPlaybackFragment);
            Log.d("mListFavoriteSongs", "onClickIItem:land "+mListSong);
        }else{
        if (mediaPlaybackService.getPlaying()) {
            mediaPlaybackService.pauseMedia();
            try {
                mediaPlaybackService.playMedia(song);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mediaPlaybackService.playMedia(song);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mNameSong.setText(song.getTitle());
        mArtist.setText(song.getArtist());
        byte[] songArt = getAlbumArt(song.getFile());
        Glide.with(mInflater.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.default_cover_art)
                .into(mPicture);}
        Ischeck = true;
        mediaPlaybackFragment.setIscheck(true);
        try {
            if (mediaPlaybackService.getPlaying()) {
                mediaPlaybackService.getmMediaPlayer().pause();
                mediaPlaybackService.playMedia(song);
            } else
                mediaPlaybackService.playMedia(song);
            String selection = " id_provider =" + song.getID();
            Cursor c = getActivity().managedQuery(mURISong, null, selection, null, null);
            if (c.moveToFirst()) {
                do {
                    if (c.getInt(c.getColumnIndex(FavoriteSongsProvider.IS_FAVORITE)) != 1)
                        if (c.getInt(c.getColumnIndex(FavoriteSongsProvider.COUNT_OF_PLAY)) < 2) {
                            ContentValues values = new ContentValues();
                            values.put(FavoriteSongsProvider.COUNT_OF_PLAY, c.getInt(c.getColumnIndex(FavoriteSongsProvider.COUNT_OF_PLAY)) + 1);
                            getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + song.getID(), null);
                        } else {
                            if (c.getInt(c.getColumnIndex(FavoriteSongsProvider.COUNT_OF_PLAY)) == 2) {
                                ContentValues values = new ContentValues();
                                values.put(FavoriteSongsProvider.COUNT_OF_PLAY, 0);
                                values.put(FavoriteSongsProvider.IS_FAVORITE, 2);
                                getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + song.getID(), null);
                            }
                        }
                } while (c.moveToNext());
            }
//            mediaPlaybackService.showNotification(song.getTitle(), song.getArtist(), song.getFile());
            updateUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mListAdapter!=null) mListAdapter.notifyDataSetChanged();
    }

    public boolean isLandscape() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return true;
        else return false;
    }

    public Uri queryAlbumUri(String imgUri) {// dung album de load anh
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, Long.parseLong(imgUri));//noi them mSrcImageSong vao artworkUri
    }

    public static byte[] getAlbumArt(String uri) {// dung file de load anh
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] albumArt = mediaMetadataRetriever.getEmbeddedPicture();  // chuyển đổi đường dẫn file media thành đường dẫn file Ảnh
        mediaMetadataRetriever.release();
        return albumArt;
    }

    public void onClickPause() {
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlaybackService != null) {
                    if (mediaPlaybackService.getPlaying()) {
                        mediaPlaybackService.pauseMedia();
                        mPlayPause.setImageResource(R.drawable.ic_media_play_light);
                        mediaPlaybackService.setPlaying(false);
                    } else {
                        if (!Ischeck) {
                            try {
                                mediaPlaybackService.playMedia(mListSong.get(index));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                mediaPlaybackService.playMedia(song);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mPlayPause.setImageResource(R.drawable.ic_pause_black_large);
                        mediaPlaybackService.setPlaying(true);
                    }
                    mediaPlaybackService.showNotification(mediaPlaybackService.getNameSong(), mediaPlaybackService.getArtist(), mediaPlaybackService.getFile());
                }
                updateUI();
            }
        });
        if(mListAdapter!=null)
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_bar, menu);
        MenuItem searchItem = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mListAdapter.getFilter().filter(s);
        return false;
    }

    public void updateUI() {
        Log.d("mListAdapter", "updateUI:330 " + mListAdapter);
        if (mediaPlaybackService != null) {
            if (mediaPlaybackService.getmMediaPlayer() != null) {
                byte[] songArt = getAlbumArt(mediaPlaybackService.getFile());
                Glide.with(mInflater.getContext()).asBitmap()
                        .load(songArt)
                        .error(R.drawable.default_cover_art)
                        .into(mPicture);
                mNameSong.setText(mediaPlaybackService.getNameSong());
                mArtist.setText(mediaPlaybackService.getArtist());
                if (mediaPlaybackService.isPlaying()) {
                    mPlayPause.setImageResource(R.drawable.ic_pause_black_large);
                } else
                    mPlayPause.setImageResource(R.drawable.ic_media_play_light);
            }
        } else {
            if (UpdateUI.getIsPlaying() == true)
                mPlayPause.setImageResource(R.drawable.ic_pause_black_large);
            else mPlayPause.setImageResource(R.drawable.ic_media_play_light);
        }
        if(mListAdapter!=null) mListAdapter.notifyDataSetChanged();
    }
}











