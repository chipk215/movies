package com.keyeswest.movies.interfaces;


import com.keyeswest.movies.models.Movie;

import java.util.List;

public interface MovieFetcherCallback {

    enum ErrorCondition {NETWORK_CONNECTIVITY, @SuppressWarnings("unused")DOWNLOAD_ERROR}

    void updateMovieList(List<Movie> movieItemList);

    void downloadErrorOccurred(ErrorCondition errorMessage);


}
