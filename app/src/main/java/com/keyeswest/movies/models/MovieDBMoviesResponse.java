package com.keyeswest.movies.models;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieDBMoviesResponse {

    @SerializedName("results")
    private List<Movie> movies;

    public static MovieDBMoviesResponse parseJSON(String response){
        Gson gson = new GsonBuilder().create();

        MovieDBMoviesResponse movieDBMoviesResponse =
                gson.fromJson(response, MovieDBMoviesResponse.class);

        return movieDBMoviesResponse;

    }

    public MovieDBMoviesResponse(){
        movies = new ArrayList<>();
    }


    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}
