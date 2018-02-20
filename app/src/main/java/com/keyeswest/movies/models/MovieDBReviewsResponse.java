package com.keyeswest.movies.models;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// Helper class for gson parser. I hope we are passed manual json parsing :)
public class MovieDBReviewsResponse {

    @SerializedName("id")
    private int mId;

    @SerializedName("page")
    private int mPage;

    @SerializedName("total_pages")
    private int mTotalPages;

    @SerializedName("total_results")
    private int mTotalResults;

    @SerializedName("results")
    private List<Review> mReviews;

    public MovieDBReviewsResponse(){
        mReviews = new ArrayList<>();
    }

    public static MovieDBReviewsResponse parseJSON(String response){
        Gson gson = new GsonBuilder().create();

        MovieDBReviewsResponse MovieDBReviewsResponse =
                gson.fromJson(response, MovieDBReviewsResponse.class);

        return MovieDBReviewsResponse;

    }


    @SuppressWarnings("unused")
    public int getId() {
        return mId;
    }

    @SuppressWarnings("unused")
    public void setId(int id) {
        mId = id;
    }

    public int getPage() {
        return mPage;
    }

    @SuppressWarnings("unused")
    public void setPage(int page) {
        mPage = page;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    @SuppressWarnings("unused")
    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    @SuppressWarnings("unused")
    public int getTotalResults() {
        return mTotalResults;
    }

    @SuppressWarnings("unused")
    public void setTotalResults(int totalResults) {
        mTotalResults = totalResults;
    }

    public List<Review> getReviews() {
        return mReviews;
    }

    @SuppressWarnings("unused")
    public void setReviews(List<Review> reviews) {
        mReviews = reviews;
    }
}
