package com.keyeswest.movies.tasks;


import android.content.Context;
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
        if (! NetworkUtilities.isNetworkAvailable(mContext)){
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
            return jsonResults;
        }catch(IOException ie){
            Log.e(TAG, "Error retrieving movie data" + ie);
            return "";
        }
    }

}
