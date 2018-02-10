package com.keyeswest.movies;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import com.keyeswest.movies.interfaces.TrailerFetcherCallback;
import com.keyeswest.movies.models.Trailer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest extends InstrumentationTestCase  {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.keyeswest.movies", appContext.getPackageName());
    }

    @Test
    public void trailerURLTest() throws MalformedURLException{
        URL url = MovieFetcher.buildTrailerURL(198663);

        URL expected = new URL("https://api.themoviedb.org/3/movie/198663/videos?api_key=" + BuildConfig.API_KEY);

        assertEquals(expected, url);
    }

    final CountDownLatch signal = new CountDownLatch(1);


    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

    }

    @Test
    public void getTrailersTest() throws Throwable{

        InstrumentationRegistry.getTargetContext().startActivity(new Intent(Intent.ACTION_MAIN));
        // create  a signal to let us know when our task is done.
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        // Execute the async task on the UI thread! THIS IS KEY!
        runTestOnUiThread(new Runnable() {

            @Override
            public void run() {

                MovieFetcher fetcher = new MovieFetcher(InstrumentationRegistry.getTargetContext());
                fetcher.fetchMovieTrailers(198663,new TrailerFetcherCallback(){

                    @Override
                    public void updateTrailerList(List<Trailer> movieItemList) {
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
