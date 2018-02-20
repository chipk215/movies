package com.keyeswest.movies.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.keyeswest.movies.DetailMovieActivity;
import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.repos.MovieRepo;
import com.keyeswest.movies.utilities.MovieFetcher;
import com.keyeswest.movies.R;
import com.keyeswest.movies.adapters.MovieAdapter;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.models.Movie;


import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;


public class MovieListFragment extends Fragment implements MovieFetcherCallback<Movie> {

    private static final String TAG = "MovieListFragment";

    // In the future figure out how to choose columns based upon display size and orientation
    private static final int NUMBER_COLUMNS = 3;

    // random number to identify fragment results expected from movie detail activity
    private static final int DETAIL_ACTIVITY = 200;

    // Used to restore subtitle on rotation
    private static final String SUB_TITLE_KEY = "subTitleKey";

    // true when loading items from the cloud
    private boolean mIsLoading = false;

    // state variable indicating whether favorite menu item should be displayed (only if favorites
    // have been saved
    private boolean mShowFavoriteMenu;

    // Defines the available filtered views of the movie list fragment
    public enum MovieFilter{
        POPULAR, TOP_RATED, FAVORITE
    }


    @BindView(R.id.movie_recycler_view) RecyclerView mMovieRecyclerView;

    private final List<Movie> mItems = new ArrayList<>();

    private MovieFetcher mMovieFetcher;

    @BindView(R.id.review_loading_spinner) ProgressBar mLoadingSpinner;

    @BindView(R.id.error_layout) LinearLayout mErrorLayout;

    @BindView(R.id.error_txt_cause) TextView mErrorText;

    @BindView(R.id.no_favorite_layout)ConstraintLayout mNoFavoritesView;

    @BindView(R.id.top_rated_button) Button mTopRatedButton;

    @BindView(R.id.popularButton)Button mPopularButton;

    private MovieFilter mCurrentFilter;

    private ActionBar mActionBar;

    private Unbinder mUnbinder;

    private MovieRepo mMovieRepo;

    private MenuItem mFavoriteItem;

    public static MovieListFragment newInstance(){
        Log.i(TAG, "New MovieListFragment Instance");
        return new MovieListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Stetho.initializeWithDefaults(getContext());
        Log.i(TAG, "MovieListFragment onCreate");

        //  For now setRetainInstance(true) seems to handle everything except the subtitle
        setRetainInstance(true);

        mMovieFetcher = new MovieFetcher(getContext());

        mCurrentFilter = MovieFilter.POPULAR;

        mMovieRepo = new MovieRepo(getContext());


        setHasOptionsMenu(true);

    }

    /**
     * Saves tool bar subtitle state during rotation
     * @param outState - bundle with subtitle string
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);

        CharSequence title = mActionBar.getSubtitle();
        if (title != null){
            String subTitle = mActionBar.getSubtitle().toString();
            outState.putString(SUB_TITLE_KEY, subTitle);
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        Log.i(TAG, "onCreateView invoked");
        String subTitle="";

        // initially assign the subtitle to Popular
        @SuppressWarnings("ConstantConditions") Resources resources = getContext().getResources();
        if (resources != null){
            subTitle = resources.getString(R.string.popular);
        }

        // overwrite the subtitle if reassigned during rotation
        if (savedInstanceState != null){
            subTitle= savedInstanceState.getString(SUB_TITLE_KEY, subTitle);
        }

        View view = inflater.inflate(R.layout.movie_list_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        // Hide the view which handles the scenario where the user un-favors the last favorite
        // movie in the detail movie view and then returns to the list view.  This view
        // is displayed instead of an empty list.
        mNoFavoritesView.setVisibility(View.GONE);

        // Helper button to help the user move on to the Popular filter view if in the empty favorite
        // scenario described above.
        mPopularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoFavoritesView.setVisibility(View.GONE);
                popularSelected();
            }
        });

        // Helper button to help the user move on to the YTopRated filter view if in the empty favorite
        // scenario described above.
        mTopRatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoFavoritesView.setVisibility(View.GONE);
                topRatedSelected();
            }
        });

        //noinspection ConstantConditions
        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        mActionBar.setSubtitle(subTitle);

        mMovieRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), NUMBER_COLUMNS));

        // Retry button when network is unavailable
        Button retryButton = view.findViewById(R.id.error_btn_retry);

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentPage = mMovieFetcher.getCurrentMoviePage();
                if (currentPage  > 1){
                    updateItems(false);
                }else{
                    updateItems(true);
                }
                updateItems(true);
            }
        });

        setupMovieAdapter();

        // Can not start loading in onCreate because of the Progress Bar
        //   -- starting and stopping the spinner is problematic if the download is initiated
        //      in onCreate
        updateItems(true);

        return view;

    }

    @Override
    public void onResume(){
        super.onResume();
        mMovieRepo.getMovieCount(new MovieRepo.QueryCountResult() {
            @Override
            public void movieResult(int recordCount) {
                Log.i(TAG, "Number of favorites= " + recordCount);
                if (recordCount > 0){
                    mShowFavoriteMenu = true;
                }else{
                    mShowFavoriteMenu = false;
                }

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_movie_sort_order, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        mFavoriteItem = menu.findItem(R.id.favorite_type);
        mFavoriteItem.setVisible(mShowFavoriteMenu);

    }


    /**
     * Handle menu item selection which determines the ordering of the movie data.
     * The user can choose to view movies by popularity or their rating. MovieDB
     * provides different endpoints for these two selections.
     *
     * @param item - the item selected in the menu
     * @return default true value
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        mNoFavoritesView.setVisibility(View.GONE);
        switch(item.getItemId()){

            case R.id.popular_type:
                popularSelected();
                return true;

            case R.id.top_rated_type:
                topRatedSelected();
                return true;

            case R.id.favorite_type:
                String subTitle = getContext().getResources().getString(R.string.favorite);
                changeMovieData(MovieFilter.FAVORITE, subTitle);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*
     * Refactored to be used by both the menu handler and for dealing with empty favorite list
     * when returning from detail fragment.
     */
    private void popularSelected(){
        String subTitle;
        //noinspection ConstantConditions
        subTitle= getContext().getResources().getString(R.string.popular);
        changeMovieData(MovieFilter.POPULAR, subTitle);
    }

