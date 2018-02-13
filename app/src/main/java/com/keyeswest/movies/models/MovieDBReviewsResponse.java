package com.keyeswest.movies.models;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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


    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public int getTotalResults() {
        return mTotalResults;
    }

    public void setTotalResults(int totalResults) {
        mTotalResults = totalResults;
    }

    public List<Review> getReviews() {
        return mReviews;
    }

    public void setReviews(List<Review> reviews) {
        mReviews = reviews;
    }
}
