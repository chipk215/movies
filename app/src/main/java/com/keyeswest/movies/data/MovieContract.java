package com.keyeswest.movies.data;


import android.net.Uri;

/**
 * Defines the Movie entity for the MovieBase database
 */
public class MovieContract {


    public static final String AUTHORITY = "com.keyeswest.movies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIE = MovieTable.TABLE_NAME;

    public static final class MovieTable {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_ORIGINAL_TITLE = "originalTitle";
        public static final String COLUMN_TITLE= "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_USER_RATING = "userRating";
        public static final String COLUMN_POPULAR_RATING = "popularRating";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
    }

}
