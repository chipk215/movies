package com.keyeswest.movies.database;

import android.content.ComponentName;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


//ATTRIBUTION: Udacity T09.05-Solution-QueryAllTasks TestTaskContentProvider

@RunWith(AndroidJUnit4.class)
public class TestMovieContentProvider {

    /* Context used to access various parts of the system */
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    //================================================================================
    // Test ContentProvider Registration
    //================================================================================

    @Test
    public void testProviderIsRegistered(){

        String packageName = mContext.getPackageName();
        String movieProviderClassName = MovieContentProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, movieProviderClassName);

        try{

            PackageManager pm = mContext.getPackageManager();
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            String incorrectAuthorityMessage =
                    "Error: TaskContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthorityMessage,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e){

            String providerNotRegisteredAtAll = "Error: TaskContentProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);

        }
    }


    //================================================================================
    // Test UriMatcher
    //================================================================================
    private static final Uri TEST_MOVIE = MovieContract.MovieTable.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_ID = TEST_MOVIE.buildUpon().appendPath("1").build();

    @Test
    public void testUriMatcher(){
        UriMatcher testMatcher = MovieContentProvider.buildUriMatcher();

        /* Test that the code returned from our matcher matches the expected MOVIE int */
        String failMessage = "Error: The MOVIE URI was matched incorrectly.";
        int actualMovieMatchCode = testMatcher.match(TEST_MOVIE);
        int expectedMovieMatchCode = MovieContentProvider.MOVIE_DIRECTORY;
        assertEquals(failMessage,actualMovieMatchCode, expectedMovieMatchCode);

        /* Test that code returned for single movie matches MOVIE_WITH_ID */
        failMessage = "Error: The MOVIE_WITH_ID URI was matched incorrectly.";
        int actual = testMatcher.match(TEST_MOVIE_WITH_ID);
        int expected = MovieContentProvider.MOVIE_WITH_ID;
        assertEquals(failMessage, actual, expected);


    }



}
