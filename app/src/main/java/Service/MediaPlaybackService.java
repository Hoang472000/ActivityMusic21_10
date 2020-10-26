package Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.out.activitymusic.BaseSongListFragment;
import com.out.activitymusic.MainActivity;
import com.out.activitymusic.MediaPlaybackFragment;
import com.out.activitymusic.R;
import com.out.activitymusic.Song;
import com.out.activitymusic.UpdateUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MediaPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {
    private static final String NOTIFICATION_CHANNEL_ID = "1";
    public static final String ACTION_PERVIOUS = "xxx.yyy.zzz.ACTION_PERVIOUS";
    public static final String ACTION_PLAY = "xxx.yyy.zzz.ACTION_PLAY";
    public static final String ACTION_NEXT = "xxx.yyy.zzz.ACTION_NEXT";
    private UpdateUI mUpdateUI;
    private MediaPlayer mMediaPlayer;
    private String mMediaFile;
    private int mResumePosition;
    private AudioManager mAudioManager;
    private final IBinder iBinder = new LocalBinder();
    private SeekBar seekBar;
    private MediaPlayer mPlayer;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private BaseSongListFragment baseSongListFragment;
    private int mCurrentPlay;
    private String mTitle = "";
    private String mArtistt = "";
    private String mPotoMusic = "";
    private String mFile = "";
    private int mCurrentPosition = 0;
    private boolean mIsFavorite;
    private int possition;
    private ArrayList<Integer> favoriteID;

    public boolean isResume() {
        return isResume;
    }


    public void setPossition(int possition) {
        this.possition = possition;
    }

    public int getPossision() {
        return possition;
    }

    public void setResume(boolean resume) {
        isResume = resume;
    }

    private boolean isResume;

    public ArrayList<Integer> getfavoriteID() {
        return favoriteID;
    }

    public void setFavoriteID(ArrayList<Integer> favoriteID, boolean mIsFavorite) {
        this.favoriteID = favoriteID;
        this.mIsFavorite = mIsFavorite;
    }

    private SharedPreferences mSharePreferences;
    private static final String SHARED_PREFERENCES_NAME = "com.out.activitymusic";

    public void setListSong(ArrayList<Song> mListSong) {
        this.mListSong = mListSong;
    }

    private ArrayList<Song> mListSong = new ArrayList<>();

    public ArrayList<Song> getListSong() {
        return mListSong;
    }

    public void setmMediaPlaybackFragment(MediaPlaybackFragment mMediaPlaybackFragment) {
        this.mMediaPlaybackFragment = mMediaPlaybackFragment;
    }

    public void setBaseSongListFragment(BaseSongListFragment baseSongListFragment) {
        this.baseSongListFragment = baseSongListFragment;
    }

    public String getNameSong() {
        return mTitle;
    }

    public String getArtist() {
        return mArtistt;
    }

    public String getPotoMusic() {
        return mPotoMusic;
    }

    public String getFile() {
        return mFile;
    }


    @Override
    public void onCreate() {
        Log.d("nhungltk123", "onCreate: ");
        mMediaPlayer = new MediaPlayer();
        mUpdateUI = new UpdateUI(getApplicationContext());
        mTitle = mUpdateUI.getTitle();
        mArtistt = mUpdateUI.getArtist();
        mPotoMusic = mUpdateUI.getAlbum();
        mFile = mUpdateUI.getFile();
        mSharePreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mCurrentPosition = mSharePreferences.getInt("currentPosision", 0);
        rand = new Random();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RemoteViews mNotification = new RemoteViews(getPackageName(), R.layout.notification);
        Log.d("nhungltk123", "onStartCommand: `");
        try {
            mMediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
        }
        if (requestAudioFocus() == false) {
        }
        if (mMediaFile != null && mMediaFile != "")
            initMediaPlayer();
        if ((mMediaPlayer != null) && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PERVIOUS:
                    previousMedia();
                    break;
                case ACTION_NEXT:
                    nextMedia();
                    break;
                case ACTION_PLAY:
                    if (mMediaPlayer.isPlaying()) {
                        pauseMedia();
                        //       mNotification.setImageViewResource(R.id.play_ntf,R.drawable.ic_baseline_play_circle_filled_24);
                    } else {
                        resumeMedia();
                        //       mNotification.setImageViewResource(R.id.play_ntf,R.drawable.ic_baseline_pause_circle_filled_24);
                    }

                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onCompletionSong() throws IOException {
        mMediaPlayer.pause();
        int rtpos = possition;
        if (shuffle && repeat == -1) {
            possition = rand.nextInt(mListSong.size());
        } else if (shuffle && repeat == 0) possition++;
        else if (!shuffle && repeat != 1) possition++;
        else if (repeat == 1) possition = rtpos;
        if ((possition >= mListSong.size()) && (repeat == 0)) possition = 0;
        if ((possition > mListSong.size() - 1) && ((repeat == -1) || (!shuffle))) pauseMedia();
        else
            try {
                playMedia(mListSong.get(possition));
            } catch (IOException e) {
                e.printStackTrace();
            }
        mMediaPlaybackFragment.getText(mListSong.get(possition));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    public int getCurrentStreamPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seekToPos(int i) {
        mMediaPlayer.seekTo(i);
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mMediaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
    }

    public void nextMedia() {
        int rtpos = possition;
        if (shuffle && repeat == -1) {
            possition = rand.nextInt(mListSong.size());
        } else if (shuffle && repeat == 0) possition++;
        else if (!shuffle && repeat != 1) possition++;
        else if (repeat == 1) possition = rtpos;
        if ((possition >= mListSong.size()) && (repeat == 0)) possition = 0;
        if ((possition > mListSong.size() - 1) && ((repeat == -1) || (!shuffle))) pauseMedia();
        else
            try {
                playMedia(mListSong.get(possition));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void previousMedia() {
        int rtpos = possition;
        if (shuffle && repeat == -1) {
            possition = rand.nextInt(mListSong.size());
        } else if (shuffle && repeat == 0) possition--;
        else if (!shuffle && repeat != 1) possition--;
        else if (repeat == 1) possition = rtpos;
        if ((possition < 0) && (repeat == 0)) possition = mListSong.size() - 1;
        if ((possition < 0) && ((!shuffle) || (repeat != -1))) pauseMedia();
        else try {
            playMedia(mListSong.get(possition));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playMedia(Song song) throws IOException {
        possition = song.getID() - 1;
        if (mMediaPlayer != null)
            mMediaPlayer.reset();
        MediaPlayer mMediaPlayer = new MediaPlayer();
        Uri uri = Uri.parse(song.getFile());
        mMediaPlayer.setDataSource(getApplicationContext(), uri);
        mMediaPlayer.prepare();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //initSong(song);
        mMediaPlayer.start();
        this.mMediaPlayer = mMediaPlayer;
        mCurrentPlay = song.getID();
        mTitle = song.getTitle();
        mArtistt = song.getArtist();
        mPotoMusic = song.getAlbum();
        mFile = song.getFile();
        showNotification(mTitle, mArtistt, mFile);
        mUpdateUI = new UpdateUI(getApplicationContext());
        mUpdateUI.UpdateTitle(song.getTitle());
        mUpdateUI.UpdateIndex(possition);
        mUpdateUI.UpdateArtist(song.getArtist());
        mUpdateUI.UpdateFile(song.getFile());
        mUpdateUI.UpdateAlbum(String.valueOf(queryAlbumUri(song.getAlbum())));
        mUpdateUI.UpdateDuration(this.mMediaPlayer.getDuration());
        mUpdateUI.UpdateCurrentPossision(this.mMediaPlayer.getCurrentPosition());
        mUpdateUI.UpdateIsPlaying(this.mMediaPlayer.isPlaying());
        isPlaying = mMediaPlayer.isPlaying();
    }

    public Uri queryAlbumUri(String imgUri) {
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, Long.parseLong(imgUri));//noi them mSrcImageSong vao artworkUri
    }

    private void stopMedia() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void pauseMedia() {

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
            isResume = true;
        }
        mUpdateUI.UpdateIsPlaying(this.mMediaPlayer.isPlaying());
        showNotification(mTitle, mArtistt, mFile);
        this.stopForeground(STOP_FOREGROUND_DETACH);
    }

    public void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
        }
    }

    private boolean shuffle = false;
    private int repeat = -1;
    private Random rand;

    public boolean getShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void setPlaying(boolean isplaysing) {
        this.isPlaying = isplaysing;
    }

    boolean isPlaying = false;

    public boolean getPlaying() {
        return isPlaying;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void showNotification(String nameSong, String nameArtist, String path) {
        createNotificationChanel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Intent previousIntent = new Intent(this, MediaPlaybackService.class);
        previousIntent.setAction(ACTION_PERVIOUS);
        PendingIntent previousPendingIntent = null;
        Intent playIntent = new Intent(this, MediaPlaybackService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = null;
        Intent nextIntent = new Intent(this, MediaPlaybackService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            previousPendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nextPendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        RemoteViews mSmallNotification = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews mNotification = new RemoteViews(getPackageName(), R.layout.notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon_music);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setCustomContentView(mSmallNotification);
        builder.setCustomBigContentView(mNotification);
        builder.setContentIntent(pendingIntent);

        mNotification.setTextViewText(R.id.title_ntf, nameSong);
        mNotification.setTextViewText(R.id.artist_ntf, nameArtist);
        mNotification.setOnClickPendingIntent(R.id.previous_ntf, previousPendingIntent);
        mNotification.setOnClickPendingIntent(R.id.next_ntf, nextPendingIntent);
        mNotification.setOnClickPendingIntent(R.id.play_ntf, playPendingIntent);
        mNotification.setImageViewResource(R.id.previous_ntf, R.drawable.ic_rew_dark);
        mNotification.setImageViewResource(R.id.next_ntf, R.drawable.ic_fwd_dark);
        mNotification.setImageViewResource(R.id.play_ntf, getPlaying() ? R.drawable.ic_baseline_pause_circle_filled_24 : R.drawable.ic_baseline_play_circle_filled_24);
        if (getAlbumn(path) != null) {
            mNotification.setImageViewBitmap(R.id.img_ntf, getAlbumn(path));
        } else {
            mNotification.setImageViewResource(R.id.img_ntf, R.drawable.icon_music_replace);
        }
        mSmallNotification.setOnClickPendingIntent(R.id.play_smallntf, playPendingIntent);
        mSmallNotification.setOnClickPendingIntent(R.id.previous_smallntf, previousPendingIntent);
        mSmallNotification.setOnClickPendingIntent(R.id.next_smallntf, nextPendingIntent);
        mSmallNotification.setImageViewResource(R.id.previous_ntf, R.drawable.ic_rew_dark);
        mSmallNotification.setImageViewResource(R.id.next_ntf, R.drawable.ic_fwd_dark);
        mSmallNotification.setImageViewResource(R.id.play_smallntf, getPlaying() ? R.drawable.ic_baseline_pause_circle_filled_24 : R.drawable.ic_baseline_play_circle_filled_24);
        if (getAlbumn(path) != null) {
            mSmallNotification.setImageViewBitmap(R.id.img_ntf_small, getAlbumn(path));
        } else {
            mSmallNotification.setImageViewResource(R.id.img_ntf_small, R.drawable.icon_music_replace);
        }

        startForeground(1, builder.build());
        if (mMediaPlaybackFragment != null) mMediaPlaybackFragment.updateUI();
        if (isLandscape())
            if (baseSongListFragment != null) baseSongListFragment.updateUIWhenLandScape();
            else if (baseSongListFragment != null) baseSongListFragment.updateUI();
    }

    public void createNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "mUSIC SERVICE CHANNEL",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    public Bitmap getAlbumn(String path) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(path);
        byte[] data = metadataRetriever.getEmbeddedPicture();
        return data == null ? null : BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public boolean isLandscape() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return true;
        else return false;
    }

}
