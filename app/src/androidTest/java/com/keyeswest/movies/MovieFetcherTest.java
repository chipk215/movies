package com.keyeswest.movies;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import com.keyeswest.movies.interfaces.TrailerFetcherCallback;
import com.keyeswest.movies.models.Trailer;
import com.keyeswest.movies.utilities.MovieFetcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


//TODO revisit and figure out how to implement this without using InstrumentationTestCase
@RunWith(AndroidJUnit4.class)
public class MovieFetcherTest extends InstrumentationTestCase {

    private CountDownLatch signal;
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

    }

    /**
     * Depends on URI which is in the android lib
     * @throws MalformedURLException
     */
    @Test
    public void trailerURLTest() throws MalformedURLException {
        URL url = MovieFetcher.buildTrailerURL(198663);

        URL expected = new URL("https://api.themoviedb.org/3/movie/198663/videos?api_key="
                + BuildConfig.API_KEY);

        assertEquals(expected, url);
    }


    @Test
    public void getTrailersTest() throws Throwable{

        // create  a signal to let us know when our task is done.
        signal = new CountDownLatch(1);

        InstrumentationRegistry.getTargetContext().startActivity(new Intent(Intent.ACTION_MAIN));

        // Execute the async task on the UI thread! THIS IS KEY!
        runTestOnUiThread(new Runnable() {

            @Override
            public void run() {

                MovieFetcher fetcher = new MovieFetcher(InstrumentationRegistry.getTargetContext());
                fetcher.fetchMovieTrailers(198663,new TrailerFetcherCallback(){

                    @Override
                    public void updateList(List<Trailer> movieItemList) {
                        int expectedCount = 2;
                        Assert.assertNotNull(movieItemList);
                        Assert.assertEquals(expectedCount,movieItemList.size());
                        signal.countDown();
                    }

                    @Override
                    public void downloadErrorOccurred(ErrorCondition errorMessage) {
                        Assert.fail();
                    }
                });
            }
        });


        signal.await(30, TimeUnit.SECONDS);
        Assert.assertTrue(true);
    }
}
