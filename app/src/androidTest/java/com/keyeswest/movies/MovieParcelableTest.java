package com.keyeswest.movies;


import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.models.Trailer;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static java.lang.Math.abs;

@RunWith(AndroidJUnit4.class)
public class MovieParcelableTest {

    private final static String LABEL = "ARGS";

    private Movie mMovieIn;

    @Before
    public void beforeTest(){
        mMovieIn = new Movie();
        mMovieIn.setOriginalTitle("Original Title");
        mMovieIn.setPosterPath("Poster Path");
        mMovieIn.setOverview("Overview");
        mMovieIn.setVoteAverage(2.14f);
        mMovieIn.setReleaseDate("Release Date");
        mMovieIn.setTitle("Title");
        mMovieIn.setPopularity(214.214f);
        mMovieIn.setVoteCount(214);
        mMovieIn.setVideo(true);
        mMovieIn.setId(999);
    }

    @Test
    public void serializeDeserializeMovieEmptyTrailerTest(){


        Bundle bundle = new Bundle();
        bundle.putParcelable(LABEL,mMovieIn);

        Movie movieOut = bundle.getParcelable(LABEL);

        Assert.assertNotNull(movieOut);

        Assert.assertEquals(mMovieIn.getOriginalTitle(), movieOut.getOriginalTitle());
        Assert.assertEquals(mMovieIn.getPosterPath(), movieOut.getPosterPath());
        Assert.assertEquals(mMovieIn.getOverview(), movieOut.getOverview());
        Assert.assertTrue(abs(mMovieIn.getVoteAverage() -  movieOut.getVoteAverage() )< 0.1);
        Assert.assertEquals(mMovieIn.getReleaseDate(), movieOut.getReleaseDate());
        Assert.assertEquals(mMovieIn.getTitle(), movieOut.getTitle());
        Assert.assertTrue(abs(mMovieIn.getPopularity() -  movieOut.getPopularity() )< 0.1);
        Assert.assertEquals(mMovieIn.getVoteCount(), movieOut.getVoteCount());
        Assert.assertEquals(mMovieIn.getVideo(), movieOut.getVideo());
        Assert.assertEquals(mMovieIn.getId(), movieOut.getId());
        Assert.assertTrue(movieOut.getTrailers().isEmpty());

    }

    @Test
    public void serializeDeserializeMovieWithTrailerTest(){

        Trailer first = new Trailer();
        first.setId("first");
        first.setISO639("639");
        first.setISO3166("3166");
        first.setKey("key one");
        first.setName("first name");
        first.setSite("site one");
        first.setSize(1);
        first.setType("type one");

        Trailer two = new Trailer();
        two.setId("two");
        two.setISO639("639_2");
        two.setISO3166("3166_2");
        two.setKey("key two");
        two.setName("second name");
        two.setSite("site two");
        two.setSize(2);
        two.setType("type two");

        mMovieIn.addTrailer(first);
        mMovieIn.addTrailer(two);


        Bundle bundle = new Bundle();
        bundle.putParcelable(LABEL,mMovieIn);

        Movie movieOut = bundle.getParcelable(LABEL);

        Assert.assertNotNull(movieOut);

        Assert.assertTrue(movieOut.getTrailers().size()==2);


    }

    @Test
    public void IntentWithMovieExtraTest(){

        Trailer first = new Trailer();
        first.setId("first");
        first.setISO639("639");
        first.setISO3166("3166");
        first.setKey("key one");
        first.setName("first name");
        first.setSite("site one");
        first.setSize(1);
        first.setType("type one");

        Trailer two = new Trailer();
        two.setId("two");
        two.setISO639("639_2");
        two.setISO3166("3166_2");
        two.setKey("key two");
        two.setName("second name");
        two.setSite("site two");
        two.setSize(2);
        two.setType("type two");

        mMovieIn.addTrailer(first);
        mMovieIn.addTrailer(two);

        Intent intent = DetailMovieActivity.newIntent(InstrumentationRegistry.getTargetContext(), mMovieIn);
        Movie movie = DetailMovieActivity.getMovie(intent);

        Assert.assertNotNull(movie);
        Assert.assertTrue(movie.getTrailers().size()==2);

    }

}