    /*
     * Refactored to be used by both the menu handler and for dealing with empty favorite list
     * when returning from detail fragment.
     */
    private void topRatedSelected(){
        String subTitle;
        //noinspection ConstantConditions
        subTitle = getContext().getResources().getString(R.string.top_rated);
        changeMovieData(MovieFilter.TOP_RATED, subTitle);
    }


    /**
     * This method implements part of the MovieFetcherCallback interface and is invoked
     * with movie data results fetched from MovieDB.
     *
     * @param movieItemList list of movies fetched from MovieDB site
     */
    @Override
    public void updateList(List<Movie> movieItemList) {

        // movie data fetching has stopped so hide the progress bar
        hideLoadingSpinner();

        if (mMovieRecyclerView.getVisibility() != View.VISIBLE){
            mMovieRecyclerView.setVisibility(View.VISIBLE);
        }

        // update the view only if movie data was fetched from MovieDB
        if ((movieItemList != null) && (! movieItemList.isEmpty())){

            //-------------------------------------------------------------------------------------
            // TL;DR
            // My movie paging scheme is bad.
            //-------------------------------
            // I will definitely take a different approach in the future.
            // Here is what's wrong, the recycler list provides an efficient mechanism for only
            // creating a limited number of views yet the movieItemList in this implementation will
            // grow to be the size of the movie list in the cloud since movies are simple appended
            // onto the list as new pages are loaded as the user scrolls.
            //
            // The paging algorithm should be changed so that the movie list itself only holds perhaps
            // three times the number of visible items that the recycler list supports. A third
            // of the items should be the items needed for the previous page, a third for the
            // currently visible items, and the final third cached for the next page.
            //
            // A sliding window should drop a third of the cached views whenever the user scrolls
            // up or down and replace them with a page of views such that only 3 consecutive page views
            // are held in the movie item list.  Deleting and adding views will also mitigate
            // issues that arise when the server reorders the movie lists in response to updated
            // popularity or voting feedback.  I've seen duplicates in my list because my list holds
            // on to the cached movie data too long.
            //
            // I don't know how to compute the number of visible items the recycler list supports
            // for a given layout but I think the computations could be done at run time.
            //
            // I'm pretty comfortable with algorithms so I'm postponing the implementation right
            // now because there is so much Android specific content I need to get on with as part
            // of this class. If the opportunity arises in the next project or the final project I'll
            // tackle this.
            //
            mItems.addAll(movieItemList);
            mMovieRecyclerView.getAdapter().notifyItemInserted(mItems.size()-1);

        }

    }


    /**
     * This method implements part of the MovieFetcherCallback interface and is invoked when
     * an error occurs fetching movie data from MovieDB.
     * @param errorCondition - error condition raised during download
     */
    @Override
    public void downloadErrorOccurred(ErrorCondition errorCondition) {
        //TODO Handle
        Log.e(TAG, "Download error occurred: "+ errorCondition);

        switch (errorCondition){
            case NETWORK_CONNECTIVITY:

                if(mErrorLayout.getVisibility() == View.GONE){
                    mErrorText.setText(getResources().getString(R.string.internet_error));
                    mLoadingSpinner.setVisibility(View.GONE);
                    mErrorLayout.setVisibility(View.VISIBLE);
                    mMovieRecyclerView.setVisibility(View.GONE);
                }

                break;
        }

        hideLoadingSpinner();
    }


