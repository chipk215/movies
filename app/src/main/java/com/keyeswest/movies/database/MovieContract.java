package com.keyeswest.movies.database;


import android.provider.BaseColumns;

/**
 * Defines the Movie entity for the MovieBase database
 */
public class MovieContract {

    public static final class MovieTable implements BaseColumns{
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_ORIGINAL_TITLE = "originalTitle";
        public static final String COLUMN_TITLE= "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_USER_RATING = "userRating";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
    }

}
