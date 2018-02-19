package com.keyeswest.movies.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


public class MovieListFragment extends Fragment implements MovieFetcherCallback<Movie> {

    private static final String TAG = "MovieListFragment";

    private static final int NUMBER_COLUMNS = 3;

    private static final String SUB_TITLE_KEY = "subTitleKey";

    private boolean mIsLoading = false;

    private boolean mShowFavoriteMenu;

    public enum MovieFilter{
        POPULAR, TOP_RATED, FAVORITE
    }

    @BindView(R.id.movie_recycler_view) RecyclerView mMovieRecyclerView;

    private final List<Movie> mItems = new ArrayList<>();

    private MovieFetcher mMovieFetcher;

    @BindView(R.id.review_loading_spinner) ProgressBar mLoadingSpinner;

    @BindView(R.id.error_layout) LinearLayout mErrorLayout;

    @BindView(R.id.error_txt_cause) TextView mErrorText;

    private MovieFilter mCurrentFilter;

    private ActionBar mActionBar;

    private Unbinder mUnbinder;

    private MovieRepo mMovieRepo;

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

        //noinspection ConstantConditions
        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        mActionBar.setSubtitle(subTitle);

        mMovieRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), NUMBER_COLUMNS));

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
        MenuItem favorite = menu.findItem(R.id.favorite_type);
        favorite.setVisible(mShowFavoriteMenu);

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
        String subTitle;
        switch(item.getItemId()){

            case R.id.popular_type:
                //noinspection ConstantConditions
                subTitle= getContext().getResources().getString(R.string.popular);
                changeMovieData(MovieFilter.POPULAR, subTitle);

                return true;

            case R.id.top_rated_type:

                //noinspection ConstantConditions
                subTitle = getContext().getResources().getString(R.string.top_rated);
                changeMovieData(MovieFilter.TOP_RATED, subTitle);
                return true;

            case R.id.favorite_type:
                subTitle = getContext().getResources().getString(R.string.favorite);
                changeMovieData(MovieFilter.FAVORITE, subTitle);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                    Intent intent;
                    if (mCurrentFilter == MovieFilter.FAVORITE){
                        intent =DetailMovieActivity.newIntent(getContext(), movie.getId());

                    }else{
                        intent= DetailMovieActivity.newIntent(getContext(),movie);
                    }


                    try{
                        startActivity(intent);

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


    /**
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
