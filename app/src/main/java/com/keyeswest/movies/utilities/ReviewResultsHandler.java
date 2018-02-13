package com.keyeswest.movies.utilities;


import com.keyeswest.movies.interfaces.MovieFetcherCallback;
import com.keyeswest.movies.interfaces.PageDataCallback;

import com.keyeswest.movies.models.Review;

import java.util.List;

public class ReviewResultsHandler extends ResultsHandler {

    PageDataCallback mPageDataCallback;

    public ReviewResultsHandler(MovieFetcherCallback fetcherCallback , PageDataCallback pageCallback){
        super(fetcherCallback);
        mPageDataCallback = pageCallback;
        mCallback = fetcherCallback;
    }

    @Override
    public void jsonResult(String jsonResult) {
        List<Review> reviews = MovieJsonUtilities.parseReviewItemJson(jsonResult, mPageDataCallback);
        mCallback.updateList(reviews);

    }
}
