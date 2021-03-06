package com.keyeswest.movies.utilities;

import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;

import java.util.List;

public class MovieResultsHandler extends ResultsHandler{

    PageDataCallback mPageDataCallback;


    public MovieResultsHandler(MovieFetcherCallback fetcherCallback ,PageDataCallback pageCallback){
        super(fetcherCallback);
        mPageDataCallback = pageCallback;
        mCallback = fetcherCallback;
    }

    @Override
    public void jsonResult(String jsonResult) {
        List<Movie> movies = MovieJsonUtilities.parseMovieItemJson(jsonResult, mPageDataCallback);
        mCallback.updateList(movies);

    }


}