package com.keyeswest.movies.models;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieDBTrailersResponse {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<Trailer> mTrailers;

    public static MovieDBTrailersResponse parseJSON(String response){
        Gson gson = new GsonBuilder().create();

        MovieDBTrailersResponse movieDBtrailersResponse =
                gson.fromJson(response, MovieDBTrailersResponse.class);

        return movieDBtrailersResponse;

    }

    public MovieDBTrailersResponse(){
        mTrailers = new ArrayList<>();
    }


    public List<Trailer> getTrailers() {
        return mTrailers;
    }

    public void setTrailers(List<Trailer> trailers) {
        this.mTrailers = trailers;
    }
}
