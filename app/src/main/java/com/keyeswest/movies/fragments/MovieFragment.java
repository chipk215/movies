package com.keyeswest.movies.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
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
import android.widget.Toast;

import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.adapters.ReviewAdapter;
import com.keyeswest.movies.data.MovieContract;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.models.Review;
import com.keyeswest.movies.repos.MovieRepo;
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

    private static final int NUDGE_VERTICAL_PIXELS = 400;

    private String mShow;
    private String mHide;


    private Movie mMovie;
    private MovieFetcher mMovieFetcher;

    private boolean mMovieTrailersFetched;
    private boolean mMovieReviewsFetched;

    private List<Trailer> mTrailers;
    private List<Review> mReviews;

    private Toast mFavoriteToast;

    @BindView(R.id.title_tv)TextView mTitleTextView;
    @BindView(R.id.release_date_tv)TextView mReleaseDateTextView;
    @BindView(R.id.voter_average_tv)TextView mVoterAverageTextView;
    @BindView(R.id.popularity_tv)TextView mPopularityTextView;
    @BindView(R.id.poster_iv)ImageView mPosterImageView;
    @BindView(R.id.synopsis_tv)TextView mSynopsisTextView;


    @BindView(R.id.trailer_show_btn) ImageButton mShowTrailerButton;
    @BindView(R.id.trailer_recycler_view) RecyclerView mTrailerRecyclerView;
    @BindView(R.id.no_trailer_tv)TextView mNoTrailersTextView;
    @BindView(R.id.trailer_loading_spinner)ProgressBar mTrailerLoadingSpinner;

    @BindView(R.id.review_show_btn) ImageButton mShowReviewButton;
    @BindView(R.id.review_recycler_view) RecyclerView mReviewRecyclerView;
    @BindView(R.id.review_loading_spinner) ProgressBar mReviewLoadingSpinner;
    @BindView(R.id.no_review_tv) TextView mNoReviewsTextView;

    @BindView(R.id.favorite_fab)FloatingActionButton mFavoriteFab;

    boolean mReviewIsLoading;

    private View mRootView;

    private Unbinder mUnbinder;

    Context mContext;
    Activity mActivity;

    private MovieRepo mMovieRepo;


    /**
     * Handles the trailer data fetched asynchronously from theMovieDB site.
     * Implements the MovieFetcherCall interface to obtain trailer data or
     * to handle errors that occurred during the fetch.
     */
    private  class TrailerResults implements MovieFetcherCallback<Trailer>{
        @Override
        public void updateList(List<Trailer> trailers) {

            mTrailers.addAll(trailers);
            mTrailerLoadingSpinner.setVisibility(View.GONE);

            // configure the UI depending upon whether there is any trailer data to display
            if (trailers.isEmpty()){

                // no trailer data so display a no trailer message to the user
                String message = mNoTrailersTextView.getText().toString();
                message = mMovie.getTitle() + " " + message;
                mNoTrailersTextView.setText(message);
                // Show the no trailer message
                mNoTrailersTextView.setVisibility(View.VISIBLE);

                // Hide the trailer list view
                mTrailerRecyclerView.setVisibility(View.GONE);


            }else{
                // display the trailer data and notify the adapter there is no data
                mTrailerRecyclerView.setVisibility(View.VISIBLE);
                mTrailerRecyclerView.getAdapter().notifyItemInserted(mTrailers.size()-1);
            }
        }

        @Override
        public void downloadErrorOccurred(ErrorCondition errorMessage) {
            Log.e(TAG,"Error fetching trailers: " + errorMessage);
            mTrailerLoadingSpinner.setVisibility(View.GONE);
        }
    }


    /**
     * Handles the review data fetched asynchronously from theMovieDB site.
     * Implements the MovieFetcherCall interface to obtain review data or
     * to handle errors that occurred during the fetch.
     */
    private class ReviewResults implements MovieFetcherCallback<Review>{

        @Override
        public void updateList(List<Review> itemList) {
            Log.i(TAG, "Updating Reviews");

            mReviews.addAll(itemList);
            mReviewLoadingSpinner.setVisibility(View.GONE);
            mReviewIsLoading = false;

            // configure the UI depending upon whether there is any review data to display
            if (mReviews.isEmpty()){
                // craft the no review message for the user
                String message = mNoReviewsTextView.getText().toString();
                message = mMovie.getTitle() + " " + message;
                mNoReviewsTextView.setText(message);
                mNoReviewsTextView.setVisibility(View.VISIBLE);
                //hide the review list
                mReviewRecyclerView.setVisibility(View.GONE);

            }else{
                if (! itemList.isEmpty()) {
                    // update the list and show the trailer data to the user.
                    mReviewRecyclerView.getAdapter().notifyItemInserted(mReviews.size() - 1);

                    // scroll the screen a bit to show trailer data in case all of the trailer items
                    // are off screen
                    showReviewsWithNudge();
                }
            }
        }


        @Override
        public void downloadErrorOccurred(ErrorCondition errorMessage) {
            Log.e(TAG, "Download error occurred" + errorMessage);
            mReviewLoadingSpinner.setVisibility(View.GONE);
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

        mMovieRepo = new MovieRepo(getContext());

        Bundle bundle = getArguments();
        if (bundle != null){
            mMovie = bundle.getParcelable(ARG_MOVIE);
        }
        else{
            Log.e(TAG, "An expected movie object was not provided to initialize the fragment");
            // return
        }

        // data sources for the recycler list views
        mTrailers = new ArrayList<>();
        mReviews = new ArrayList<>();

        // indicates whether the first page of trailers and reviews have been fetched
        // helps with UI configuration
        mMovieTrailersFetched = false;
        mMovieReviewsFetched = false;

        // cache these resource strings for easy reference
        mShow = getResources().getString(R.string.show);
        mHide =  getResources().getString(R.string.hide);

        mReviewIsLoading = false;

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mContext = getContext();
        mActivity = getActivity();


        View view = inflater.inflate(R.layout.movie_detail_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mTitleTextView.setText(mMovie.getTitle());

        mReleaseDateTextView.setText(mMovie.getReleaseDate());

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        mVoterAverageTextView.setText(decimalFormat.format(mMovie.getVoteAverage()));

        mPopularityTextView.setText(decimalFormat.format(mMovie.getPopularity()));

        String imagePath = MovieFetcher.getPosterPathURL(mMovie.getPosterPath());
        Picasso.with(getContext()).load(imagePath).into(mPosterImageView);

        mSynopsisTextView.setText(mMovie.getOverview());

        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

      //  setInitialFavoriteState();
        setInitialFavoriteButtonState();

        mFavoriteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFavoriteFab.getTag().equals(getResources().getString(R.string.star))){

                    showAToast(R.string.remove_favorite);

                    mFavoriteFab.setImageResource(R.drawable.ic_action_star_border);
                    mFavoriteFab.setTag(getResources().getString(R.string.border));
                }else{

                    showAToast(R.string.add_favorite);
                    mMovieRepo.addMovie(mMovie, new MovieRepo.AddMovieResult() {
                        @Override
                        public void movieResult(Uri movieUri) {
                            if (movieUri != null) {
                                Log.i(TAG, "Inserted movie into database" + movieUri.toString());
                            }else{
                                Log.i(TAG, "Failed to insert movie into database");
                            }
                        }
                    });
                    mFavoriteToast.show();
                    mFavoriteFab.setImageResource(R.drawable.ic_action_star);
                    mFavoriteFab.setTag(getResources().getString(R.string.star));
                }
            }
        });

        setupTrailerAdapter();
        setupReviewAdapter();
        setupTrailerVisibility();
        setupReviewVisibility();

        mRootView = view;

        return view;

    }

    private void setInitialFavoriteButtonState(){

        // Set as not a favorite
        mFavoriteFab.setImageResource(R.drawable.ic_action_star_border);
        mFavoriteFab.setTag(getResources().getString(R.string.border));

        mMovieRepo.getMovieById(mMovie.getId(), new MovieRepo.MovieResult() {
            @Override
            public void movieResult(List<Movie> movies) {
                if ((movies.size() == 1) && (movies.get(0).getId() == mMovie.getId())){
                    mFavoriteFab.setImageResource(R.drawable.ic_action_star);
                    mFavoriteFab.setTag(getResources().getString(R.string.star));
                }
            }
        });

    }

    private void setInitialFavoriteState(){

        // Set as not a favorite
        mFavoriteFab.setImageResource(R.drawable.ic_action_star_border);
        mFavoriteFab.setTag(getResources().getString(R.string.border));

        String[] projection={
                MovieContract.MovieTable.COLUMN_MOVIE_ID
        };

        String selectionClause = MovieContract.MovieTable.COLUMN_MOVIE_ID + "=  ?";
        String[] selectionArgs = { Long.toString(mMovie.getId()) };

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                projection,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        if ((movieCursor != null) && (movieCursor.getCount() == 1)) {

                movieCursor.moveToFirst();
                long movieId = movieCursor.getLong(movieCursor
                        .getColumnIndex(MovieContract.MovieTable.COLUMN_MOVIE_ID));

                if (mMovie.getId() == movieId){

                    // set as favorite
                    mFavoriteFab.setImageResource(R.drawable.ic_action_star);
                    mFavoriteFab.setTag(getResources().getString(R.string.star));
                }
        }
    }

    private void showAToast(int resId){
        if (mFavoriteToast != null){
            mFavoriteToast.cancel();
        }
        mFavoriteToast = Toast.makeText(mContext, resId, Toast.LENGTH_SHORT);
        mFavoriteToast.show();
    }


    /**
     * Users can collapse or expand review data to unclutter the UI.
     * This method implements the logic for hiding and showing Reviews based upon the user
     * clicking the corresponding expand/collapse button on the screen.
     */
    private void setupReviewVisibility() {
        // ensure the review section is initially collapsed
        hideReview();
        mShowReviewButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String tagString = (String) mShowReviewButton.getTag();
                if (tagString.equals(mShow)) {
                    // Show tag represents a collapsed view that should now be expanded

                    // change the button icon to the collapse view state and toggle the tag
                    mShowReviewButton.setImageResource(R.drawable.ic_action_collapse);
                    mShowReviewButton.setTag(mHide);

                    if (!mMovieReviewsFetched) {
                        // no reviews have been fetched yet

                        // note: the list of reviews will not be made visible until it is determined
                        // that there are reviews for the movie
                        mMovieReviewsFetched = true;
                        updateReviewItems(true);

                    }else{

                        // the user has toggled the expand/collapse Review button so update the UI
                        showReview();
                    }
                }else {

                    // user is collapsing the review section
                    // set up the button to expand the section
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

                String tagString = (String)mShowTrailerButton.getTag();
                if (tagString.equals(mShow)){

                    mShowTrailerButton.setImageResource(R.drawable.ic_action_collapse);
                    mShowTrailerButton.setTag(mHide);


                    if (! mMovieTrailersFetched) {
                        mMovieTrailersFetched = true;
                        mTrailerLoadingSpinner.setVisibility(View.VISIBLE);
                        mMovieFetcher.fetchMovieTrailers(mMovie.getId(), new TrailerResults());

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
            //TODO inform user that video is not available
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


    private void setupReviewAdapter(){

        if (isAdded()){
            mReviewRecyclerView.setAdapter(new ReviewAdapter(mReviews));
            mReviewRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mReviewRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                    //current number of child views attached to the parent RecyclerView
                    int visibleItemCount = layoutManager.getChildCount();
                    Log.i(TAG,"visibleCount= " + visibleItemCount);

                    //number of items in the adapter bound to the parent RecyclerView
                    int totalItemCount = layoutManager.getItemCount();
                    Log.i(TAG,"totalItemCount= " + totalItemCount);

                    int firstVisibleItem = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
                    Log.i(TAG,"firstVisibleItem= " + firstVisibleItem);

                    if ((totalItemCount - visibleItemCount) <= firstVisibleItem){
                        Log.i(TAG, "Get next page...");

                        if (!mReviewIsLoading){

                            updateReviewItems(false);

                        }
                    }

                }
            });

        }
    }


    private void updateReviewItems(Boolean firstPage){

        mReviewIsLoading = true;
        mReviewLoadingSpinner.setVisibility(View.VISIBLE);
        if (firstPage){

            mMovieFetcher.fetchFirstReviewPage(mMovie.getId(), new ReviewResults());

        }else{
            mMovieFetcher.fetchNextReviewPage(mMovie.getId());
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
        mNoReviewsTextView.setVisibility(View.GONE);
    }

    private void showReview(){

        if (mReviews.isEmpty()){
            mNoReviewsTextView.setVisibility(View.VISIBLE);

        }else {
            showReviewsWithNudge();
        }
    }


    /**
     * The review section is at the bottom of a scrollable view. This method ensures the view is
     * scrolled up a bit to show new review data that may have been added but is off screen and not
     * visible to the user until they scroll.
     *
     * There probably is a better way to to handle this but this will do for now.
     */
    private void showReviewsWithNudge(){
        mReviewRecyclerView.setVisibility(View.VISIBLE);
        final ScrollView sv = mRootView.findViewById(R.id.detail_sv);

        // Attribution:  https://stackoverflow.com/a/6438136/9128441
        sv.post(new Runnable() {
            public void run() {
                sv.scrollBy(0, NUDGE_VERTICAL_PIXELS);
            }
        });

    }
}
