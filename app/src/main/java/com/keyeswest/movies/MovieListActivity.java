package com.keyeswest.movies;

import android.support.v4.app.Fragment;

import com.keyeswest.movies.fragments.MovieListFragment;

/*       >>See Attribution in SingleFragmentActivity<<  */


public class MovieListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return MovieListFragment.newInstance();
    }


}
