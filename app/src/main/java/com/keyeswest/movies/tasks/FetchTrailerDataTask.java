package com.keyeswest.movies.tasks;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.keyeswest.movies.interfaces.TrailerFetcherCallback;
import com.keyeswest.movies.models.Trailer;
import com.keyeswest.movies.utilities.MovieJsonUtilities;
import com.keyeswest.movies.utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchTrailerDataTask extends AsyncTask<URL, Void, List<Trailer>> {

    private static final String TAG = "FetchTrailerDataTask";

    final private Context mContext;
    final private TrailerFetcherCallback mTrailerFetcherCallback;

    public FetchTrailerDataTask(Context context, TrailerFetcherCallback trailerFetcherCallback){
        mContext = context;
        mTrailerFetcherCallback = trailerFetcherCallback;
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
            mTrailerFetcherCallback.downloadErrorOccurred(TrailerFetcherCallback.ErrorCondition.NETWORK_CONNECTIVITY);
            cancel(true);
        }
    }

    @Override
    protected List<Trailer> doInBackground(URL... urls) {
        if ((urls.length== 1) &&(urls[0] != null)) {
            return fetchTrailers(urls[0]);
        }

        // return empty list if no URL has been provided
        return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(List<Trailer> items){
        mTrailerFetcherCallback.updateTrailerList(items);
    }

    private List<Trailer> fetchTrailers(URL trailersURL){
        String jsonResults;
        try {
            jsonResults = NetworkUtilities.getResponseFromHttpUrl(trailersURL);
            Log.i(TAG, jsonResults);
            return MovieJsonUtilities.parseTrailerItemJson(jsonResults);
        }catch(IOException ie){
            Log.e(TAG, "Error retrieving movie data" + ie);
            return new ArrayList<>();
        }
    }
}
