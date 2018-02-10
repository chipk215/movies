package com.keyeswest.movies.interfaces;


import com.keyeswest.movies.models.Trailer;

import java.util.List;

public interface TrailerFetcherCallback {

    enum ErrorCondition {NETWORK_CONNECTIVITY, @SuppressWarnings("unused")DOWNLOAD_ERROR}

    void updateTrailerList(List<Trailer> movieItemList);

    void downloadErrorOccurred(ErrorCondition errorMessage);
}
