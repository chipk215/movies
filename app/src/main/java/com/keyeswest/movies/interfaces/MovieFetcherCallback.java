package com.keyeswest.movies.interfaces;


import com.keyeswest.movies.ErrorCondition;

import java.util.List;

// Generic interface supporting Movies, Trailers, and Reviews
public interface MovieFetcherCallback<T> {

    ErrorCondition errorCondition= ErrorCondition.NETWORK_CONNECTIVITY;
    void updateList(List<T> itemList);

    void downloadErrorOccurred(ErrorCondition errorMessage);


}
