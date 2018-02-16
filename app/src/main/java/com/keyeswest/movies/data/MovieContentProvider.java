package com.keyeswest.movies.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.keyeswest.movies.data.MovieContract.MovieTable.TABLE_NAME;

public class MovieContentProvider  extends ContentProvider{

    //MOVIE represents the directory of movies
    public static final int MOVIE_DIRECTORY = 100;
    public static final int MOVIE_WITH_ID = 101;


    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE, MOVIE_DIRECTORY);
        matcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        return matcher;
    }

    private MovieBaseHelper mMovieBaseHelper;


    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMovieBaseHelper = new MovieBaseHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = mMovieBaseHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match){
            case MOVIE_DIRECTORY:
                cursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                //return null;

                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final SQLiteDatabase db = mMovieBaseHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch(match){
            case MOVIE_DIRECTORY:

                long movieId = db.insert(TABLE_NAME, null, values);
                if (movieId > 0){
                    //success
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieTable.CONTENT_URI, movieId);
                }
                else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
                default:
                    throw new UnsupportedOperationException("Unsupported insert operation.");
        }

        // Notify the resolver that the uri has changed
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = mMovieBaseHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int moviesDeleted = 0;

        switch(match){
            case MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                moviesDeleted = db.delete(TABLE_NAME, "movieId=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (moviesDeleted != 0){
            // A movie was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return moviesDeleted;


    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
