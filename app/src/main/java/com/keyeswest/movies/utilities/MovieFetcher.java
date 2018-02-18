package com.keyeswest.movies.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.keyeswest.movies.BuildConfig;
import com.keyeswest.movies.fragments.MovieListFragment;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;

import com.keyeswest.movies.models.Movie;

import com.keyeswest.movies.models.Review;
import com.keyeswest.movies.tasks.ListAsyncTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;



/**
 * Handles fetching data from MovieDB.
 *
 * It is presumed that the caller will hold the reference to an instance of this class for the
 * lifetime of the caller. State data is held by MovieFetcher that is needed for fetching
 * paged data results.
 */
public class MovieFetcher  {
    private static final String TAG = MovieFetcher.class.getSimpleName();

    private static final String MOVIE_DB_URL=
            "https://api.themoviedb.org/3/movie/";

    private static final String MOVIE_IMAGE_URL = "http://image.tmdb.org/t/p";

    private static final String API_KEY_PARAM = "api_key";
    private static final String PAGE_PARAM = "page";

    private static final String POPULAR_ENDPOINT = "popular";
    private static final String TOP_RATED_ENDPOINT = "top_rated";

    private static final String VIDEO_PATH = "videos";
    private static final String REVIEW_PATH = "reviews";

    private MovieFetcherCallback mMoviesFetcherCallback;
    private MovieFetcherCallback mReviewsFetcherCallback;

    private ListAsyncTask.ResultsCallback mResultsCallback;

    private final Context mContext;

    // Save the endpoint and page number for next page request
    private String mMovieEndpoint;

    private PageCounter mMoviePageCounter;

    private PageCounter mReviewPageCounter;


    @SuppressWarnings("FieldCanBeLocal")
    private static final String API_KEY = BuildConfig.API_KEY;

    private URL buildMoviesURL(int requestPageNumber){

        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(mMovieEndpoint)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(PAGE_PARAM, Integer.toString(requestPageNumber))
                .build();

        URL url = null;
        try{
            url = new URL(uri.toString());
        }catch(MalformedURLException me){
            me.printStackTrace();
        }
        return url;

    }


    private URL buildReviewURL(int requestPageNumber, long movieId){
        URL url = null;

        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(Long.toString(movieId))
                .appendPath(REVIEW_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        try{
            url = new URL(uri.toString());
        }catch(MalformedURLException me){
            me.printStackTrace();
        }

        return url;

    }

    /**
     * Constructs the URL for fetching movie trailers.
     *
     * public accessibility for unit testing
     * @param movieId - movie ID to insert in URL
     * @return themovieDB URL to fetch trailers
     */
    public static URL buildTrailerURL(long movieId){
        URL url = null;

        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(Long.toString(movieId))
                .appendPath(VIDEO_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        try{
            url = new URL(uri.toString());
        }catch(MalformedURLException me){
            me.printStackTrace();
        }
        return url;
    }


    public MovieFetcher(Context context){
        mContext = context;
    }

    public static String getPosterPathURL(String posterPath){

        //TODO revisit and use some criteria to select image size
        String imageSize = "w185";
        Uri uri = Uri.parse(MOVIE_IMAGE_URL).buildUpon()
                .appendPath(imageSize)
                .appendEncodedPath(posterPath)
               .build();

        return uri.toString();

    }

    /**
     * Fetch Movie Trailers (theMovieDB API provides only 1 page of results)
     * @param movieId - identifies the movie whose trailers are to be fetched
     * @param callback - client callback with trailers
     */
    public void fetchMovieTrailers(long movieId, MovieFetcherCallback callback){

        URL trailerURL = buildTrailerURL(movieId);

        new ListAsyncTask(mContext,new TrailerResultsHandler(callback)).execute(trailerURL);
    }



    /**
     * Fetch the first page of movie results from the endpoint specified by the filter.
     * @param filter - determines which endpoint to use e.g. popular or top rated
     * @param callback - client callback with movies
     */
    public void fetchFirstMoviePage(MovieListFragment.MovieFilter filter, MovieFetcherCallback callback){
        Log.i(TAG, "fetchFirstMoviePage");

        mMoviesFetcherCallback = callback;
        int requestPageNumber = 1;
        setMovieEndpoint(filter);

        URL moviesURL = buildMoviesURL(requestPageNumber);
        mMoviePageCounter = new PageCounter();
        new ListAsyncTask(mContext,new MovieResultsHandler(mMoviesFetcherCallback,mMoviePageCounter)).execute(moviesURL);
    }



    /**
     * Provides mechanism for fetching the next page of movie data from the endpoint established
     * in fetchFirstMoviePage. The page state data is kept in MovieFetcher.
     */
    public void fetchNextMoviePage(){

        Log.i(TAG, "fetchNextMoviePage");

       // int lastPageFetched = mCurrentMoviePage;
        int lastPageFetched = mMoviePageCounter.getCurrentPageNumber();
        int nextPage = lastPageFetched + 1;
        if (nextPage <= mMoviePageCounter.getTotalPages()){
            Log.i(TAG, "fetching page: "+ nextPage);
            URL moviesURL = buildMoviesURL(nextPage);
            new ListAsyncTask(mContext,
                    new MovieResultsHandler(mMoviesFetcherCallback,
                            mMoviePageCounter)).execute(moviesURL);
        }else {

            //return empty list if last page has been retrieved
            mMoviesFetcherCallback.updateList(new ArrayList<Movie>());
        }
    }


    public void fetchFirstReviewPage(long movieId, MovieFetcherCallback callback){
        Log.i(TAG, "fetchFirstReviewPage");
        mReviewsFetcherCallback = callback;
        int requestPageNumber = 1;
        URL reviewsURL = buildReviewURL(requestPageNumber, movieId);
        mReviewPageCounter = new PageCounter();

        new ListAsyncTask(mContext,
                new ReviewResultsHandler(mReviewsFetcherCallback,
                        mReviewPageCounter)).execute(reviewsURL);

    }

    public void fetchNextReviewPage(long movieId){
        Log.i(TAG, "fetchNextReviewPage");

        int lastPageFetched = mReviewPageCounter.getCurrentPageNumber();
        int nextPage = lastPageFetched + 1;
        if (nextPage <= mReviewPageCounter.getTotalPages()){
            Log.i(TAG, "fetching review page: "+ nextPage);
            URL reviewURL = buildReviewURL(nextPage, movieId);
            new ListAsyncTask(mContext,
                    new ReviewResultsHandler(mReviewsFetcherCallback,
                            mReviewPageCounter)).execute(reviewURL);
        }else {

            //return empty list if last page has been retrieved
            mReviewsFetcherCallback.updateList(new ArrayList<Review>());
        }

    }


    private void setMovieEndpoint(MovieListFragment.MovieFilter filter){
        switch (filter){
            case POPULAR: mMovieEndpoint = POPULAR_ENDPOINT;
                break;
            case TOP_RATED: mMovieEndpoint = TOP_RATED_ENDPOINT;
        }
    }


    public int getCurrentReviewPage(){
        if (mReviewPageCounter != null){
            return mReviewPageCounter.getCurrentPageNumber();
        }

        return 1;
    }

    public int getCurrentMoviePage(){
        if (mMoviePageCounter != null) {
            return mMoviePageCounter.getCurrentPageNumber();
        }
        return 1;

    }

}
