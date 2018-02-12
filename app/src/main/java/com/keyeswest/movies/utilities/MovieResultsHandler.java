package com.keyeswest.movies.utilities;


import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.tasks.ListAsyncTask;

import java.util.List;

public class MovieResultsHandler implements ListAsyncTask.ResultsCallback{

    PageDataCallback mPageDataCallback;
    MovieFetcherCallback mMovieCallback;


    public MovieResultsHandler(MovieFetcherCallback movieCallback ,PageDataCallback pageCallback){
        mPageDataCallback = pageCallback;
        mMovieCallback = movieCallback;
    }

    @Override
    public void jsonResult(String jsonResult) {
        List<Movie> movies = MovieJsonUtilities.parseMovieItemJson(jsonResult, mPageDataCallback);
        mMovieCallback.updateMovieList(movies);

    }

    @Override
    public void downloadErrorOccurred(ErrorCondition errorMessage) {
        mMovieCallback.downloadErrorOccurred(MovieFetcherCallback.ErrorCondition.NETWORK_CONNECTIVITY);
    }
}