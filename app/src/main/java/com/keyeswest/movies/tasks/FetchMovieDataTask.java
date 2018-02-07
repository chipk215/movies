package com.keyeswest.movies.tasks;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;


import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.utilities.MovieJsonUtilities;
import com.keyeswest.movies.utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FetchMovieDataTask extends AsyncTask<URL, Void, List<Movie>> {
    private static final String TAG = "FetchMovieDataTask";

    @SuppressLint("StaticFieldLeak")
    final private Context mContext;
    final private MovieFetcherCallback mFetcherCallback;
    final private PageDataCallback mPageDataCallback;


    /**
     *
     * @param context - context of caller
     * @param movieFetcherCallback - callback to handle movie data results
     * @param pageDataCallback - callback to manage paged results
     */
    public FetchMovieDataTask(Context context, MovieFetcherCallback movieFetcherCallback,
                       PageDataCallback pageDataCallback ){

        mContext  = context;
        mFetcherCallback = movieFetcherCallback;
        mPageDataCallback = pageDataCallback;

    }

    @Override
    protected void onPreExecute(){

        // check network connectivity
        ConnectivityManager connectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            // If no connectivity, cancel task and update Callback with null data.
            mFetcherCallback.downloadErrorOccurred(MovieFetcherCallback.ErrorCondition.NETWORK_CONNECTIVITY);
            cancel(true);
        }
    }

    @Override
    protected List<Movie> doInBackground(URL... urls){

        if ((urls.length== 1) &&(urls[0] != null)) {
            return fetchMovies(urls[0]);
        }

        // return empty list if no URL has been provided
        return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(List<Movie> items){
        mFetcherCallback.updateMovieList(items);
    }


    /**
     * Invokes the network request to MovieDB and parses the JSON response.
     * @param moviesURL - url to access movie data
     * @return List of movie data
     */
    private  List<Movie> fetchMovies(URL moviesURL){

        String jsonResults;
        try {
            jsonResults = NetworkUtilities.getResponseFromHttpUrl(moviesURL);
            Log.i(TAG, jsonResults);
            return MovieJsonUtilities.parseMovieItemJson(jsonResults, mPageDataCallback);
        }catch(IOException ie){
            Log.e(TAG, "Error retrieving movie data" + ie);
            return new ArrayList<>();
        }

    }
}



