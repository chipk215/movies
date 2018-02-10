package com.keyeswest.movies.utilities;


import android.util.Log;

import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.models.MovieDBMoviesResponse;
import com.keyeswest.movies.models.MovieDBTrailersResponse;
import com.keyeswest.movies.models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieJsonUtilities {

    private static final String TAG = "MovieJsonUtilities";


    public static List<Trailer> parseTrailerItemJson(String jsonString){
        List<Trailer> trailerItems = new ArrayList<>();

        if (jsonString == null){
            return trailerItems;
        }

        trailerItems = MovieDBTrailersResponse.parseJSON(jsonString).getTrailers();
        return trailerItems;

    }

    public static List<Movie> parseMovieItemJson(String jsonString, PageDataCallback callback){

        final String pageField = "page";
        final String resultsField = "results";
        final String totalPagesField = "total_pages";

        List<Movie> movieItems = new ArrayList<>();

        if (jsonString == null){
            return movieItems;
        }

        try{

            // top level object
            JSONObject jsonObject = new JSONObject(jsonString);

            // Page of results containing data
            if (jsonObject.has(pageField)){
                callback.setCurrentPage(jsonObject.optInt(pageField));
            }

            //Get total pages for pagination
            if (jsonObject.has(totalPagesField)){
                callback.setTotalPages(jsonObject.optInt(totalPagesField));
            }

            if(jsonObject.has(resultsField)){

               // JSONArray results = jsonObject.getJSONArray(resultsField);
                movieItems = MovieDBMoviesResponse.parseJSON(jsonString).getMovies();
            }

            return movieItems;

        }catch (final JSONException je) {
            Log.e(TAG, "JSON parsing error: " + je);
            return movieItems;

        }

    }


}
