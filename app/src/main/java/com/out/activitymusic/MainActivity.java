package com.out.activitymusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.out.activitymusic.interfaces.IDataFavorite;
import com.out.activitymusic.interfaces.IDataFragment;
import com.out.activitymusic.interfaces.IDisplayMediaFragment;

import java.util.ArrayList;

import Service.MediaPlaybackService;

public class MainActivity extends AppCompatActivity implements IDisplayMediaFragment, IDataFragment, IDataFavorite, NavigationView.OnNavigationItemSelectedListener {
    public static IntentFilter Broadcast_PLAY_NEW_AUDIO;
    String PRIVATE_MODE = "color";
    AllSongsFragment allSongsFragment;
    BaseSongListFragment baseSongListFragment;
    MediaPlaybackFragment mediaPlaybackFragment;
    private FavoriteSongsFragment mFavoriteSongsFragment;
    public MediaPlaybackService mediaPlaybackService;
    boolean serviceBound = false;
    private ArrayList<Song> mListSong;
    SharedPreferences sharedPreferences;
    private DrawerLayout mDrawerLayout;
    private UpdateUI mUpdateUI;
    public static final String SHARED_PREFERENCES_NAME="com.out.activitymusic";


    public MediaPlaybackService getMediaPlaybackService() {
        return mediaPlaybackService;
    }

    private boolean isFavorite = false;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HoangCVonStart", "onCreate: ");

        if (savedInstanceState != null) {
            isFavorite = savedInstanceState.getBoolean("IS_FAVORITE");
        }
        setContentView(R.layout.activity_main);
        initPermission();
        mUpdateUI = new UpdateUI(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        if (drawer != null)
            drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null)
            navigationView.setNavigationItemSelectedListener(this);
        mediaPlaybackFragment = new MediaPlaybackFragment();
        allSongsFragment = new AllSongsFragment(this, this, this.mediaPlaybackFragment);
        mFavoriteSongsFragment = new FavoriteSongsFragment(null, mediaPlaybackService, mediaPlaybackFragment, this, this);
        if (mediaPlaybackService != null)
            mFavoriteSongsFragment.setLisSong(mediaPlaybackService.getListSong());
        Log.d("HoangCVmediaPlaybackService", "onCreate: "+mediaPlaybackService);
        allSongsFragment.setAllSong(mFavoriteSongsFragment);
        if (!isLandScape()) {
            if (isFavorite) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentSongOne, mFavoriteSongsFragment).commit();
                mFavoriteSongsFragment.setLisSong(mListSong);
                mDrawerLayout = findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                FragmentManager manager = this.getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.fragmentSongOne, allSongsFragment).commit();
            }
        } else {
            if (!isFavorite) {
                FragmentManager manager = this.getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.fragmentSongOne, allSongsFragment).commit();
                FragmentManager manager1 = this.getSupportFragmentManager();
                manager1.beginTransaction().replace(R.id.fragmentMediaTwo, mediaPlaybackFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentSongOne, mFavoriteSongsFragment).commit();
                mDrawerLayout = findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                FragmentManager manager = this.getSupportFragmentManager();
                manager.beginTransaction().addToBackStack(null).replace(R.id.fragmentMediaTwo, mediaPlaybackFragment).commit();
            }
        }
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mediaPlaybackService = binder.getService();
            if (mListSong != null) mediaPlaybackService.setListSong(mListSong);
            iConnectActivityAndBaseSong.connectActivityAndBaseSong();
            serviceBound = true;
            allSongsFragment.setService(mediaPlaybackService);
            mediaPlaybackFragment.setService(mediaPlaybackService);
            if (isLandScape()) {
                allSongsFragment.setService(mediaPlaybackService);
                mediaPlaybackService.setmMediaPlaybackFragment(mediaPlaybackFragment);
                mediaPlaybackFragment.setService(mediaPlaybackService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onStart() {
        startService();
        super.onStart();
    }

    public void startService() {
        Intent intent = new Intent(this, MediaPlaybackService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onclickIDisplay(Song song) {
        mediaPlaybackFragment = new MediaPlaybackFragment().newInstance(song);
        FragmentManager manager1 = this.getSupportFragmentManager();
        manager1.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragmentSongOne, mediaPlaybackFragment)
                .commit();
        mediaPlaybackFragment.setListSong(mListSong);
        //mediaPlaybackFragment.setService(mediaPlaybackService);
        mediaPlaybackFragment.updateUI();
        if (mediaPlaybackService != null) mediaPlaybackFragment.updateTime();
        mediaPlaybackService.setmMediaPlaybackFragment(mediaPlaybackFragment);
        mediaPlaybackService.setListSong(mListSong);
        getSupportActionBar().hide();

    }

    @Override
    public void onclickIData(ArrayList ListSong) {
        if (!isFavorite) {
            this.mListSong = ListSong;
        }
    }

    public void setService(MediaPlaybackService service) {
        this.mediaPlaybackService = service;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.favorite) {
            isFavorite = true;
            mFavoriteSongsFragment = new FavoriteSongsFragment(mediaPlaybackService.getListSong(), mediaPlaybackService, mediaPlaybackFragment, this, this);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentSongOne, mFavoriteSongsFragment).commit();
            mDrawerLayout = findViewById(R.id.drawer_layout);
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.List_music) {
            isFavorite = false;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentSongOne, allSongsFragment).commit();
            mDrawerLayout = findViewById(R.id.drawer_layout);
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        sharedPreferences =getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is favorite",isFavorite);
        editor.commit();
        mediaPlaybackFragment.setIsFavorite(isFavorite);
        return true;

    }

    @Override
    public void onClickIDataFavorite(ArrayList arrayList) {
        if (isFavorite) {
            this.mListSong = arrayList;
        }
    }

    //Bkav Nhungltk
    interface IConnectActivityAndBaseSong {
        void connectActivityAndBaseSong();
    }

    public boolean isLandScape() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return true;
        else return false;
    }

    private IConnectActivityAndBaseSong iConnectActivityAndBaseSong;

    public void setiConnectActivityAndBaseSong(IConnectActivityAndBaseSong iConnectActivityAndBaseSong) {
        this.iConnectActivityAndBaseSong = iConnectActivityAndBaseSong;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permision Write File is Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Permision Write File is Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(MainActivity.this, "Permission isn't granted ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permisson don't granted and dont show dialog again ", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("HoangCVonStart", "onBackPressed: ");
        getSupportActionBar().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("HoangCVonStart", "onResume: ");
    }

    @Override
    protected void onPause() {
        Log.d("HoangCVonStart", "onPause: ");
        super.onPause();
        setService(mediaPlaybackService);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("IS_FAVORITE", isFavorite);
        super.onSaveInstanceState(outState);
    }
}