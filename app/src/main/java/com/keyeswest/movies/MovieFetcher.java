package com.keyeswest.movies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.keyeswest.movies.fragments.MovieListFragment;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.tasks.FetchMovieDataTask;

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

    private MovieFetcherCallback mFetcherCallback;

    private final Context mContext;

    // Save the endpoint and page number for next page request
    private String mEndpoint;

    // The current page of data being fetched from MovieDB
    private int mCurrentPage;

    // The total number of pages available for the endpoint
    private int mTotalPages;


    // Ultimately we should proxy calls to MovieDB from a server or AWS lambda function and not
    // have the api key in the app
    // public for testing
    @SuppressWarnings("FieldCanBeLocal")
    private static final String API_KEY = BuildConfig.API_KEY;

    private URL buildURL(int requestPageNumber){

        Uri uri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendPath(mEndpoint)
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

    public MovieFetcher(Context context){
        mContext = context;
        mCurrentPage = 1;
        mTotalPages = 1;
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

    /** Initial request to an endpoint for Page 1 data
     *
     * @param filter identifies the endpoint (popular or top rated)
     * @param callback provides mechanism for passing results back to caller
     */
    public void fetchFirstPage(MovieListFragment.MovieFilter filter,
                               MovieFetcherCallback callback){

        Log.i(TAG, "fetchFirstPage");
        mFetcherCallback = callback;
        int requestPageNumber = 1;
        setEndpoint(filter);

        URL moviesURL = buildURL(requestPageNumber);

        new FetchMovieDataTask(mContext, mFetcherCallback, this).execute(moviesURL);
    }

    /**
     * Provides mechanism for fetching the next page of movie data from the endpoint established
     * in fetchFirstPage. The page state data is kept in MovieFetcher.
     */
    public  void fetchNextPage(){

        Log.i(TAG, "fetchNextPage");

        int lastPageFetched = mCurrentPage;
        int nextPage = lastPageFetched + 1;
        if (nextPage <= mTotalPages){
            Log.i(TAG, "fetching page: "+ nextPage);
            URL moviesURL = buildURL(nextPage);
            new FetchMovieDataTask(mContext, mFetcherCallback,this).execute(moviesURL);
        }else {

            //return empty list if last page has been retrieved
            mFetcherCallback.updateMovieList(new ArrayList<Movie>());
        }
    }


    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public void setCurrentPage(int currentPage) {
        mCurrentPage = currentPage;
    }

    private void setEndpoint(MovieListFragment.MovieFilter filter){
        switch (filter){
            case POPULAR: mEndpoint = POPULAR_ENDPOINT;
                break;
            case TOP_RATED: mEndpoint = TOP_RATED_ENDPOINT;
        }
    }

}
