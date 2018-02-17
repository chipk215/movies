package com.keyeswest.movies.data;


import android.database.Cursor;
import android.database.CursorWrapper;

import com.keyeswest.movies.models.Movie;

public class MovieCursorWrapper extends CursorWrapper {

    public MovieCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Movie getMovie(){

        long movieId = getLong(getColumnIndex(MovieContract.MovieTable.COLUMN_MOVIE_ID));
        String originalTitle = getString(getColumnIndex(MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE));
        String title = getString(getColumnIndex(MovieContract.MovieTable.COLUMN_TITLE));

        //revisit poster after learning how to deal with blobs

        String synopsis = getString(getColumnIndex(MovieContract.MovieTable.COLUMN_SYNOPSIS));
        float userRating = getFloat(getColumnIndex(MovieContract.MovieTable.COLUMN_USER_RATING));
        String releaseDate = getString(getColumnIndex(MovieContract.MovieTable.COLUMN_RELEASE_DATE));

        Movie movie = new Movie(movieId);
        movie.setOriginalTitle(originalTitle);
        movie.setTitle(title);
        movie.setOverview(synopsis);
        movie.setVoteAverage(userRating);
        movie.setReleaseDate(releaseDate);

        return movie;
    }

}

