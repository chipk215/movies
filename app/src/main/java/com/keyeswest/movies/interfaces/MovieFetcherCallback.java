package com.keyeswest.movies.interfaces;


import com.keyeswest.movies.ErrorCondition;
import com.keyeswest.movies.models.Movie;

import java.util.List;

public interface MovieFetcherCallback<T> {

    ErrorCondition errorCondition= ErrorCondition.NETWORK_CONNECTIVITY;
    void updateList(List<T> itemList);

    void downloadErrorOccurred(ErrorCondition errorMessage);


}
