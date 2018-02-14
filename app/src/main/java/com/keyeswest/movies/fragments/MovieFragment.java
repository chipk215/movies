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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.adapters.ReviewAdapter;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.models.Review;
import com.keyeswest.movies.utilities.MovieFetcher;
import com.keyeswest.movies.R;
import com.keyeswest.movies.adapters.TrailerAdapter;

import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.models.Trailer;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MovieFragment extends Fragment  {
    private static final String TAG = "MovieFragment";
    private static final String ARG_MOVIE = "movie_arg";

    private String mShow;
    private String mHide;


    private Movie mMovie;
    private MovieFetcher mMovieFetcher;

    private boolean mMovieTrailersFetched;
    private boolean mMovieReviewsFetched;

    private List<Trailer> mTrailers;
    private List<Review> mReviews;

    @BindView(R.id.title_tv)TextView mTitleTextView;
    @BindView(R.id.release_date_tv)TextView mReleaseDateTextView;
    @BindView(R.id.voter_average_tv)TextView mVoterAverageTextView;
    @BindView(R.id.popularity_tv)TextView mPopularityTextView;
    @BindView(R.id.poster_iv)ImageView mPosterImageView;
    @BindView(R.id.synopsis_tv)TextView mSynopsisTextView;


    @BindView(R.id.trailer_show_btn) ImageButton mShowTrailerButton;
    @BindView(R.id.trailer_recycler_view) RecyclerView mTrailerRecyclerView;
    @BindView(R.id.no_trailer_tv)TextView mNoTrailersTextView;

    @BindView(R.id.review_show_btn) ImageButton mShowReviewButton;
    @BindView(R.id.review_recycler_view) RecyclerView mReviewRecyclerView;
    @BindView(R.id.loading_spinner) ProgressBar mLoadingSpinner;

    private View mRootView;

    private Unbinder mUnbinder;

    Context mContext;
    Activity mActivity;

    private  class TrailerResults implements MovieFetcherCallback<Trailer>{
        @Override
        public void updateList(List<Trailer> trailers) {
            for (Trailer trailer :trailers){
                Log.i(TAG,"Trailer title: " + trailer.getName());
                mMovie.addTrailer(trailer);
            }
            if (trailers.isEmpty()){
                String message = mNoTrailersTextView.getText().toString();
                message = mMovie.getOriginalTitle() + " " + message;
                mNoTrailersTextView.setText(message);
                mNoTrailersTextView.setVisibility(View.VISIBLE);
                mTrailerRecyclerView.setVisibility(View.GONE);


            }else{
                mTrailers.addAll(trailers);
                mTrailerRecyclerView.setVisibility(View.VISIBLE);
                mTrailerRecyclerView.getAdapter().notifyDataSetChanged();
            }


        }

        @Override
        public void downloadErrorOccurred(ErrorCondition errorMessage) {
            Log.e(TAG,"Error fetching trailers: " + errorMessage);
        }
    }


    private class ReviewResults implements MovieFetcherCallback<Review>{

        @Override
        public void updateList(List<Review> itemList) {
            Log.i(TAG, "Updating Reviews");
            mReviews.addAll(itemList);

            mReviewRecyclerView.getAdapter().notifyItemInserted(mReviews.size()-1);
            mLoadingSpinner.setVisibility(View.GONE);

            final ScrollView sv = mRootView.findViewById(R.id.detail_sv);
            sv.post(new Runnable() {
                public void run() {
                    sv.scrollBy(0,200);
                }
            });


        }

        @Override
        public void downloadErrorOccurred(ErrorCondition errorMessage) {
            Log.e(TAG, "Download error occurred" + errorMessage);
            mLoadingSpinner.setVisibility(View.GONE);
        }
    }


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
            // return
        }

        mTrailers = new ArrayList<>();
        mReviews = new ArrayList<>();

        mMovieTrailersFetched = false;
        mMovieReviewsFetched = false;

        mShow = getResources().getString(R.string.show);
        mHide =  getResources().getString(R.string.hide);

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

        String imagePath = MovieFetcher.getPosterPathURL(mMovie.getPosterPath());
        Picasso.with(getContext()).load(imagePath).into(mPosterImageView);

        mSynopsisTextView.setText(mMovie.getOverview());

        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupReviewAdapter();
        setupTrailerVisibility();
        setupReviewVisibility();

        mRootView = view;

        return view;

    }


    private void setupReviewVisibility() {
        hideReview();

        mShowReviewButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG,"RV Button Clicked");
                String tagString = (String) mShowReviewButton.getTag();
                if (tagString.equals(mShow)) {
                    Log.d(TAG,"RV Button tag is show moving to hide");
                    mShowReviewButton.setImageResource(R.drawable.ic_action_collapse);
                    mShowReviewButton.setTag(mHide);
                    showReview();
                    if (!mMovieReviewsFetched) {
                        mMovieReviewsFetched = true;
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        mMovieFetcher.fetchFirstReviewPage(mMovie.getId(), new ReviewResults());

                    }
                }else {
                    Log.d(TAG,"RV Button tag is hide moving to show");
                    mShowReviewButton.setImageResource(R.drawable.ic_action_expand);
                    mShowReviewButton.setTag(mShow);
                    hideReview();
                }
            }
        });
    }

    private void setupTrailerVisibility(){
        hideTrailer();

        mShowTrailerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(),"Click", Toast.LENGTH_SHORT).show();

                String tagString = (String)mShowTrailerButton.getTag();
                if (tagString.equals(mShow)){

                    mShowTrailerButton.setTag(mHide);
                    mShowTrailerButton.setImageResource(R.drawable.ic_action_collapse);

                    if (! mMovieTrailersFetched) {
                        setupTrailerAdapter();
                        mMovieFetcher.fetchMovieTrailers(mMovie.getId(), new TrailerResults());
                        mMovieTrailersFetched = true;
                    }else{
                        showTrailer();

                    }
                }else{
                    mShowTrailerButton.setImageResource(R.drawable.ic_action_expand);
                    mShowTrailerButton.setTag(mShow);
                    hideTrailer();
                }
            }
        });

    }



    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
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
        if (isAdded() && (mTrailerRecyclerView.getAdapter() == null)){

            mTrailerRecyclerView.setAdapter(new TrailerAdapter(mTrailers, new TrailerAdapter.OnItemClickListener(){
                @Override
                public void onItemClick(Trailer trailer){
                    playVideo(trailer);
                }
            }));
        }
    }


    private void setupReviewAdapter(){
        if (isAdded()){
            mReviewRecyclerView.setAdapter(new ReviewAdapter(mReviews));
        }
    }


    private void hideTrailer(){
        mNoTrailersTextView.setVisibility(View.GONE);
        mTrailerRecyclerView.setVisibility(View.GONE);
    }

    private void showTrailer(){
        if (mTrailers.isEmpty()){
            mNoTrailersTextView.setVisibility(View.VISIBLE);
        }else {
            mTrailerRecyclerView.setVisibility(View.VISIBLE);
        }

    }

    private void hideReview(){
        mReviewRecyclerView.setVisibility(View.GONE);
    }

    private void showReview(){
        mReviewRecyclerView.setVisibility(View.VISIBLE);
        final ScrollView sv = mRootView.findViewById(R.id.detail_sv);
        sv.post(new Runnable() {
            public void run() {
                sv.scrollBy(0,200);
            }
        });
    }
}
