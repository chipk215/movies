package com.keyeswest.movies;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.keyeswest.movies.fragments.MovieFragment;
import com.keyeswest.movies.models.Movie;


public class DetailMovieActivity extends SingleFragmentActivity {

    private static final String EXTRA_MOVIE = "com.keyeswest.movies.movie";

    public static Intent newIntent(Context context, Movie movie){
        Intent intent = new Intent(context, DetailMovieActivity.class);
        intent.putExtra(EXTRA_MOVIE, movie);
        return intent;
    }

    @Override
    protected Fragment createFragment() {


        //TODO error handling - what if there is no intent?
        Movie movie = getIntent().getParcelableExtra(EXTRA_MOVIE);


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
