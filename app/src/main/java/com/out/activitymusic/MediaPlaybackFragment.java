package com.out.activitymusic;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.out.activitymusic.database.FavoriteSongsProvider;
import com.out.activitymusic.interfaces.ITransmissionAllSongsFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import Service.MediaPlaybackService;

public class MediaPlaybackFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    TextView mNameSong, mTimeSong2, mTimeSong1;
    ImageView mPictureSmall;
    private ImageView mMore, mImageBig;
    private ImageView mPlayPauseMedia;
    private MediaPlaybackService mMediaPlaybackService;
    private Song song;
    private ImageView mLike, mDisLike, mQueue;
    private ImageView mPlayPrevious, mPlayNext, mShuffle, mRepeat;
    private SeekBar mSeekBar;
    private TextView mArtist;
    private ArrayList<Song> mListSong;
    private ArrayList<Integer> favoriteID;
    private BaseSongListFragment baseSongListFragment;
    private ITransmissionAllSongsFragment iTransmissionAllSongsFragment;

    public ArrayList<Song> getListFavoriteSong() {
        return mListFavoriteSong;
    }

    public void setListFavoriteSong(ArrayList<Song> mListFavoriteSong) {
        this.mListFavoriteSong = mListFavoriteSong;
    }

    public void setBaseSongListFragment(BaseSongListFragment baseSongListFragment) {
        this.baseSongListFragment = baseSongListFragment;
    }


    private ArrayList<Song> mListFavoriteSong;
    private SharedPreferences mSharePreferences;

    public void setIscheck(boolean ischeck) {
        this.ischeck = ischeck;
    }

    private boolean ischeck;
    UpdateUI mUpdateUI;
    private boolean isIscheck1 = false;
    private View view;
    private boolean mIsFavorite;

   /* public boolean getIsFavorite() {
        return mIsFavorite;
    }

    public void setIsFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }*/

    private ListAdapter mListAdapter;

    public MediaPlaybackFragment newInstance(Song song) {
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        MediaPlaybackFragment fragment = new MediaPlaybackFragment(this.iTransmissionAllSongsFragment);
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
    }

    public void setService(MediaPlaybackService service) {
        this.mMediaPlaybackService = service;
    }

    public MediaPlaybackFragment(ITransmissionAllSongsFragment iTransmissionAllSongsFragment) {
        this.iTransmissionAllSongsFragment = iTransmissionAllSongsFragment;
    }
    public MediaPlaybackFragment(){

    }

    private MainActivity getActivityMusic() {
        if (getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        }
        return null;
    }

    public void setData() {
        mMediaPlaybackService = getActivityMusic().getMediaPlaybackService();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setData();
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
        if (getArguments() != null) {
            setText(getArguments());
        }
        Popmenu();

        if (mMediaPlaybackService != null) {
            mSeekBar.setMax(mUpdateUI.getDuration());
            updateUI();
            updateTime();
        }
        if (isLandscape()) {
            if (mQueue.getVisibility() == View.VISIBLE)
                mQueue.setVisibility(View.INVISIBLE);
            mImageBig.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
        mQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iTransmissionAllSongsFragment.onClickTransmissionAllSongsFragment();

            }
        });
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
                    mMediaPlaybackService.seekToPos(progress);
                }
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                mTimeSong1.setText(formatTime.format(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaPlaybackService.getmMediaPlayer().seekTo(seekBar.getProgress());
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
                .error(R.drawable.icon_music_replace)
                .into(mPictureSmall);
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.icon_music_replace)
                .into(mImageBig);
        mSeekBar.setMax(mMediaPlaybackService.getDuration());
    }

    public void setText(Bundle bundle) {
        mNameSong.setText(bundle.getString("song"));
        mArtist.setText(bundle.getString("artist1"));
        mTimeSong2.setText(bundle.getString("song1"));
        byte[] songArt = getAlbumArt(bundle.getString("song2"));
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.icon_music_replace)
                .into(mPictureSmall);
        Glide.with(view.getContext()).asBitmap()
                .load(songArt)
                .error(R.drawable.icon_music_replace)
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
                Log.d("values", "onMenuItemClick: " + values);
                getContext().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + mListSong.get(mMediaPlaybackService.getPossision()).getID(), null);
                Toast.makeText(getContext(), "addFavorite song //" + mListSong.get(mMediaPlaybackService.getPossision()).getTitle(), Toast.LENGTH_SHORT).show();
                mMediaPlaybackService.getListSong().get(mMediaPlaybackService.getPossision()).setFavorite(true);
                return true;
            case R.id.remove_song_favorite:
                ContentValues values1 = new ContentValues();
                values1.put(FavoriteSongsProvider.IS_FAVORITE, 1);
                values1.put(FavoriteSongsProvider.COUNT_OF_PLAY, 0);
                getContext().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values1, FavoriteSongsProvider.ID_PROVIDER + "= " + mListSong.get(mMediaPlaybackService.getPossision()).getID(), null);
                Toast.makeText(getContext(), "removeFavorite song //" + mListSong.get(mMediaPlaybackService.getPossision()).getTitle(), Toast.LENGTH_SHORT).show();
                mMediaPlaybackService.getListSong().get(mMediaPlaybackService.getPossision()).setFavorite(false);
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
        mMediaPlaybackService.setPossition(mUpdateUI.getIndex());

        switch (view.getId()) {
            case R.id.like:
                mListSong.get(mMediaPlaybackService.getPossision()).setFavorite(true);
                mLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_default);
                ContentValues values = new ContentValues();
                values.put(FavoriteSongsProvider.IS_FAVORITE, 2);
                getContext().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + (mListSong.get(mMediaPlaybackService.getPossision()).getID()), null);
                Toast.makeText(getContext(), "addFavorite song //" + mListSong.get(mMediaPlaybackService.getPossision()).getTitle(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.play_previous: {
                mMediaPlaybackService.previousMedia();
                getText(mListSong.get(mMediaPlaybackService.getPossision()));
                break;
            }
            case R.id.play_pause_media: {
                if (mMediaPlaybackService.getPlaying()) {
                    mMediaPlaybackService.pauseMedia();
                    mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    mMediaPlaybackService.setPlaying(false);
                } else {

                    mMediaPlaybackService.setPlaying(true);

                    if (mMediaPlaybackService != null) {
                        if (mMediaPlaybackService.isResume()) {
                            mMediaPlaybackService.resumeMedia();
                        } else {
                            try {
                                mMediaPlaybackService.playMedia(mListSong.get(mUpdateUI.getIndex()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    setCheck(true);
                }
                mMediaPlaybackService.showNotification(mMediaPlaybackService.getNameSong(), mMediaPlaybackService.getArtist(), mMediaPlaybackService.getFile());
                break;
            }
            case R.id.play_next: {
                mMediaPlaybackService.nextMedia();
                Log.d("mediaPlaybackService", "onClick: " + mMediaPlaybackService.getPossision());
                Log.d("mediaPlaybackService", "onClick: " + mMediaPlaybackService);
                getText(mListSong.get(mMediaPlaybackService.getPossision()));
                break;
            }
            case R.id.dislike:
                mListSong.get(mMediaPlaybackService.getPossision()).setFavorite(false);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_selected);
                mLike.setImageResource(R.drawable.ic_thumbs_up_default);
                ContentValues values1 = new ContentValues();
                values1.put(FavoriteSongsProvider.IS_FAVORITE, 1);
                values1.put(FavoriteSongsProvider.COUNT_OF_PLAY, 0);
                getContext().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values1, FavoriteSongsProvider.ID_PROVIDER + "= " + (mListSong.get(mMediaPlaybackService.getPossision()).getID()), null);
                Toast.makeText(getContext(), "removeFavorite song //" + mListSong.get(mMediaPlaybackService.getPossision()).getTitle(), Toast.LENGTH_SHORT).show();

                break;
            case R.id.shuffle: {
                if (!mMediaPlaybackService.getShuffle()) {
                    mShuffle.setImageResource(R.drawable.ic_play_shuffle_orange);
                    mMediaPlaybackService.setShuffle(true);
                    mUpdateUI.UpdateShuffler(true);
                } else {
                    mShuffle.setImageResource(R.drawable.ic_shuffle_white);
                    mMediaPlaybackService.setShuffle(false);
                    mUpdateUI.UpdateShuffler(false);
                }
                break;
            }
            case R.id.repeat: {
                if (mMediaPlaybackService.getRepeat() == 1) {
                    mRepeat.setImageResource(R.drawable.ic_repeat_white);
                    mMediaPlaybackService.setRepeat(-1);
                    mUpdateUI.UpdateRepeat(-1);
                } else if (mMediaPlaybackService.getRepeat() == -1) {
                    mRepeat.setImageResource(R.drawable.ic_repeat_dark_selected);
                    mMediaPlaybackService.setRepeat(0);
                    mUpdateUI.UpdateRepeat(0);
                } else {
                    mRepeat.setImageResource(R.drawable.ic_repeat_one_song_dark);
                    mMediaPlaybackService.setRepeat(1);
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

    public void checkFavorite() {
        Log.d("checkFavorite", "checkFavorite: " + mListSong);
        if (mMediaPlaybackService != null)
            if (mListSong.get(mMediaPlaybackService.getPossision()).getFavorite())
                mIsFavorite = true;
            else mIsFavorite = false;
    }

    public void updateUI() {
        if (mMediaPlaybackService != null && mSeekBar != null) {
            updateTime();
            checkFavorite();
            mSeekBar.setMax(mMediaPlaybackService.getDuration());
            mNameSong.setText(mMediaPlaybackService.getNameSong());
            mArtist.setText(mMediaPlaybackService.getArtist());
            byte[] songArt = getAlbumArt(mMediaPlaybackService.getFile());
            Glide.with(view.getContext()).asBitmap()
                    .load(songArt)
                    .error(R.drawable.icon_music_replace)
                    .into(mPictureSmall);
            Glide.with(view.getContext()).asBitmap()
                    .load(songArt)
                    .error(R.drawable.icon_music_replace)
                    .into(mImageBig);
            SimpleDateFormat formmatTime = new SimpleDateFormat("mm:ss");
            if (mMediaPlaybackService.getDuration() != 0)
                mTimeSong2.setText(formmatTime.format(mMediaPlaybackService.getDuration()));
            else {
                mTimeSong2.setText(formmatTime.format(Integer.valueOf(mListSong.get(mUpdateUI.getIndex()).getDuration())));
            }
            if (mMediaPlaybackService.getPlaying() == true) {
                mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            } else {
                mPlayPauseMedia.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
            }

            if (mMediaPlaybackService.getShuffle()) {
                mShuffle.setBackgroundResource(R.drawable.ic_play_shuffle_orange);
            } else
                mShuffle.setBackgroundResource(R.drawable.ic_shuffle_white);

            if (mMediaPlaybackService.getRepeat() == 0) {
                mRepeat.setBackgroundResource(R.drawable.ic_repeat_dark_selected);
            } else if (mMediaPlaybackService.getRepeat() == -1) {
                mRepeat.setBackgroundResource(R.drawable.ic_repeat_white);
            } else mRepeat.setBackgroundResource(R.drawable.ic_repeat_one_song_dark);
            if (mIsFavorite) {
                mLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_default);
            } else {
                mLike.setImageResource(R.drawable.ic_thumbs_up_default);
                mDisLike.setImageResource(R.drawable.ic_thumbs_down_selected);
            }

        }
    }


    public void updateTime() {
        final Handler handler = new Handler();
        if (song != null || mListSong != null)
            handler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                    if (!mMediaPlaybackService.isResume()) {
                        mTimeSong1.setText(formatTime.format(0));
                        mSeekBar.setProgress(0);
                    } else {
                        mTimeSong1.setText(formatTime.format(mMediaPlaybackService.getCurrentStreamPosition()));
                        mSeekBar.setProgress(mMediaPlaybackService.getCurrentStreamPosition());
                    }

                    if (mMediaPlaybackService.isPlaying()) {
                        mTimeSong1.setText(formatTime.format(mMediaPlaybackService.getCurrentStreamPosition()));
                        mSeekBar.setProgress(mMediaPlaybackService.getCurrentStreamPosition());
                    }
                    mMediaPlaybackService.getmMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            try {
                                if (mMediaPlaybackService.getPlaying() == false) {
                                } else mMediaPlaybackService.onCompletionSong();
                                mSeekBar.setMax(mMediaPlaybackService.getDuration());
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
