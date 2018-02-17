package com.keyeswest.movies.repos;


import android.net.Uri;

import com.keyeswest.movies.models.Movie;

import java.util.List;

public class SqlResult {

    private List<Movie> mMovies;

    private int mCount;

    private Uri mResultUri;

    public SqlResult(List<Movie> movies){
        mMovies = movies;
        mCount = 0;
        mResultUri = null;
    }

    public SqlResult(Uri uri){
        mResultUri = uri;
    }

    public List<Movie> getMovies() {
        return mMovies;
    }

    public void setMovies(List<Movie> movies) {
        this.mMovies = movies;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public Uri getResultUri() {
        return mResultUri;
    }

    public void setResultUri(Uri resultUri) {
        this.mResultUri = resultUri;
    }
}