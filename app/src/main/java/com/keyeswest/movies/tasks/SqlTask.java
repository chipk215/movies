package com.keyeswest.movies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.keyeswest.movies.data.MovieContract;
import com.keyeswest.movies.data.MovieCursorWrapper;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.repos.MovieRepo;
import com.keyeswest.movies.repos.SqlResult;
import com.keyeswest.movies.utilities.DatabaseBitmapUtility;
import com.keyeswest.movies.utilities.MovieFetcher;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.keyeswest.movies.repos.MovieRepo.MOVIE_ID_KEY;
import static com.keyeswest.movies.repos.MovieRepo.MOVIE_KEY;
import static com.keyeswest.movies.repos.MovieRepo.OPERATION_KEY;
import static com.keyeswest.movies.repos.MovieRepo.PROJECTION_ARGS_KEY;
import static com.keyeswest.movies.repos.MovieRepo.SELECTION_ARGS_KEY;
import static com.keyeswest.movies.repos.MovieRepo.SELECTION_CLAUSE_KEY;

public class SqlTask extends AsyncTask<Bundle,Void,SqlResult> {
    private static final String TAG = "SqlTask";

    private Context mContext;
    private MovieRepo.QuerySetResult mQuerySetResult;
    private MovieRepo.InsertResult mInsertResult;
    private MovieRepo.QueryCountResult mQueryCountResult;
    private Exception caughtException = null;


    /**
     * Execute the requested SQL operation
     * @param bundles - arguments for the MovieContentProvider and enum indicating what SQL
     *                operation to execute.
     * @return the results of the SQL operation
     */
    @Override
    protected SqlResult doInBackground(Bundle... bundles) {
        Bundle args;
        // must have a bundle
        SqlResult result = new SqlResult();
        if ((bundles != null) && (bundles.length == 1) ){
            args = bundles[0];
        }else{
            return null;
        }

        try {

            // retrieve the bundled content provider arguments
            String selectionClause = args.getString(SELECTION_CLAUSE_KEY);
            String[] selectionArgs = args.getStringArray(SELECTION_ARGS_KEY);
            String[] projectionArgs = args.getStringArray(PROJECTION_ARGS_KEY);
            // the SL operation to perform
            MovieRepo.Operations operation = (MovieRepo.Operations) args.get(OPERATION_KEY);

            // save the requested operation to use when handling results
            result.setSqlOperation(operation);

            switch (operation) {
                case CREATE:
                    //insert a movie
                    Movie movie = args.getParcelable(MOVIE_KEY);
                    ContentValues values = getContentValues(movie);
                    Uri uri = mContext.getContentResolver().insert(MovieContract.MovieTable.CONTENT_URI, values);
                    result.setResultUri(uri);
                    return result;

                case QUERY_SET:

                    //query the database
                    Cursor cursor = mContext.getContentResolver().query(
                            MovieContract.MovieTable.CONTENT_URI,
                                /* Columns; leaving this null returns every column in the table */
                            projectionArgs,
                                /* Optional specification for columns in the projection clause above */
                            selectionClause,
                                /* Values for "where" clause */
                            selectionArgs,
                                /* Sort order to return in Cursor */
                            null);

                    //process the results
                    MovieCursorWrapper movieCursor = new MovieCursorWrapper(cursor);
                    List<Movie> movies = new ArrayList<>();
                    try {
                        movieCursor.moveToFirst();
                        while (!movieCursor.isAfterLast()) {
                            movies.add(movieCursor.getMovie());
                            movieCursor.moveToNext();
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    result.setMovies(movies);
                    return result;

                case UPDATE:

                    return null;

                case DELETE:
                    // delete a movie record
                    long movieId = args.getLong(MOVIE_ID_KEY);
                    Uri uriToDelete = MovieContract.MovieTable.CONTENT_URI.buildUpon()
                            .appendPath(Long.toString(movieId)).build();
                    int deleted = mContext.getContentResolver().delete(uriToDelete,
                            null, null);

                    result.setCount(deleted);

                    return result;

                case QUERY_COUNT:
                    // queries the collection for a count of records matching query
                    //query the database
                    Cursor queryCursor = mContext.getContentResolver().query(
                            MovieContract.MovieTable.CONTENT_URI,
                                /* Columns; leaving this null returns every column in the table */
                            null,
                                /* Optional specification for columns in the projection clause above */
                            selectionClause,
                                /* Values for "where" clause */
                            selectionArgs,
                                /* Sort order to return in Cursor */
                            null);

                    if (queryCursor != null){
                        result.setCount(queryCursor.getCount());
                        queryCursor.close();
                    }else{
                        result.setCount(0);
                    }

                    return result;
            }

            return null;
        }catch (Exception ex){
            caughtException = ex;
        }

        return null;
    }


    @Override
    protected void onPostExecute(SqlResult sqlResult) {

        if (caughtException == null) {
            // match the results to the corresponding callback method
            switch (sqlResult.getSqlOperation()){

                case CREATE:
                    mInsertResult.movieResult(sqlResult.getResultUri());
                    break;

                case QUERY_SET:
                    mQuerySetResult.movieResult(sqlResult.getMovies());
                    break;

                case QUERY_COUNT:
                case DELETE:
                    mQueryCountResult.movieResult(sqlResult.getCount());
                    break;

                default:
                    break;
            }

        }else{
            Log.e(TAG, "Exception thrown handling Sql operation" + caughtException);

            //TODO revisit and determine how to continue

        }
    }

    public SqlTask(Context context){
        mContext = context;
    }


    public void setQuerySetResult(MovieRepo.QuerySetResult querySetResult) {
        mQuerySetResult = querySetResult;
    }

    public void setInsertResult(MovieRepo.InsertResult insertResult) {
        mInsertResult = insertResult;
    }

    public void setQueryCountResult(MovieRepo.QueryCountResult queryCountResult) {
        mQueryCountResult = queryCountResult;
    }

    private ContentValues getContentValues(Movie movie) throws IOException{

        // Get the movie poster synchronously since we are off the UI thread
        String posterPath = MovieFetcher.getPosterPathURL(movie.getPosterPath());
        Bitmap posterImage = Picasso.with(mContext).load(posterPath).get();
        byte[] posterBytes = DatabaseBitmapUtility.getBytes(posterImage);

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieTable.COLUMN_MOVIE_ID, movie.getId());
        values.put(MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(MovieContract.MovieTable.COLUMN_USER_RATING, movie.getVoteAverage());
        values.put(MovieContract.MovieTable.COLUMN_TITLE, movie.getTitle());
        values.put(MovieContract.MovieTable.COLUMN_POSTER, posterBytes);
        values.put(MovieContract.MovieTable.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(MovieContract.MovieTable.COLUMN_SYNOPSIS, movie.getOverview());

        return values;

    }
}