    /**
     * Handles the returned intent data from the movie detail fragment when viewing a favorite movie.
     * @param requestCode - corresponding intent code
     * @param resultCode - success
     * @param data - intent containing a boolean extra and a long integer extra
     *             - the boolean extra indicates whether the user unfavored the movie
     *             - the long integer corresponds to the movie id of the unfavored movie
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == DETAIL_ACTIVITY) && (resultCode == RESULT_OK)){

            boolean wasRemoved = DetailMovieActivity.favoriteWasRemoved(data);
            if (wasRemoved){

                // remove it from the list of movies being displayed
                long movieId = DetailMovieActivity.getMovieId(data);

                int index = findMovieInList(movieId);

                // -1 indicates the movie could not be found in the list, an unexpected error
                // condition

                if (index != -1) {

                    // remove the movie from the list
                    mItems.remove(index);
                    mMovieRecyclerView.getAdapter().notifyItemRemoved(index);

                    if (mItems.isEmpty()){

                        // if after removing the favorite movie there are no more favorite
                        // movies to show then display a no favorite message and guide
                        // the user to viewing popular or top rated movies
                        mNoFavoritesView.setVisibility(View.VISIBLE);
                        mFavoriteItem.setVisible(false);
                    }

                }
            }
        }
    }


    /*
     * Just a quick linear search for a movie in the favorites list.
     * This could be optimized (hashed) to handle large lists.
     */
    private int findMovieInList(long movieId){
        int result = -1;

        for (int i=0; i< mItems.size(); i++){
            if (mItems.get(i).getId() == movieId){
                return i;
            }
        }
        return result;
    }


    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
    }


    private void setupMovieAdapter(){

        // isAdded() confirms that the fragment has been attached to an activity
        if (isAdded()){
            mMovieRecyclerView.setAdapter(new MovieAdapter(mItems, new MovieAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Movie movie) {
                    try{
                        Intent intent;
                        if (mCurrentFilter == MovieFilter.FAVORITE){
                            intent =DetailMovieActivity.newIntent(getContext(), movie.getId());
                            startActivityForResult(intent,DETAIL_ACTIVITY);

                        }else{
                            intent= DetailMovieActivity.newIntent(getContext(),movie);
                            startActivity(intent);
                        }

                    }catch (ActivityNotFoundException anf){
                            Log.e(TAG, "Activity not found" + anf);
                        }
                }
            }));
            mMovieRecyclerView.setItemAnimator(new DefaultItemAnimator());

            mMovieRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                    super.onScrolled(recyclerView, dx, dy);

                    // No paging for favorites pulled from database, we are currently returning all
                    // the favorites in the db for simplicity.
                    if (mCurrentFilter != MovieFilter.FAVORITE) {

                        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                        //current number of child views attached to the parent RecyclerView
                        int visibleItemCount = layoutManager.getChildCount();
                        Log.i(TAG, "visibleCount= " + visibleItemCount);

                        //number of items in the adapter bound to the parent RecyclerView
                        int totalItemCount = layoutManager.getItemCount();
                        Log.i(TAG, "totalItemCount= " + totalItemCount);

                        int firstVisibleItem = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                        Log.i(TAG, "firstVisibleItem= " + firstVisibleItem);

                        if ((totalItemCount - visibleItemCount) <= firstVisibleItem) {
                            Log.i(TAG, "Get next page...");

                            if (!mIsLoading) {

                                updateItems(false);

                            }
                        }
                    }
                }
            });
        }
    }


    /*
     * Initiates the fetching of paged movie data from MovieDB.
     *
     * @param firstPage - identifies whether the request is for first page data or if false to
     *                  fetch the next page. The paging state data is maintained in in
     *                  MovieFetcher and includes the current endpoint (popular or top rated),
     *                  the last page of data fetched, and the callback to this fragment provided
     *                  in fetchFirstMoviePage.
     */
    private void updateItems(Boolean firstPage){

        mErrorLayout.setVisibility(View.GONE);

        if (firstPage){
            // this prevents reloading the list after a rotation
            if (mItems.isEmpty()) {
                mMovieFetcher.fetchFirstMoviePage(mCurrentFilter, this);
                mIsLoading = true;
                mLoadingSpinner.setVisibility(View.VISIBLE);
            }
        }else{
            mMovieFetcher.fetchNextMoviePage();
            mIsLoading = true;
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingSpinner(){
        mLoadingSpinner.setVisibility(View.GONE);

        mIsLoading = false;
    }

    private void clearMovieDataList(){
        int size = mItems.size();
        mItems.clear();
        mMovieRecyclerView.getAdapter().notifyItemRangeRemoved(0,size);

    }

    private void changeMovieData(MovieFilter filter, String subTitle){

        if (mCurrentFilter != filter) {

            // Clear the list, switch endpoints, and update toolbar

            clearMovieDataList();
            mActionBar.setSubtitle(subTitle);
            mCurrentFilter = filter;
            updateItems(true);
        }
    }

}
