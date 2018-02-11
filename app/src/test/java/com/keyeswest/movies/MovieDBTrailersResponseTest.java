package com.keyeswest.movies;


import com.keyeswest.movies.models.MovieDBTrailersResponse;

import org.junit.Assert;
import org.junit.Test;



public class MovieDBTrailersResponseTest {



    @Test
    public void trailerDeserialization(){

        String json = "{\"id\":198663,\"results\":[{\"id\":\"5723d43c9251413eaf003719\"," +
                "\"iso_639_1\":\"en\",\"iso_3166_1\":\"US\",\"key\":\"64-iSYVmMVY\"," +
                "\"name\":\"Official Trailer\",\"site\":\"YouTube\"," +
                "\"size\":1080,\"type\":\"Trailer\"}," +
                "{\"id\":\"5723d4c6c3a368222d000296\",\"iso_639_1\":\"en\"," +
                "\"iso_3166_1\":\"US\",\"key\":\"3b946aGm0zs\",\"name\":\"Official Trailer 2\"," +
                "\"site\":\"YouTube\",\"size\":1080,\"type\":\"Trailer\"}]}";
        MovieDBTrailersResponse response = MovieDBTrailersResponse.parseJSON(json);
        Assert.assertNotNull(response);
    }
}
