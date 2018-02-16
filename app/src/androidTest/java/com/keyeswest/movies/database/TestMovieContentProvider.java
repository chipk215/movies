package com.keyeswest.movies.database;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


//ATTRIBUTION: Udacity T09.05-Solution-QueryAllTasks TestTaskContentProvider

@RunWith(AndroidJUnit4.class)
public class TestMovieContentProvider {

    /* Context used to access various parts of the system */
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    private final static String ORIGINAL_TITLE = "Original Title";
    private final static float USER_RATING = 1.5f;


    @Before
    public void setUp(){
        MovieBaseHelper dbHelper = new MovieBaseHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(MovieContract.MovieTable.TABLE_NAME, null, null);
        database.close();
    }

    // Leave the test with empty MovieTable
    @After
    public void cleanUp(){
        MovieBaseHelper dbHelper = new MovieBaseHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(MovieContract.MovieTable.TABLE_NAME, null, null);
        database.close();

    }

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


    //================================================================================
    // Test Insert via ContentResolver
    //================================================================================
    /**
     * Tests inserting a single row of data via a ContentResolver
     */
    @Test
    public void testInsert(){

        int testMovieId = 1;

        /* Create movie to insert */
        ContentValues testValues = createTestMovieRecord(testMovieId);


        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

         /* Register a content observer to be notified of changes to data at a given URI (movie) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                MovieContract.MovieTable.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                movieObserver);

        Uri uri = contentResolver.insert(MovieContract.MovieTable.CONTENT_URI, testValues);

        Uri expectedUri = ContentUris.withAppendedId(MovieContract.MovieTable.CONTENT_URI, testMovieId );

        String insertProviderFailed = "Unable to insert item through Provider";
        assertEquals(insertProviderFailed, uri, expectedUri);

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        movieObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(movieObserver);



    }


    //================================================================================
    // Test Query (for movie table)
    //================================================================================


    /**
     * Inserts data, then tests if a query for the movie directory returns that data as a Cursor
     */
    @Test
    public void testMovieQuery() {

        // Get access to a writable database
        MovieBaseHelper dbHelper = new MovieBaseHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // create a movie record
        int testMovieId = 1;
        /* Create movie to insert */
        ContentValues testValues = createTestMovieRecord(testMovieId);

        // insert the record directly into the database
        long movieRowId = database.insert(
                MovieContract.MovieTable.TABLE_NAME,
                null,
                testValues);

        String insertFailed = "Unable to insert directly into the database";
        Assert.assertTrue(insertFailed, movieRowId != -1);

        database.close();

        /* Perform the ContentProvider query */
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, movieCursor != null);

        queryFailed = "\"Query failed to return the correct Cursor\";";
        assertEquals(queryFailed,1, movieCursor.getCount());

        movieCursor.moveToFirst();

        // Check the id
        long actualId = movieCursor.getLong(movieCursor
                .getColumnIndex(MovieContract.MovieTable.COLUMN_MOVIE_ID));
        String failId = "Movie ID data is incorrect.";
        assertEquals(failId, (long)testMovieId, actualId);

        // Check the title
        String actualTitle =
                movieCursor.getString(movieCursor
                        .getColumnIndex(MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE));
        String failMessage = "Original Title data is incorrect.";
        assertEquals(failMessage, ORIGINAL_TITLE, actualTitle);


        //check the user rating
        float actualRating = movieCursor.getFloat(movieCursor.
                getColumnIndex(MovieContract.MovieTable.COLUMN_USER_RATING));
        String failRating = "User rating is incorrect.";
        assertEquals(failRating, USER_RATING, actualRating);
        movieCursor.close();


    }


    //================================================================================
    // Query for specific record in database
    //================================================================================
    @Test
    public void testQueryForExistingRecord(){

        // Get access to a writable database
        MovieBaseHelper dbHelper = new MovieBaseHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // create a movie record
        int testMovieId = 1;
        /* Create movie to insert */
        ContentValues testValues = createTestMovieRecord(testMovieId);

        // insert the record directly into the database
        long movieRowId = database.insert(
                MovieContract.MovieTable.TABLE_NAME,
                null,
                testValues);

        String insertFailed = "Unable to insert directly into the database";
        Assert.assertTrue(insertFailed, movieRowId != -1);

        database.close();

        // request only the id field
        String[] projection={
                MovieContract.MovieTable.COLUMN_MOVIE_ID
        };


        String selectionClause = MovieContract.MovieTable.COLUMN_MOVIE_ID + "=  ?";
        String[] selectionArgs = { Long.toString(testMovieId) };

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                projection,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        Assert.assertNotNull(movieCursor);
        String failMessage = "Returned incorrect cursor";
        Assert.assertEquals(failMessage, 1, movieCursor.getCount());
        movieCursor.moveToFirst();
        Assert.assertEquals(failMessage,testMovieId,  movieCursor.getLong(movieCursor
                .getColumnIndex(MovieContract.MovieTable.COLUMN_MOVIE_ID)));


        // verify that title was not returned from query (projection only requested ids)
        Assert.assertEquals(failMessage, -1, movieCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_TITLE));

    }


    //================================================================================
    // Query for specific record not in database
    //================================================================================
    @Test
    public void testQueryForNonExistingRecord(){
        String[] projection={
                MovieContract.MovieTable.COLUMN_MOVIE_ID
        };

        long testMovieId = 1;
        String selectionClause = MovieContract.MovieTable.COLUMN_MOVIE_ID + "=  ?";
        String[] selectionArgs = { Long.toString(testMovieId) };

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                projection,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        Assert.assertNotNull(movieCursor);
        String failMessage = "Returned incorrect cursor";
        Assert.assertEquals(failMessage, 0, movieCursor.getCount());
    }

    //================================================================================
    // Test Delete (for a single item)
    //================================================================================


    /**
     * Tests deleting a single row of data via a ContentResolver
     */
    @Test
    public void testDelete() {

        // Get access to a writable database
        MovieBaseHelper dbHelper = new MovieBaseHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // create a movie record
        int testMovieId = 1;
        /* Create movie to insert */
        ContentValues testValues = createTestMovieRecord(testMovieId);

        // insert the record directly into the database
        long movieRowId = database.insert(
                MovieContract.MovieTable.TABLE_NAME,
                null,
                testValues);

        String insertFailed = "Unable to insert directly into the database";
        Assert.assertTrue(insertFailed, movieRowId != -1);

        database.close();


        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

         /* Register a content observer to be notified of changes to data at a given URI (movie) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                MovieContract.MovieTable.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                movieObserver);


        Uri uriToDelete = MovieContract.MovieTable.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(testMovieId)).build();

        int moviesDeleted = contentResolver.delete(uriToDelete, null, null);
        String deleteFailed = "Unable to delete item in the database";
        assertTrue(deleteFailed, moviesDeleted != 0);
        assertEquals(deleteFailed, 1, moviesDeleted);


    /*
         * If this fails, it's likely you didn't call notifyChange in your delete method from
         * your ContentProvider.
         */
        movieObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(movieObserver);


    }




    private ContentValues createTestMovieRecord(int testMovieId){

        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieTable.COLUMN_MOVIE_ID, testMovieId);
        testValues.put(MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE, ORIGINAL_TITLE);
        testValues.put(MovieContract.MovieTable.COLUMN_USER_RATING, USER_RATING);

        return testValues;
    }
}
