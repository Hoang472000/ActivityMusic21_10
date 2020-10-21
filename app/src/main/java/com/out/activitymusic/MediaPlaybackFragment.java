package com.out.activitymusic;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.out.activitymusic.database.FavoriteSongsProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Service.MediaPlaybackService;

public class MediaPlaybackFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    TextView mNameSong, mTimeSong2, mTimeSong1;
    ImageView mPictureSmall;
    private ImageView mMore, mImageBig;
    private ImageView mPlayPauseMedia;
    private MediaPlaybackService mediaPlaybackService;
    private Song song;
    private ImageView mLike, mDisLike, mQueue;
    private ImageView mPlayPrevious, mPlayNext, mShuffle, mRepeat;
    private SeekBar mSeekBar;
    private TextView mArtist;
    private ArrayList<Song> mListSong;

    public void setIscheck(boolean ischeck) {
        this.ischeck = ischeck;
    }

    private boolean ischeck ;
    UpdateUI mUpdateUI;
    private boolean isIscheck1 = false;
    private View view;
    private boolean mIsFavorite;

    public boolean isIsFavorite() {
        return mIsFavorite;
    }

    public void setIsFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }

    private ListAdapter mListAdapter;

    public MediaPlaybackFragment newInstance(Song song) {
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("audio", song);
        bundle.putString("song", song.getTitle());
        bundle.putString("artist1", song.getArtist());
        bundle.putString("song1", formatTime.format(Integer.valueOf(song.getDuration())));
        bundle.putString("song2", song.getFile());
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setCheck(boolean check) {
        this.isIscheck1 = check;
    }

    public boolean getcheck() {
        return isIscheck1;
    }

    public void setListSong(ArrayList mListSong) {
        this.mListSong = mListSong;
        Log.d("mListSong123", "setListSong: "+mListSong);
    }

    public void setService(MediaPlaybackService service) {
        this.mediaPlaybackService = service;
    }


    public MediaPlaybackFragment() {
    }

    private MainActivity getActivityMusic() {
        if (getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        }
        return null;
    }

    public void setData() {
        mediaPlaybackService = getActivityMusic().getMediaPlaybackService();
        Log.d("Hoanafs1gCggV", "setData: " + mediaPlaybackService);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setData();
        Log.d("MediaOnCreate", "onCreateView: " + mediaPlaybackService);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.mediaplaybackfragment, container, false);
        init();
        mUpdateUI = new UpdateUI(getContext());
        firstUpdate();
        onClickItem();
        onClickSeekBar();

        Log.d("mListSong123", "setListSong:coCreat "+mListSong);
        if (getArguments() != null) {
            setText(getArguments());
        }
        Popmenu();

        if (mediaPlaybackService != null) {
            mSeekBar.setMax(mUpdateUI.getDuration());
            updateUI();
            updateTime();
        }
        if (isLandscape()) {
            if (mQueue.getVisibility() == View.VISIBLE)
                mQueue.setVisibility(View.INVISIBLE);
            updateUI();

        }
        return view;
    }

    public void init() {
        mNameSong = view.findViewById(R.id.media_name_song);
        mArtist = view.findViewById(R.id.media_artist);
        mTimeSong2 = view.findViewById(R.id.TimeSong2);
        mTimeSong1 = view.findViewById(R.id.TimeSong1);
        mPictureSmall = view.findViewById(R.id.picture_small);
        mMore = view.findViewById(R.id.more_vert);
        mQueue = view.findViewById(R.id.queue_music);
        mPlayPauseMedia = view.findViewById(R.id.play_pause_media);
        mLike = view.findViewById(R.id.like);
        mPlayPrevious = view.findViewById(R.id.play_previous);
        mDisLike = view.findViewById(R.id.dislike);
        mPlayNext = view.findViewById(R.id.play_next);
        mSeekBar = view.findViewById(R.id.seekBar);
        mShuffle = view.findViewById(R.id.shuffle);
        mRepeat = view.findViewById(R.id.repeat);
        mImageBig = view.findViewById(R.id.image_big);
    }

    public void onClickItem() {
        mLike.setOnClickListener(this);
        mPlayPrevious.setOnClickListener(this);
        mPlayPauseMedia.setOnClickListener(this);
        mPlayNext.setOnClickListener(this);
        mDisLike.setOnClickListener(this);
        mShuffle.setOnClickListener(this);
        mRepeat.setOnClickListener(this);
    }

    public void firstUpdate() {
        if (isLandscape()) {
            SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
            mUpdateUI = new UpdateUI(getContext());
            mNameSong.setText(mUpdateUI.getTitle());
            mArtist.setText(mUpdateUI.getArtist());
            mPictureSmall.setImageURI(Uri.parse(mUpdateUI.getAlbum()));
            mImageBig.setImageURI(Uri.parse(mUpdateUI.getAlbum()));
            mTimeSong2.setText(formatTime.format(mUpdateUI.getDuration()));
            if (mUpdateUI.getIsPlaying() == true)
                mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            else mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
            if (mUpdateUI.getShuffler() == true)
                mShuffle.setImageResource(R.drawable.ic_play_shuffle_orange);
            else mShuffle.setImageResource(R.drawable.ic_shuffle_white);
            if (mUpdateUI.getRepeat() == -1) mRepeat.setImageResource(R.drawable.ic_repeat_white);
            else if (mUpdateUI.getRepeat() == 0)
                mRepeat.setImageResource(R.drawable.ic_repeat_dark_selected);
            else if (mUpdateUI.getRepeat() == 1)
                mRepeat.setImageResource(R.drawable.ic_repeat_one_song_dark);
        }
    }

    public void onClickSeekBar() {
        mSeekBar.setMax(mUpdateUI.getDuration());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlaybackService.seekToPos(progress);
                }
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                mTimeSong1.setText(formatTime.format(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlaybackService.getmMediaPlayer().seekTo(seekBar.getProgress());
            }
        });
    }

    public void getText(Song song) {
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        this.song = song;
        mNameSong.setText(song.getTitle());
        mArtist.setText(song.getArtist());
        mTimeSong2.setText(formatTime.format(Integer.valueOf(song.getDuration())));
        byte[] songArt = getAlbumArt(song.getFile());
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.default_cover_art)
                .into(mPictureSmall);
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.default_cover_art)
                .into(mImageBig);
        mSeekBar.setMax(mediaPlaybackService.getDuration());
    }

    public void setText(Bundle bundle) {
        mNameSong.setText(bundle.getString("song"));
        mArtist.setText(bundle.getString("artist1"));
        mTimeSong2.setText(bundle.getString("song1"));
        byte[] songArt = getAlbumArt(bundle.getString("song2"));
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.default_cover_art)
                .into(mPictureSmall);
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.default_cover_art)
                .into(mImageBig);
    }

    public Uri queryAlbumUri(String imgUri) {//dung album de load anh
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, Long.parseLong(imgUri));//noi them imgUri vao artworkUri
    }

    public static byte[] getAlbumArt(String uri) {// dung file de load anh
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] albumArt = mediaMetadataRetriever.getEmbeddedPicture();  // chuyển đổi đường dẫn file media thành đường dẫn file Ảnh
        mediaMetadataRetriever.release();
        return albumArt;
    }

    public void Popmenu() {
        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.setOnMenuItemClickListener(MediaPlaybackFragment.this);
                popup.inflate(R.menu.poupup_menu);
                popup.show();
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_song_favorite:
                ContentValues values = new ContentValues();
                values.put(FavoriteSongsProvider.IS_FAVORITE, 2);
                getContext().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + mListSong.get(mediaPlaybackService.getPossision()).getID(), null);
                Toast.makeText(getContext(), "addFavorite song //" + mListSong.get(mediaPlaybackService.getPossision()).getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.remove_song_favorite:
                ContentValues values1 = new ContentValues();
                values1.put(FavoriteSongsProvider.IS_FAVORITE, 1);
                values1.put(FavoriteSongsProvider.COUNT_OF_PLAY, 0);
                getContext().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values1, FavoriteSongsProvider.ID_PROVIDER + "= " + mListSong.get(mediaPlaybackService.getPossision()).getID(), null);
                Toast.makeText(getContext(), "removeFavorite song //" + mListSong.get(mediaPlaybackService.getPossision()).getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    public boolean isLandscape() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return true;
        else return false;
    }


    @Override
    public void onClick(View view) {
        //mediaPlaybackService.setListSong(mListSong);
        Log.d("mListSong123", "onClick: "+mListSong+"   pós:  "+mUpdateUI.getIndex());
        mediaPlaybackService.setPossition(mUpdateUI.getIndex());
        switch (view.getId()) {
            case R.id.like:
                mLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_default);
                ContentValues values = new ContentValues();
                values.put(FavoriteSongsProvider.IS_FAVORITE, 2);
                Log.d("ID_PROVIDER", "onClick: "+FavoriteSongsProvider.ID_PROVIDER  + "     "+ mUpdateUI.getIndex());
                getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + mUpdateUI.getIndex()+1, null);
                Toast.makeText(getContext(), "like song //" + mediaPlaybackService.getNameSong(), Toast.LENGTH_SHORT).show();
                /*        ContentValues values = new ContentValues();
                values.put(FavoriteSongsProvider.FAVORITE,2);
                getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI,values,FavoriteSongsProvider.ID_PROVIDER +"= "+ mMediaPlaybackService.getMinIndex(),null);
                Toast.makeText(getContext(),  "like song //"+ mMediaPlaybackService.getNameSong(), Toast.LENGTH_SHORT).show();
            }
        });*/
                break;
            case R.id.play_previous: {
                mediaPlaybackService.previousMedia();
                getText(mListSong.get(mediaPlaybackService.getPossision()));
                break;
            }
            case R.id.play_pause_media: {
                if (mediaPlaybackService.getPlaying()) {
                    mediaPlaybackService.pauseMedia();
                    mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    mediaPlaybackService.setPlaying(false);
                } else {

                    mediaPlaybackService.setPlaying(true);

                    if (mediaPlaybackService != null) {
                        if (mediaPlaybackService.isResume()) {
                            mediaPlaybackService.resumeMedia();
                        } else {
                            try {
                                mediaPlaybackService.playMedia(mListSong.get(mUpdateUI.getIndex()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    setCheck(true);
                }
                mediaPlaybackService.showNotification(mediaPlaybackService.getNameSong(),mediaPlaybackService.getArtist(),mediaPlaybackService.getFile());
                break;
            }
            case R.id.play_next: {
                mediaPlaybackService.nextMedia();
                getText(mListSong.get(mediaPlaybackService.getPossision()));
                break;
            }
            case R.id.dislike:
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_selected);
                mLike.setImageResource(R.drawable.ic_thumbs_up_default);
                values = new ContentValues();
                values.put(FavoriteSongsProvider.IS_FAVORITE, 1);
                getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + mediaPlaybackService.getPossision(), null);
                Toast.makeText(getContext(), "dislike song //" + mediaPlaybackService.getNameSong(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.shuffle: {
                if (!mediaPlaybackService.getShuffle()) {
                    mShuffle.setImageResource(R.drawable.ic_play_shuffle_orange);
                    mediaPlaybackService.setShuffle(true);
                    mUpdateUI.UpdateShuffler(true);
                } else {
                    mShuffle.setImageResource(R.drawable.ic_shuffle_white);
                    mediaPlaybackService.setShuffle(false);
                    mUpdateUI.UpdateShuffler(false);
                }
                break;
            }
            case R.id.repeat: {
                if (mediaPlaybackService.getRepeat() == 1) {
                    mRepeat.setImageResource(R.drawable.ic_repeat_white);
                    mediaPlaybackService.setRepeat(-1);
                    mUpdateUI.UpdateRepeat(-1);
                } else if (mediaPlaybackService.getRepeat() == -1) {
                    mRepeat.setImageResource(R.drawable.ic_repeat_dark_selected);
                    mediaPlaybackService.setRepeat(0);
                    mUpdateUI.UpdateRepeat(0);
                } else {
                    mRepeat.setImageResource(R.drawable.ic_repeat_one_song_dark);
                    mediaPlaybackService.setRepeat(1);
                    mUpdateUI.UpdateRepeat(1);
                }
                break;
            }
            default:
                break;
        }
        isIscheck1 = true;
        updateUI();
    }

    public void updateUI() {
        if (mediaPlaybackService != null && mSeekBar != null) {
            updateTime();
            mSeekBar.setMax(mediaPlaybackService.getDuration());
            mNameSong.setText(mediaPlaybackService.getNameSong());
            mArtist.setText(mediaPlaybackService.getArtist());
            byte[] songArt = getAlbumArt(mediaPlaybackService.getFile());
            Glide.with(view.getContext()).asBitmap()
                    .load(songArt)
                    .error(R.drawable.default_cover_art)
                    .into(mPictureSmall);
            Glide.with(view.getContext()).asBitmap()
                    .load(songArt)
                    .error(R.drawable.default_cover_art)
                    .into(mImageBig);
            SimpleDateFormat formmatTime = new SimpleDateFormat("mm:ss");
            if (mediaPlaybackService.getDuration()!=0)
                mTimeSong2.setText(formmatTime.format(mediaPlaybackService.getDuration()));
            else {
                mTimeSong2.setText(formmatTime.format(Integer.valueOf(mListSong.get(mUpdateUI.getIndex()).getDuration())));
            }
            if (mediaPlaybackService.getPlaying() == true) {
                mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            } else {
                mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
            }

            if (mediaPlaybackService.getShuffle()) {
                mShuffle.setBackgroundResource(R.drawable.ic_play_shuffle_orange);
            } else
                mShuffle.setBackgroundResource(R.drawable.ic_shuffle_white);

            if (mediaPlaybackService.getRepeat() == 0) {
                mRepeat.setBackgroundResource(R.drawable.ic_repeat_dark_selected);
            } else if (mediaPlaybackService.getRepeat() == -1) {
                mRepeat.setBackgroundResource(R.drawable.ic_repeat_white);
            } else mRepeat.setBackgroundResource(R.drawable.ic_repeat_one_song_dark);
            Log.d("HoangCVmIsFavorite", "updateUI: "+mIsFavorite);
     /*       if (mIsFavorite) {
                mLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_default);
            } else {
                mLike.setImageResource(R.drawable.ic_thumbs_up_default);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_selected);
            }*/

        }
    }

    public void updateLike() {
//        for (int i=0;i<id_provider.size()-1;i++){
//            for(int j=0;j<mListSong.size()-1;j++)
//            if(id_provider[i]==j){
//                mLike.setImageResource(R.drawable.ic_thumbs_up_selected);
//                mDisLike.setImageResource(R.drawable.ic_thumbs_down_default);
//            }
//        }
    }

    public void updateTime() {
        final Handler handler = new Handler();
        if (song != null || mListSong != null)
            handler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                    if (!mediaPlaybackService.isResume()) {
                        mTimeSong1.setText(formatTime.format(0));
                        mSeekBar.setProgress(0);
                    } else {
                        mTimeSong1.setText(formatTime.format(mediaPlaybackService.getCurrentStreamPosition()));
                        mSeekBar.setProgress(mediaPlaybackService.getCurrentStreamPosition());
                    }

                    if (mediaPlaybackService.isPlaying()) {
                        mTimeSong1.setText(formatTime.format(mediaPlaybackService.getCurrentStreamPosition()));
                        mSeekBar.setProgress(mediaPlaybackService.getCurrentStreamPosition());
                    }
                    mediaPlaybackService.getmMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            try {
                                if (mediaPlaybackService.getPlaying() == false) {
                                } else mediaPlaybackService.onCompletionSong();
                                mSeekBar.setMax(mediaPlaybackService.getDuration());
//                                updateUI();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    handler.postDelayed(this, 500);
                }
            }, 100);
    }

}
