package com.keyeswest.movies.utilities;


import com.keyeswest.movies.interfaces.TrailerFetcherCallback;
import com.keyeswest.movies.models.Trailer;
import com.keyeswest.movies.tasks.ListAsyncTask;

import java.util.List;


public class TrailerResultsHandler implements ListAsyncTask.ResultsCallback{


    TrailerFetcherCallback mTrailerCallback;


    public TrailerResultsHandler(TrailerFetcherCallback trailerCallback ){

        mTrailerCallback = trailerCallback;
    }
    @Override
    public void jsonResult(String jsonResult) {
        List<Trailer> trailers = MovieJsonUtilities.parseTrailerItemJson(jsonResult);
        mTrailerCallback.updateTrailerList(trailers);
    }

    @Override
    public void downloadErrorOccurred(ErrorCondition errorMessage) {
        mTrailerCallback.downloadErrorOccurred(TrailerFetcherCallback.ErrorCondition.NETWORK_CONNECTIVITY);

    }
}
