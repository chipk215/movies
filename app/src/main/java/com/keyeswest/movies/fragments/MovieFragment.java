package com.keyeswest.movies.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keyeswest.movies.MovieFetcher;
import com.keyeswest.movies.R;
import com.keyeswest.movies.models.Movie;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MovieFragment extends Fragment {
    private static final String TAG = "MovieFragment";
    private static final String ARG_MOVIE = "movie_arg";

    private Movie mMovie;

    @BindView(R.id.title_tv)TextView mTitleTextView;
    @BindView(R.id.release_date_tv)TextView mReleaseDateTextView;
    @BindView(R.id.voter_average_tv)TextView mVoterAverageTextView;
    @BindView(R.id.popularity_tv)TextView mPopularityTextView;
    @BindView(R.id.poster_iv)ImageView mPosterImageView;
    @BindView(R.id.synopsis_tv)TextView mSynopsisTextView;

    private Unbinder mUnbinder;



    /**
     * newInstance enables a fragment argument representing a movie to be used to initialize the
     * fragment.
     * @param movie - the movie object that the fragment is representing.
     * @return initialized movie fragment
     */
    public static MovieFragment newInstance(Movie movie){
        Log.i(TAG, "New MovieFragment Instance");

        //Re-bundle the movie since the fragment has not yet been created
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        MovieFragment fragment = new MovieFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     *  In conjunction with the construction of the fragment using newInstance, fragment arguments
     *  are retrieved and used to initialize the movie fragment.
     * @param savedInstanceState - not currently used
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null){
            mMovie = bundle.getParcelable(ARG_MOVIE);
        }
        else{
            Log.e(TAG, "An expected movie object was not provided to initialize the fragment");
            mMovie = new Movie();
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.movie_detail_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);


        mTitleTextView.setText(mMovie.getOriginalTitle());

        mReleaseDateTextView.setText(mMovie.getReleaseDate());

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        mVoterAverageTextView.setText(decimalFormat.format(mMovie.getVoteAverage()));

        mPopularityTextView.setText(decimalFormat.format(mMovie.getPopularity()));

        //TODO research - does Picasso get the image from the cache or will the new context force a fetch?
        String imagePath = MovieFetcher.getPosterPathURL(mMovie.getPosterPath());
        Picasso.with(getContext()).load(imagePath).into(mPosterImageView);

        mSynopsisTextView.setText(mMovie.getOverview());

        return view;

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
