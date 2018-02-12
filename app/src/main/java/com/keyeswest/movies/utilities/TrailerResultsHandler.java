package com.keyeswest.movies.utilities;

import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.models.Trailer;

import java.util.List;


public class TrailerResultsHandler extends ResultsHandler{


    public TrailerResultsHandler(MovieFetcherCallback trailerCallback ){

        super(trailerCallback);
        mCallback = trailerCallback;
    }
    @Override
    public void jsonResult(String jsonResult) {
        List<Trailer> trailers = MovieJsonUtilities.parseTrailerItemJson(jsonResult);
        mCallback.updateList(trailers);
    }


}
