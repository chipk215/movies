package com.keyeswest.movies.utilities;


import android.util.Log;

import com.keyeswest.movies.interfaces.PageDataCallback;
import com.keyeswest.movies.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieJsonUtilities {

    private static final String TAG = "MovieJsonUtilities";

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
                JSONArray results = jsonObject.getJSONArray(resultsField);

                if (results != null){
                    for (int i=0; i< results.length(); i++){

                        JSONObject record = results.getJSONObject(i);
                        if (record != null){

                            Movie movie = parseRecord(record);

                            movieItems.add(movie);
                        }
                    }
                }
            }

            return movieItems;

        }catch (final JSONException je) {
            Log.e(TAG, "JSON parsing error: " + je);
            return movieItems;

        }

    }

    private static String parseString(JSONObject jsonObject, String field){
        if (jsonObject.has(field)){
            return jsonObject.optString(field);
        }
        return "";
    }


    private static Movie parseRecord(JSONObject record){

        // Following fields are Movie properties
        final String posterPathField = "poster_path";

        final String overviewField = "overview";
        final String releaseDate = "release_date";


        final String originalTitleField = "original_title";

        final String titleField = "title";

        final String popularityField = "popularity";
        final String voteCountField = "vote_count";
        final String videoField = "video";
        final String voteAverageField = "vote_average";


        Movie movie = new Movie();

        movie.setPosterPath(parseString(record, posterPathField));

        movie.setOverview(parseString(record,overviewField));

        movie.setReleaseDate(parseString(record, releaseDate));

        movie.setOriginalTitle(parseString(record, originalTitleField));

        movie.setTitle(parseString(record, titleField));


        if (record.has(popularityField)){
            movie.setPopularity((float)record.optDouble(popularityField));
        }

        if (record.has(voteCountField)){
            movie.setVoteCount(record.optInt(voteCountField));
        }

        if (record.has(videoField)){
            movie.setVideo(record.optBoolean(videoField));
        }

        if (record.has(voteAverageField)){
            movie.setVoteAverage((float)record.optDouble(voteAverageField));
        }

        return movie;
    }
}
