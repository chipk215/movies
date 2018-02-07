package com.keyeswest.movies.fragments;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keyeswest.movies.DetailMovieActivity;
import com.keyeswest.movies.MovieFetcher;
import com.keyeswest.movies.R;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.models.Movie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;


public class MovieListFragment extends Fragment implements MovieFetcherCallback {

    private static final String TAG = "MovieListFragment";
    private static final int NUMBER_COLUMNS = 3;

    private static final String SUB_TITLE_KEY = "subTitleKey";

    private boolean mIsLoading = false;

    public enum MovieFilter{
        POPULAR, TOP_RATED
    }

    private RecyclerView mMovieRecyclerView;

    private final List<Movie> mItems = new ArrayList<>();

    private MovieFetcher mMovieFetcher;

    private ProgressBar mLoadingSpinner;

    private LinearLayout mErrorLayout;

    private TextView mErrorText;

    private MovieFilter mCurrentFilter;

    private ActionBar mActionBar;


    // Implement MovieListFragment as a Singleton (although the default constructor cannot be private)
    public static MovieListFragment newInstance(){
        Log.i(TAG, "New MovieListFragment Instance");
        return new MovieListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "MovieListFragment onCreate");

        // Notes (for the future):
        // I first attempted to manually save the state of MovieListFragment in onSaveInstanceState
        // and learned a few things:
        //  1) A fragment holding a reference to a POJO is required to also save the state of the
        //     POJO on a rotation (unless the POJO is a singleton). A bit cumbersome
        //     but feasible (I did it). But maybe using POJOs is not a great idea and
        //     references should be to other (headless)fragments which could save their state.
        //
        //  2) More of an issue is reconstituting the paged MovieDB data on a rotation. I initially
        //     saved the last paged fetched 'N' from MovieDB on rotation which maintained the view, but
        //     the preceding pages were not fetched and so pages 1 through N-1 were not available
        //     after rotation, unless they were also pre-fetched or by enabling the user to
        //     effectively reverse page when scrolling up... interesting but not ready to tackle now.
        //
        //  3) I somewhat regret using Async task to handle the MovieDB fetches rather than
        //     using a dedicated background thread which would enable MovieListFragment to easily queue
        //     up page requests. Perhaps I'll change in Phase II when I learn more about saving
        //     state and using fragments.
        //
        //  For now setRetainInstance(true) seems to handle everything except the subtitle
        setRetainInstance(true);

        mMovieFetcher = new MovieFetcher(getContext());

        mCurrentFilter = MovieFilter.POPULAR;

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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "MovieListFragment onActivityCreated");
    }



    @Override
    public void onDestroy(){
        mMovieRecyclerView.clearOnScrollListeners();
        super.onDestroy();

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

       // mActionBar=(Toolbar)view.findViewById(R.id.my_toolbar);
        //noinspection ConstantConditions
        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

       // ((AppCompatActivity)getActivity()).setSupportActionBar(mActionBar);

        // FYI to change the main title https://stackoverflow.com/a/26506858/9128441
        // ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Main Title");
        //noinspection ConstantConditions
        mActionBar.setSubtitle(subTitle);

        mMovieRecyclerView =  view.findViewById(R.id.movie_recycler_view);

        mMovieRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), NUMBER_COLUMNS));

        mLoadingSpinner = view.findViewById(R.id.loading_spinner);

        mErrorLayout = view.findViewById(R.id.error_layout);

        mErrorText = view.findViewById(R.id.error_txt_cause);

        Button retryButton = view.findViewById(R.id.error_btn_retry);

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_movie_sort_order, menu);
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
    public void updateMovieList(List<Movie> movieItemList) {

        if (mMovieRecyclerView.getVisibility() != View.VISIBLE){
            mMovieRecyclerView.setVisibility(View.VISIBLE);
        }

        // update the view only if movie data was fetched from MovieDB
        if ((movieItemList != null) && (! movieItemList.isEmpty())){

            mItems.addAll(movieItemList);

            mMovieRecyclerView.getAdapter().notifyItemInserted(mItems.size()-1);
        }

        // movie data fetching has stopped so hide the progress bar
        hideLoadingSpinner();
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


    private class MovieHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, Target{

        private final ImageView mItemImageView;
        private Movie mMovie;

        MovieHolder(View itemView){
            super(itemView);

            mItemImageView = itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        void bindMovieItem(Movie movieItem){
            mMovie = movieItem;
            String posterPath = movieItem.getPosterPath();
            String imagePath = MovieFetcher.getPosterPathURL(posterPath);
            Log.i(TAG, imagePath);
            Picasso.with(getContext()).load(imagePath).into(this);
        }

        @Override
        public void onClick(View v){
            //Toast.makeText(getContext(),"Movie clicked", Toast.LENGTH_SHORT).show();
            Intent intent= DetailMovieActivity.newIntent(getContext(),mMovie);

            try{
                startActivity(intent);

            }catch (ActivityNotFoundException anf){
                Log.e(TAG, "Activity not found" + anf);
            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mItemImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // TODO:  provide an error icon, raise network connectivity dialog

            // at this point the errorDrawable has not been set so we can't use it as a substitute

            // internet connectivity failure here is a corner case since we just got the
            // movie data with network error protection but this case should be handled
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }


    private class MovieAdapter extends RecyclerView.Adapter<MovieHolder>{

        private final List<Movie> mMovieItems;

        MovieAdapter(List<Movie> movieItems){
            mMovieItems = movieItems;
        }

        @Override
        public MovieHolder  onCreateViewHolder(ViewGroup viewGroup, int viewType){

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_movie_image,
                    viewGroup, false);

            return new MovieHolder(view);
        }


        @Override
        public void onBindViewHolder(MovieHolder movieHolder, int position){
            Movie movieItem = mMovieItems.get(position);
            movieHolder.bindMovieItem(movieItem);
        }

        @Override
        public int getItemCount(){
            return mMovieItems.size();
        }

    }


    private void setupMovieAdapter(){

        // isAdded() confirms that the fragment has been attached to an activity
        if (isAdded()){
            mMovieRecyclerView.setAdapter(new MovieAdapter(mItems));
            mMovieRecyclerView.setItemAnimator(new DefaultItemAnimator());

            mMovieRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                    int firstVisibleItem = ((GridLayoutManager)layoutManager).findFirstVisibleItemPosition();
                    Log.i(TAG,"firstVisibleItem= " + firstVisibleItem);

                    if ((totalItemCount - visibleItemCount) <= firstVisibleItem){
                        Log.i(TAG, "Get next page...");

                        if (!mIsLoading){

                            updateItems(false);

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
     *                  in fetchFirstPage.
     */
    private void updateItems(Boolean firstPage){

        mErrorLayout.setVisibility(View.GONE);
        mIsLoading = true;
        mLoadingSpinner.setVisibility(View.VISIBLE);
        if (firstPage){
            mMovieFetcher.fetchFirstPage(mCurrentFilter, this);
        }else{
            mMovieFetcher.fetchNextPage();
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
