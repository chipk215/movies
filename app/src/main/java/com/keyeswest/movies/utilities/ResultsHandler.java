package com.keyeswest.movies.utilities;


import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.tasks.ListAsyncTask;

public abstract class ResultsHandler implements ListAsyncTask.ResultsCallback {

    protected MovieFetcherCallback mCallback;

    @Override
    public void downloadErrorOccurred(ErrorCondition errorMessage) {
        mCallback.downloadErrorOccurred(errorMessage);

    }

    ResultsHandler(MovieFetcherCallback callback ){
        mCallback = callback;
    }
}
