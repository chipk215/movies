package com.keyeswest.movies.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keyeswest.movies.DetailMovieActivity;
import com.keyeswest.movies.utilities.MovieFetcher;
import com.keyeswest.movies.R;
import com.keyeswest.movies.adapters.TrailerAdapter;
import com.keyeswest.movies.interfaces.TrailerFetcherCallback;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.models.Trailer;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class MovieFragment extends Fragment implements TrailerFetcherCallback {
    private static final String TAG = "MovieFragment";
    private static final String ARG_MOVIE = "movie_arg";

    private Movie mMovie;
    private MovieFetcher mMovieFetcher;

    private List<Trailer> mTrailers;

    @BindView(R.id.title_tv)TextView mTitleTextView;
    @BindView(R.id.release_date_tv)TextView mReleaseDateTextView;
    @BindView(R.id.voter_average_tv)TextView mVoterAverageTextView;
    @BindView(R.id.popularity_tv)TextView mPopularityTextView;
    @BindView(R.id.poster_iv)ImageView mPosterImageView;
    @BindView(R.id.synopsis_tv)TextView mSynopsisTextView;

    @BindView(R.id.trailer_recycler_view) RecyclerView mTrailerRecyclerView;

    private Unbinder mUnbinder;


    Context mContext;
    Activity mActivity;



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

        mMovieFetcher = new MovieFetcher(getContext());

        Bundle bundle = getArguments();
        if (bundle != null){
            mMovie = bundle.getParcelable(ARG_MOVIE);
        }
        else{
            Log.e(TAG, "An expected movie object was not provided to initialize the fragment");
            mMovie = new Movie();
        }

        mTrailers = new ArrayList<>();




    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mContext = getContext();
        mActivity = getActivity();


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

        mMovieFetcher.fetchMovieTrailers(mMovie.getId(), this);
/*
        if (mMovie.getTrailers().isEmpty()){
            // fetch trailers
            mMovieFetcher.fetchMovieTrailers(mMovie.getId(), this);

        }
        */

        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return view;

    }



    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void updateTrailerList(List<Trailer> trailers) {
        for (Trailer trailer :trailers){
            Log.i(TAG,"Trailer title: " + trailer.getName());
            mMovie.addTrailer(trailer);
        }

        mTrailers.addAll(trailers);


        mTrailerRecyclerView.setVisibility(View.VISIBLE);
       //setMovieUpdateResult(mMovie);
        setupTrailerAdapter();

    }

    @Override
    public void downloadErrorOccurred(ErrorCondition errorMessage) {
        Log.e(TAG,"Error fetching trailers: " + errorMessage);
    }

    private void setMovieUpdateResult(Movie movie){
        Log.i(TAG,"Invoking setMovieUpdateResult " + movie.getTrailers().size());

        Intent intent = DetailMovieActivity.newIntent(mContext, movie);
        mActivity.setResult(RESULT_OK,intent);

        Movie movieDebug  = DetailMovieActivity.getMovie(intent);
        Log.d(TAG,"Debug Intent trailers equal:" + movieDebug.getTrailers().size());

    }


    private void playVideo(Trailer trailer){

        Uri uri = trailer.getVideoUri();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null){
            startActivity(intent);
        }else{
            //TODO inform user that video is not avialable
        }

    }


    private void setupTrailerAdapter(){
        if (isAdded()){
            mTrailerRecyclerView.setAdapter(new TrailerAdapter(mTrailers, new TrailerAdapter.OnItemClickListener(){
                @Override
                public void onItemClick(Trailer trailer){
                    playVideo(trailer);
                }
            }));
        }
    }
}
