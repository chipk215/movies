package com.keyeswest.movies.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.keyeswest.movies.DetailMovieActivity;
import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.adapters.ReviewAdapter;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.models.Review;
import com.keyeswest.movies.repos.MovieRepo;
import com.keyeswest.movies.utilities.MovieFetcher;
import com.keyeswest.movies.R;
import com.keyeswest.movies.adapters.TrailerAdapter;

import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.models.Trailer;
import com.keyeswest.movies.utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


@SuppressWarnings("ConstantConditions")
public class MovieFragment extends Fragment  {
    private static final String TAG = "MovieFragment";
    private static final String ARG_MOVIE = "movie_arg";
    private static final String ARG_MOVIE_ID = "movie_arg_id";

    private static final String TRAILER_HIDDEN_KEY = "trailer_hidden_key";
    private static final String REVIEW_HIDDEN_KEY = "review_hidden_key";

    private static final int NUDGE_VERTICAL_PIXELS = 400;

    // convenience properties holding resource strings
    private String mShow;
    private String mHide;

    // the movie associated with the movie fragment
    private Movie mMovie;
    private long mMovieId;

    // movieFetcher to fetch data from theMovieDB
    private MovieFetcher mMovieFetcher;

    // state variables used to indicate if trailers and reviews have been fetched for the first time
    private boolean mMovieTrailersFetched;
    private boolean mMovieReviewsFetched;

    // list of trailers and reviews fetched from theMovieDB
    private List<Trailer> mTrailers;
    private List<Review> mReviews;

    // used to communicate movie has been favored or unfavored
    private Toast mFavoriteToast;

    // helper to handle failed network operations for fetching trailers, reviews and intents for
    // playing videos and accessing movie reviews online
    private enum NetworkOperations {TRAILER, REVIEW, ACTION_VIEW}
    private NetworkOperations mFailedOperation = null;
    private Intent mFailedImplicitIntent = null;

    // callback handlers for async fetching of trailer and review data
    private TrailerResults mTrailerHandler;
    private ReviewResults mReviewHandler;

    @BindView(R.id.original_title_tv)TextView mOriginalTitleTextView;
    @BindView(R.id.movie_title_tv)TextView mMovieTitleTextView;
    @BindView(R.id.movie_title_label_tv)TextView mMovieTitleLabelTextView;
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

    @BindView(R.id.error_layout) LinearLayout mErrorLayout;
    @BindView(R.id.error_txt_cause) TextView mErrorText;

    @BindView(R.id.movie_detail_layout) CoordinatorLayout mParentLayout;

    // true when loading a review
    private boolean mReviewIsLoading;

    // reference to top level view to access scrollview for programmatic adjustment
    private View mRootView;

    // ButterKnife helper
    private Unbinder mUnbinder;

    // stash the context and activity for convenience
    private Context mContext;

    // movie repo for SQL access
    private MovieRepo mMovieRepo;

    // favorites return an intent to the calling activity
    private Boolean mIsFavorite;

    // the intent returned to the calling activity if the user is viewing favorites
    // the returned intent informs the calling activity if the user removed the favorite while
    // in the detail view
    private Intent mReturnIntent;


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

