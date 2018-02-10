package com.keyeswest.movies;

import com.keyeswest.movies.models.MovieDBTrailersResponse;
import com.keyeswest.movies.utilities.MovieJsonUtilities;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;




/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void TrailerDeserialization(){

        String json = "{\"id\":198663,\"results\":[{\"id\":\"5723d43c9251413eaf003719\",\"iso_639_1\":\"en\",\"iso_3166_1\":\"US\",\"key\":\"64-iSYVmMVY\",\"name\":\"Official Trailer\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Trailer\"},{\"id\":\"5723d4c6c3a368222d000296\",\"iso_639_1\":\"en\",\"iso_3166_1\":\"US\",\"key\":\"3b946aGm0zs\",\"name\":\"Official Trailer 2\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Trailer\"}]}";
        MovieDBTrailersResponse response = MovieDBTrailersResponse.parseJSON(json);
        Assert.assertNotNull(response);
    }
}