package com.out.activitymusic;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.out.activitymusic.database.FavoriteSongsProvider;
import com.out.activitymusic.interfaces.IItemClickListener;

import java.util.ArrayList;

import Service.MediaPlaybackService;
import es.claucookie.miniequalizerlibrary.EqualizerView;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements Filterable {
    private ArrayList<Song> mListSong;
    private ArrayList<Song> listSongFull;
    private LayoutInflater mInflater;
    private View playMediaSong;
    RelativeLayout mLinearLayout;
    AllSongsFragment allSongsFragment;
    private Context mContext;
    private IItemClickListener IItemClickListener;
    private ImageView image;
    private ImageView mPlayPause;
    private int mPosision, mPos;
    UpdateUI mUpdateUI;
    public MediaPlaybackService mediaPlaybackService;

    public void setService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }

    public ListAdapter() {
    }

    public ListAdapter(Context context, ArrayList<Song> ListView, IItemClickListener IItemClickListener) {
        mInflater = LayoutInflater.from(context);
        this.IItemClickListener = IItemClickListener;
        this.mListSong = ListView;
        listSongFull = new ArrayList<>(ListView);
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.list_view, parent, false);
        playMediaSong = mInflater.inflate(R.layout.allsongsfragment, parent, false);
        mLinearLayout = playMediaSong.findViewById(R.id.bottom);
        mPlayPause = playMediaSong.findViewById(R.id.play_pause);
        allSongsFragment = new AllSongsFragment();
        image = (ImageView) mItemView.findViewById(R.id.more_vert);
        mUpdateUI = new UpdateUI(mContext);
        mPos = mUpdateUI.getCurrentPossision();
        Log.d("HoangCV6", "onCreateViewHolder: ");
        return new ViewHolder(mItemView, this);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Song mCurrent = mListSong.get(position);
        holder.mId.setText((position + 1) + "");
        holder.mNameSong.setText(mCurrent.getTitle());
        holder.mDuration.setText(getDurationTime(mCurrent.getDuration()));
        holder.mMore.setImageResource(R.drawable.ic_more_vert);
        Log.d("HoangCV555", "onBindViewHolder: " + mediaPlaybackService);
        if (mediaPlaybackService != null) {
            if ((mediaPlaybackService.getNameSong()).equals(mListSong.get(position).getTitle()) == true) {
                Log.d("HoangCV12345", "onBindViewHolder: holder: " + holder);
                holder.mId.setVisibility(View.INVISIBLE);
                holder.mNameSong.setTypeface(null, Typeface.BOLD);
                holder.mEqualizer.animateBars();
                if (!mediaPlaybackService.getPlaying()) holder.mEqualizer.stopBars();
                holder.mEqualizer.setVisibility(View.VISIBLE);
            } else {
                holder.mId.setVisibility(View.VISIBLE);
                holder.mNameSong.setTypeface(null, Typeface.NORMAL);
                holder.mEqualizer.setVisibility(View.INVISIBLE);
            }
        }
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.mMore);
                popupMenu.inflate(R.menu.poupup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.add_song_favorite:
                                ContentValues values = new ContentValues();
                                values.put(FavoriteSongsProvider.IS_FAVORITE, 2);
                                mContext.getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + mListSong.get(mPosision).getID(), null);
                                Toast.makeText(mContext, "addFavorite song //" + mListSong.get(mPosision).getTitle(), Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.remove_song_favorite:
                                ContentValues values1 = new ContentValues();
                                values1.put(FavoriteSongsProvider.IS_FAVORITE, 1);
                                values1.put(FavoriteSongsProvider.COUNT_OF_PLAY, 0);
                                mContext.getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values1, FavoriteSongsProvider.ID_PROVIDER + "= " + mListSong.get(mPosision).getID(), null);
                                Toast.makeText(mContext, "removeFavorite song //" + mListSong.get(mPosision).getTitle(), Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Song> filterdList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filterdList.addAll(listSongFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Song item : listSongFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        filterdList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterdList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mListSong.clear();
            mListSong.addAll((ArrayList) filterResults.values);
            notifyDataSetChanged();
        }
    };

    private String getDurationTime(String str) {
        int mili = Integer.parseInt(str) / 1000;
        int phut = mili / 60;
        int giay = mili % 60;
        if (giay < 10)
            return String.valueOf(phut) + ":0" + String.valueOf(giay);
        else return String.valueOf(phut) + ":" + String.valueOf(giay);
    }

    @Override
    public int getItemCount() {
        Log.d("HoangC1Vff", "getItemCount: " + mListSong.size());
        return mListSong.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public EqualizerView mEqualizer;
        public TextView mNameSong;
        public TextView mDuration;
        final ListAdapter mAdapter;
        public TextView mId;
        public ImageView mMore;
        private UpdateUI mUpdateUI;

        public ViewHolder(@NonNull View itemView, ListAdapter adapter) {
            super(itemView);
            this.mAdapter = adapter;
            mEqualizer = itemView.findViewById(R.id.equalizer);
            mId = itemView.findViewById(R.id.STT);
            mNameSong = itemView.findViewById(R.id.item_name_song);
            mDuration = itemView.findViewById(R.id.item_time_song);
            mMore = itemView.findViewById(R.id.item_more_vert);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mPosision = Integer.parseInt(String.valueOf(mId.getText())) - 1;
            IItemClickListener.onClickIItem(mListSong.get(mPosision));
            notifyDataSetChanged();
        }
    }

}


