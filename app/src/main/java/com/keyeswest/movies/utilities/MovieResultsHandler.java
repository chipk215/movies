package com.keyeswest.movies.utilities;

import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;

import java.util.List;

public class MovieResultsHandler extends ResultsHandler{

    PageDataCallback mPageDataCallback;


    public MovieResultsHandler(MovieFetcherCallback movieCallback ,PageDataCallback pageCallback){
        super(movieCallback);
        mPageDataCallback = pageCallback;
        mCallback = movieCallback;
    }

    @Override
    public void jsonResult(String jsonResult) {
        List<Movie> movies = MovieJsonUtilities.parseMovieItemJson(jsonResult, mPageDataCallback);
        mCallback.updateList(movies);

    }


}