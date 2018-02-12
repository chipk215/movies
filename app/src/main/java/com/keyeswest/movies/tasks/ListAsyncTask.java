package com.keyeswest.movies.tasks;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;


import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;


public class ListAsyncTask extends AsyncTask<URL, Void, String> {
    final private static String TAG="ListAsyncTask";

    final private Context mContext;
    final private ResultsCallback mResultsCallback;


    public interface ResultsCallback{
        ErrorCondition errorCondition= ErrorCondition.NETWORK_CONNECTIVITY;

        void jsonResult(String jsonResult);

        void downloadErrorOccurred(ErrorCondition errorMessage);
    }

    public ListAsyncTask(Context context, ResultsCallback resultsCallback) {
        mContext = context;
        mResultsCallback = resultsCallback;


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
            mResultsCallback.downloadErrorOccurred(ResultsCallback.errorCondition.NETWORK_CONNECTIVITY);
            cancel(true);
        }
    }

    @Override
    protected String doInBackground(URL... urls) {
        if ((urls.length== 1) &&(urls[0] != null)) {
            return fetchData(urls[0]);
        }

        // return empty string if no URL has been provided
        return "";
    }

    @Override
    protected void onPostExecute(String jsonString){
        mResultsCallback.jsonResult(jsonString);
    }

    private  String fetchData(URL dataURL){

        String jsonResults;
        try {
            jsonResults = NetworkUtilities.getResponseFromHttpUrl(dataURL);
            Log.i(TAG, jsonResults);
            //move this to client... parsing requires knowledge of list type
            return jsonResults;
        }catch(IOException ie){
            Log.e(TAG, "Error retrieving movie data" + ie);
            return "";
        }

    }


}
