package com.keyeswest.movies.utilities;


import com.keyeswest.movies.interfaces.PageDataCallback;

public class PageCounter implements PageDataCallback {

    private int mCurrentPageNumber;
    private int mTotalPages;

    public  int getCurrentPageNumber() {
        return mCurrentPageNumber;
    }

    public int getTotalPages() {
        return mTotalPages;
    }


    @Override
    public void setCurrentPage(int pageNumber) {
        mCurrentPageNumber = pageNumber;
    }

    @Override
    public void setTotalPages(int totalMoviePages) {
        mTotalPages = totalMoviePages;

    }

    public PageCounter(){
        mCurrentPageNumber = 1;
        mTotalPages = 1;
    }
}
