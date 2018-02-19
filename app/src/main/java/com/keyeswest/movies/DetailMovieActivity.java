package com.keyeswest.movies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.keyeswest.movies.fragments.MovieFragment;
import com.keyeswest.movies.models.Movie;


public class DetailMovieActivity extends SingleFragmentActivity {

    private static final String TAG = "DetailMovieActivity";

    private static final String EXTRA_MOVIE = "com.keyeswest.movies.movie";
    public static final String EXTRA_MOVIE_ID = "com.keyeswest.movies.movieId";
    public static final String EXTRA_MOVIE_FAVORITE_REMOVED = "com.keyeswest.movies.favoriteRemoved";

    public static Intent newIntent(Context context, Movie movie){
        Intent intent = new Intent(context, DetailMovieActivity.class);
        intent.putExtra(EXTRA_MOVIE, movie);
        return intent;
    }

    public static Intent newIntent(Context context, long movieId){
        Intent intent = new Intent(context, DetailMovieActivity.class);
        intent.putExtra(EXTRA_MOVIE_ID, movieId);
        return intent;
    }

    public static Movie getMovie(Intent result){
        Log.d(TAG, "getMovie invoked.");
        if (result != null){
            Movie movie = result.getParcelableExtra(EXTRA_MOVIE);
            Log.d(TAG, "movie trailer count: " + movie.getTrailers().size());
            return movie;
        }

        return null;
    }

    public static long getMovieId(Intent intent){
        if (intent != null){
            long movieId = intent.getLongExtra(EXTRA_MOVIE_ID,0);
            return movieId;
        }

        return 0;
    }

    public static boolean favoriteWasRemoved(Intent intent){
        if (intent != null){
            boolean wasRemoved = intent.getBooleanExtra(EXTRA_MOVIE_FAVORITE_REMOVED, false);
            return wasRemoved;
        }

        return false;
    }

    @Override
    protected Fragment createFragment() {


        //TODO error handling - what if there is no intent?

        Movie movie = getIntent().getParcelableExtra(EXTRA_MOVIE);

        if (movie == null){
            long movieId = getIntent().getLongExtra(EXTRA_MOVIE_ID,0);
            return MovieFragment.newInstance(movieId);

        }else{

        /* **** ATTRIBUTION ****
        *
        * De-serializing here uncouples the caller from the fragment which should
        * make the fragment more reusable.
        *
        * The concept came from:
        * 3rd Edition Android Programming
        * Big Nerd Ranch Guide by Philips, Stewart, and Marsicano
        *
        */
        return MovieFragment.newInstance(movie);
        }

    }




}
