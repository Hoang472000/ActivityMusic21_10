package com.out.activitymusic.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class FavoriteSongsProvider extends ContentProvider {

    private static final String DB_SONGS = "db_songs";
    public static final String AUTHORITY = "com.out.activitymusic.database.FavoriteSongsProvider";
    private static final int DB_VESION = 1;
    static final String CONTENT_PATH = "favoritesongs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_PATH);

    private static final String TABLE_FAVORITESONGS = "favoritesongs";
    static final String ID = "id";
    public static final String ID_PROVIDER ="id_provider";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String COUNT_OF_PLAY = "count_of_play";
    static final String CREATE_TABLE_FAVORITESONGS =
            "create table " + TABLE_FAVORITESONGS + "( "+ ID +" integer primary key autoincrement," +
                    ID_PROVIDER + " integer ,"+
                    IS_FAVORITE + " integer default 0, " + //  0 : not like // 1 : stop like // 2 : like
                    COUNT_OF_PLAY + " integer default 0  );"; // number click // if count =3 => is_favorite=1 expect  is_favorite=1

    private static HashMap<String, String> HASMAP;
    private static UriMatcher sUriMatcher;
    private static final int URI_ALL_ITEM_CODE = 1;
    private static final int URI_ONE_ITEM_CODE = 2;
    private SQLiteDatabase database;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, CONTENT_PATH, URI_ALL_ITEM_CODE);
        sUriMatcher.addURI(AUTHORITY, CONTENT_PATH + "/#", URI_ONE_ITEM_CODE);
    }

    private static class FavoriteSongsDatabase extends SQLiteOpenHelper {


        public FavoriteSongsDatabase(@Nullable Context context) {
            super(context, DB_SONGS, null, DB_VESION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //sqLiteDatabase.execSQL(CREATE_TABLE_LISTSONGS);
            sqLiteDatabase.execSQL(CREATE_TABLE_FAVORITESONGS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITESONGS);
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public boolean onCreate() {

        FavoriteSongsDatabase mFavoriteSongsDatabase = new FavoriteSongsDatabase(getContext());
        database = mFavoriteSongsDatabase.getWritableDatabase();
        if (database == null)
            return false;
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String seclection, String[] seclectionArg, String orderBy) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_FAVORITESONGS);
        switch (sUriMatcher.match(uri)) {
            case URI_ALL_ITEM_CODE:
                queryBuilder.setProjectionMap(HASMAP);
                break;
            case URI_ONE_ITEM_CODE:
                queryBuilder.appendWhere(ID +"="+uri.getPathSegments().get(1));
                break;
            default:
       //         throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (orderBy == null || orderBy == "") {
            orderBy = ID;
        }
        Cursor cursor = queryBuilder.query(database, projection, seclection, seclectionArg, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case URI_ALL_ITEM_CODE:
                return "vnd.android.cursor.dir/vnd.com.out.activitymusic.database." + CONTENT_PATH;
            case URI_ONE_ITEM_CODE:
                return "vnd.android.cursor.item/vnd.com.out.activitymusic.database." + CONTENT_PATH;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long rowID = database.insert(TABLE_FAVORITESONGS, "", contentValues);

        if (rowID > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Failed to add a record into " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] slectionArg) {
        int count = 0;
        switch (sUriMatcher.match(uri)){
            case URI_ALL_ITEM_CODE:
                // Truong hop xoa toan bo notes
                count = database.delete(TABLE_FAVORITESONGS, selection, slectionArg);
                break;

            case URI_ONE_ITEM_CODE:
                // Truong hop xoa 1 note
                String id = uri.getPathSegments().get(1);
                count = database.delete( TABLE_FAVORITESONGS, ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), slectionArg);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // Notify cho cac thanh phan lang nghe
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArg) {
        //  return mFavoriteSongsDatabase.update(contentValues, selection, selectionArg);
        Log.d("ID_PROVIDER", "update: ");
        int count =0;
        switch (sUriMatcher.match(uri)){
            case URI_ALL_ITEM_CODE:
                count= database.update(TABLE_FAVORITESONGS, contentValues, selection,selectionArg);
                Log.d("ID_PROVIDER", "update:URI_ALL_ITEM_CODE "+count);
                break;
            case URI_ONE_ITEM_CODE:
                count= database.update(TABLE_FAVORITESONGS,contentValues,
                        ID +" = "+uri.getPathSegments().get(1)+(!TextUtils.isEmpty(selection)?"AND ("+selection +')':""),selectionArg);
                Log.d("ID_PROVIDER", "update:URI_ONE_ITEM_CODE "+count);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