    public static MovieFragment newInstance(long movieId){
        Log.i(TAG, "New MovieFragment Instance");

        //Re-bundle the movie since the fragment has not yet been created
        Bundle args = new Bundle();
        args.putLong(ARG_MOVIE_ID, movieId);
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

        //process the fragment arguments
        Bundle bundle = getArguments();

        if (bundle != null){

            // determine if a movie object or movieId was provided as an argument
            mMovie = bundle.getParcelable(ARG_MOVIE);
            mIsFavorite = false;
            if (mMovie == null){

                // A movie object was not passed so must be a movieId corresponding to a
                // favorite item. The favorite movie will be retrieved from the database in
                // onCreateView
                mIsFavorite = true;
                mMovieId = bundle.getLong(ARG_MOVIE_ID);
            }
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

        mTrailerHandler = new TrailerResults();
        mReviewHandler = new ReviewResults();

        setRetainInstance(true);

    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mContext = getContext();
        Activity activity = getActivity();

        View view = inflater.inflate(R.layout.movie_detail_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        //-----------------------------------------------------------------------------------
        // Another suspect decision...
        //
        // So in trying to leverage the detail view for favorites, I needed to return data to
        // the calling activity when the calling activity is showing only favorites and the user
        // un-favors the movie in this view. The calling activity needs to remove the un-favored
        // movie from the display when the fragment returns. This is handled with a result but
        // only for the favorites view.
        //
        // Probably should just be consistent and have both the favorites and non-favorite views
        // return results.
        //
        if (mMovie != null){
            // update the view with the movie object passed to the fragment
            updateView();
        }else{

            // get the favorite movie from the database
            mMovieRepo.getMovieById(mMovieId, new MovieRepo.QuerySetResult() {
                @Override
                public void movieResult(List<Movie> movies) {

                    // or error checking
                    mMovie = movies.get(0);

                    // initialize the intent to return to the calling activity indicating the
                    // user did not un-favor the movie
                    setReturnIntent(false);

                    // update view with movie favorite information
                    updateView();

                }
            });
        }


        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Attribution: https://stackoverflow.com/a/41201865/9128441
        // list item delimiter
        mTrailerRecyclerView.addItemDecoration(new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL));

        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReviewRecyclerView.addItemDecoration(new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL));

        // Retry button for network connectivity issues
        Button retryButton = view.findViewById(R.id.error_btn_retry);

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // determine which network operation to retry

                switch(mFailedOperation){
                    case TRAILER:
                        mMovieFetcher.fetchMovieTrailers(mMovie.getId(), mTrailerHandler);
                        break;
                    case REVIEW:
                        int currentPage = mMovieFetcher.getCurrentReviewPage();
                        if (currentPage > 1){
                            updateReviewItems(false);
                        }else{
                            updateReviewItems(true);
                        }
                        break;
                    case ACTION_VIEW:
                        startActivityWithIntent(mFailedImplicitIntent);
                        break;
                }

            }
        });


        mFavoriteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                 * A filled in star represents the state where the user has made the movie a
                 * favorite. If the user clicks on a filled in star they are initiating a request
                 * to remove the movie from the database of favorites.
                 *
                 * Conversely, an unfilled star represents the state where the movie is not a user
                 * favorite. A click on an unfilled start initiates a request to add the movie
                 * to the database of favorites.
                 */
                if (mFavoriteFab.getTag().equals(getResources().getString(R.string.star))){
                    removeFavoriteMovie();
                }else{

                    addFavoriteMovie();
                }
            }
        });

        setupTrailerAdapter();
        setupReviewAdapter();

        boolean hideTrailers = true;
        boolean hideReviews = true;

        if (savedInstanceState != null){
            hideTrailers = savedInstanceState.getBoolean(TRAILER_HIDDEN_KEY, true);
            hideReviews = savedInstanceState.getBoolean(REVIEW_HIDDEN_KEY, true);
        }

        setupTrailerVisibility(hideTrailers);
        setupReviewVisibility(hideReviews);


        mRootView = view;
        return view;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){

        // save the expand/collapse state of the trailer and review sections
        outState.putBoolean(TRAILER_HIDDEN_KEY, isTrailerHidden());
        outState.putBoolean(REVIEW_HIDDEN_KEY, isReviewHidden());

        super.onSaveInstanceState(outState);

    }



    /*
     * Update the view with movie information
     */
    private void updateView(){

        mOriginalTitleTextView.setText(mMovie.getOriginalTitle());

        // show movie title if different than original title
        if (! mMovie.getTitle().equals(mMovie.getOriginalTitle())){
            mMovieTitleTextView.setText(mMovie.getTitle());
            mMovieTitleLabelTextView.setVisibility(View.VISIBLE);
            mMovieTitleTextView.setVisibility(View.VISIBLE);
        }else{
            mMovieTitleLabelTextView.setVisibility(View.GONE);
            mMovieTitleTextView.setVisibility(View.GONE);
        }

        mReleaseDateTextView.setText(mMovie.getReleaseDate());

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        mVoterAverageTextView.setText(decimalFormat.format(mMovie.getVoteAverage()));

        mPopularityTextView.setText(decimalFormat.format(mMovie.getPopularity()));

        if (mMovie.getPosterImage() == null) {
            String imagePath = MovieFetcher.getPosterPathURL(mMovie.getPosterPath());
            Picasso.with(getContext()).load(imagePath).into(mPosterImageView);
        }else{
            mPosterImageView.setImageBitmap(mMovie.getPosterImage());
        }

        mSynopsisTextView.setText(mMovie.getOverview());

        setInitialFavoriteButtonState();

    }


    /*
     * Check the database and see if the movie is a favorite.
     * If so set the fab image to a filled in star, otherwise an unfilled star.
     */
    private void setInitialFavoriteButtonState(){

        // Set as not a favorite which will be overridden if movie is in db
        setFabUnfilled();

        //Query database to see if movie is in db
        mMovieRepo.getMovieById(mMovie.getId(), new MovieRepo.QuerySetResult() {
            @Override
            public void movieResult(List<Movie> movies) {
                if ((movies.size() == 1) && (movies.get(0).getId() == mMovie.getId())){
                    setFabFilled();
                }
            }
        });

    }


    /*
     * Removes movie from database of favorites.
     */
    private void removeFavoriteMovie(){
        // tell the user the movie is being removed from the database
        showToast(R.string.remove_favorite);

        // disable the fab while the async sql operation is executing to prevent the user from
        // tapping the button multiple times while the sql operation is executing. Button will be
        // on completion of operation.
        mFavoriteFab.setEnabled(false);

        // initiate deletion of movie record
        mMovieRepo.deleteMovieById(mMovie.getId(), new MovieRepo.QueryCountResult() {

            // Handle the results of the delete operation
            @Override
            public void movieResult(int recordCount) {

                // Expect 1 record to be deleted
                if (recordCount != 1){

                    // Do we need to tell the user anything? This is an unexpected state.
                    // Leave the FAB disabled and do not change the fab icon to an unfilled star
                    Log.e(TAG, "Failed to remove movie from favorites");
                    return;
                }

                // success case
                mFavoriteFab.setEnabled(true);

                // Change the fab to an unfilled star indicating the movie is not favored and the
                // next click will favor the movie again
                setFabUnfilled();
            }
        });

        setReturnIntent(true);

    }



    /**
     * Add the movie to the favorites database.
     */
    private void addFavoriteMovie(){
        // tell the user the movie is added to the favorites
        showToast(R.string.add_favorite);

        // disable fab while the insert operation is executing
        mFavoriteFab.setEnabled(false);

        // insert movie
        mMovieRepo.addMovie(mMovie, new MovieRepo.InsertResult() {

            // Handle insert operation results
            @Override
            public void movieResult(Uri movieUri) {
                // Success is a Uri tot he new record
                if (movieUri != null) {
                    Log.i(TAG, "Inserted movie into database" + movieUri.toString());

                    // enable the fab
                    mFavoriteFab.setEnabled(true);

                    // change the fab image to a filled in star representing the movie is now
                    // a favorite
                    setFabFilled();
                }else{
                    Log.i(TAG, "Failed to insert movie into database");

                    // leaving the fab disabled
                    // leaving the fab image as an unfilled star

                    // consider what else to do in this failure case
                }

            }
        });

        setReturnIntent(false);
    }


    /*
     * Update the intent being returned to the calling activity to reflect whether the user
     * unfavored the movie or not.
     */
    private void  setReturnIntent(boolean removeFavorite){
        if (mIsFavorite){
            if (mReturnIntent == null){
                mReturnIntent =  DetailMovieActivity.newIntent(getContext(),mMovie.getId());
            }
            mReturnIntent.putExtra(DetailMovieActivity.EXTRA_MOVIE_FAVORITE_REMOVED ,removeFavorite);

            getActivity().setResult(Activity.RESULT_OK, mReturnIntent);

        }
    }


    /*
     * If the user clicks the fab before the previous message has vanished then cancel the current
     * toast.
     * @param resId - resource id of message to be displayed
     */
    private void showToast(int resId){
        if (mFavoriteToast != null){
            mFavoriteToast.cancel();
        }
        mFavoriteToast = Toast.makeText(mContext, resId, Toast.LENGTH_SHORT);
        mFavoriteToast.show();
    }


    /*
     * Users can collapse or expand review data to unclutter the UI.
     * This method implements the logic for hiding and showing Reviews based upon the user
     * clicking the corresponding expand/collapse button on the screen.
     */
    private void setupReviewVisibility(boolean hideReviews) {

        if (hideReviews){
            hideReview();
            mShowReviewButton.setTag(mShow);
        }else{
            showReview();
            mShowReviewButton.setTag(mHide);
        }

        //handle toggling the review expand/collapse button
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
                        //fetch the fist page of reviews
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


    /*
     * Similar to reviews the trailers section is collapsible.
     */
    private void setupTrailerVisibility(boolean hideTrailers){

        if (hideTrailers){
            hideTrailer();
            mShowTrailerButton.setTag(mShow);
        }else{
            showTrailer();
            mShowTrailerButton.setTag(mHide);
        }



        // handle toggling the expand/collapse button
        mShowTrailerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String tagString = (String)mShowTrailerButton.getTag();
                if (tagString.equals(mShow)){
                    // section is currently collapsed and user initiated expanding

                    // change the button to collapse to support next action
                    mShowTrailerButton.setImageResource(R.drawable.ic_action_collapse);
                    mShowTrailerButton.setTag(mHide);


                    if (! mMovieTrailersFetched) {

                        // trailer data has not yet been fetched from theMovieDB so fetch
                        mMovieTrailersFetched = true;
                        mTrailerLoadingSpinner.setVisibility(View.VISIBLE);
                        //why do we need a new TrailerResults object each time?
                        mMovieFetcher.fetchMovieTrailers(mMovie.getId(), mTrailerHandler);

                    }else{

                        // trailer data has previously been fetched so just display it
                        showTrailer();
                    }
                }else{
                    // trailers are currently visible to hide and swap button image
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
            startActivityWithIntent(intent);
        }
    }

    private void viewReview(String url){
        if (url != null){
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivityWithIntent(intent);
            }
        }
    }

    private void startActivityWithIntent(Intent intent){
        if (NetworkUtilities.isNetworkAvailable(mContext)) {
            clearNetworkErrorMessage();
            startActivity(intent);
        }else{

            //post error message
            postNetworkErrorMessage(intent);
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
            mReviewRecyclerView.setAdapter(new ReviewAdapter(mReviews, new ReviewAdapter.OnFullReviewClickListener(){

                @Override
                public void onItemClick(String reviewURL) {
                    viewReview(reviewURL);
                }
            }));
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

            mMovieFetcher.fetchFirstReviewPage(mMovie.getId(), mReviewHandler);

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

    private boolean isReviewHidden(){
        return (mReviewRecyclerView.getVisibility() == View.GONE) &&
                (mNoReviewsTextView.getVisibility() == View.GONE);

    }

    private boolean isTrailerHidden(){
        return (mTrailerRecyclerView.getVisibility() == View.GONE) &&
                (mNoTrailersTextView.getVisibility() == View.GONE);

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


    private void setFabFilled(){
        mFavoriteFab.setImageResource(R.drawable.ic_action_star);
        mFavoriteFab.setTag(getResources().getString(R.string.star));
    }

    private void setFabUnfilled(){
        mFavoriteFab.setImageResource(R.drawable.ic_action_star_border);
        mFavoriteFab.setTag(getResources().getString(R.string.border));

    }

    /*
     * Displays error message if network connectivity is lost prior to starting an implicit
     * intent activity like playing a trailer or viewing a review online
     * @param intent - the implicit intent
     */
    private void postNetworkErrorMessage(Intent intent){
        if(mErrorLayout.getVisibility() == View.GONE){
            mErrorText.setText(getResources().getString(R.string.internet_error));
            mErrorLayout.setVisibility(View.VISIBLE);
            mParentLayout.setVisibility(View.GONE);
            mFailedOperation = NetworkOperations.ACTION_VIEW;
            mFailedImplicitIntent = intent;
        }

    }


    /*
     * Clears the network error message if after network connectivity is re-established
     */
    private void clearNetworkErrorMessage(){
        mErrorLayout.setVisibility(View.GONE);
        mParentLayout.setVisibility(View.VISIBLE);
        mFailedOperation = null;
        mFailedImplicitIntent = null;
    }




    /*
     * Handles the review data fetched asynchronously from theMovieDB site.
     * Implements the MovieFetcherCall interface to obtain review data or
     * to handle errors that occurred during the fetch.
     */
    private class ReviewResults implements MovieFetcherCallback<Review>{

        @Override
        public void updateList(List<Review> itemList) {
            Log.i(TAG, "Updating Reviews");

            mFailedOperation = null;

            mParentLayout.setVisibility(View.VISIBLE);
            mErrorLayout.setVisibility(View.GONE);

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
            Log.e(TAG, "Review Download error occurred" + errorMessage);
            switch (errorCondition){
                case NETWORK_CONNECTIVITY:

                    if(mErrorLayout.getVisibility() == View.GONE){
                        mErrorText.setText(getResources().getString(R.string.internet_error));
                        mReviewLoadingSpinner.setVisibility(View.GONE);
                        mErrorLayout.setVisibility(View.VISIBLE);
                        mParentLayout.setVisibility(View.GONE);
                        mFailedOperation = NetworkOperations.REVIEW;
                    }

                    break;
            }

        }
    }


    /*
     * Handles the trailer data fetched asynchronously from theMovieDB site.
     * Implements the MovieFetcherCall interface to obtain trailer data or
     * to handle errors that occurred during the fetch.
     */
    private class TrailerResults implements MovieFetcherCallback<Trailer>{
        @Override
        public void updateList(List<Trailer> trailers) {


            mParentLayout.setVisibility(View.VISIBLE);
            mErrorLayout.setVisibility(View.GONE);

            mFailedOperation = null;

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

            switch (errorCondition){
                case NETWORK_CONNECTIVITY:

                    if(mErrorLayout.getVisibility() == View.GONE){
                        mErrorText.setText(getResources().getString(R.string.internet_error));
                        mTrailerLoadingSpinner.setVisibility(View.GONE);
                        mErrorLayout.setVisibility(View.VISIBLE);
                        mParentLayout.setVisibility(View.GONE);
                        mFailedOperation = NetworkOperations.TRAILER;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
