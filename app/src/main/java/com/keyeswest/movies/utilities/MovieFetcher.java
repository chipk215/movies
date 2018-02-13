package com.keyeswest.movies.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.keyeswest.movies.BuildConfig;
import com.keyeswest.movies.fragments.MovieListFragment;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;

import com.keyeswest.movies.models.Movie;

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
public class MovieFetcher implements PageDataCallback {
    private static final String TAG = MovieFetcher.class.getSimpleName();

    private static final String MOVIE_DB_URL=
            "https://api.themoviedb.org/3/movie/";

    private static final String MOVIE_IMAGE_URL = "http://image.tmdb.org/t/p";

    private static final String API_KEY_PARAM = "api_key";
    private static final String PAGE_PARAM = "page";

    private static final String POPULAR_ENDPOINT = "popular";
    private static final String TOP_RATED_ENDPOINT = "top_rated";

    private static final String VIDEO_PATH = "videos";

    private MovieFetcherCallback mFetcherCallback;
    private ListAsyncTask.ResultsCallback mResultsCallback;

    private final Context mContext;

    // Save the endpoint and page number for next page request
    private String mMovieEndpoint;

    // The current page of data being fetched from MovieDB
    private int mCurrentMoviePage;

    // The total number of pages available for the endpoint
    private int mTotalMoviePages;


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

    public static URL buildTrailerURL(int movieId){
        URL url = null;

        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(Integer.toString(movieId))
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
        mCurrentMoviePage = 1;
        mTotalMoviePages = 1;
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
    public void fetchMovieTrailers(int movieId, MovieFetcherCallback callback){

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

        mFetcherCallback = callback;
        int requestPageNumber = 1;
        setMovieEndpoint(filter);

        URL moviesURL = buildMoviesURL(requestPageNumber);
        new ListAsyncTask(mContext,new MovieResultsHandler(mFetcherCallback,this)).execute(moviesURL);
    }



    /**
     * Provides mechanism for fetching the next page of movie data from the endpoint established
     * in fetchFirstMoviePage. The page state data is kept in MovieFetcher.
     */
    public  void fetchNextMoviePage(){

        Log.i(TAG, "fetchNextMoviePage");

        int lastPageFetched = mCurrentMoviePage;
        int nextPage = lastPageFetched + 1;
        if (nextPage <= mTotalMoviePages){
            Log.i(TAG, "fetching page: "+ nextPage);
            URL moviesURL = buildMoviesURL(nextPage);
            new ListAsyncTask(mContext,new MovieResultsHandler(mFetcherCallback,this)).execute(moviesURL);
        }else {

            //return empty list if last page has been retrieved
            mFetcherCallback.updateList(new ArrayList<Movie>());
        }
    }


    public void setTotalMoviePages(int totalMoviePages) {
        mTotalMoviePages = totalMoviePages;
    }

    public void setCurrentMoviePage(int currentMoviePage) {
        mCurrentMoviePage = currentMoviePage;
    }

    private void setMovieEndpoint(MovieListFragment.MovieFilter filter){
        switch (filter){
            case POPULAR: mMovieEndpoint = POPULAR_ENDPOINT;
                break;
            case TOP_RATED: mMovieEndpoint = TOP_RATED_ENDPOINT;
        }
    }

}
