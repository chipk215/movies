package com.keyeswest.movies.repos;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.keyeswest.movies.data.MovieContract;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.tasks.SqlTask;

import java.util.List;

// This could be converted to a generic base class if more database entities were to be defined

public class MovieRepo  {
    @SuppressWarnings("unused")
    private static final String TAG= "MovieRepo";

    public static final String SELECTION_CLAUSE_KEY = "SELECTION_CLAUSE_KEY";
    public static final String SELECTION_ARGS_KEY = "SELECTION_ARGS_KEY";
    public static final String PROJECTION_ARGS_KEY = "PROJECTION_ARGS_KEY";
    public static final String OPERATION_KEY = "OPERATION_KEY";
    public static final String MOVIE_KEY = "MOVIE_KEY";
    public static final String MOVIE_ID_KEY = "MOVIE_ID_KEY";

    public enum Operations{ CREATE, QUERY_SET, QUERY_COUNT, UPDATE, DELETE}


    private final Context mContext;

    public MovieRepo(Context context ){
        mContext = context;

    }


    //================================================================================
    // All of the database operations are performed asynchronously. These interfaces
    // define the callbacks used to get the results back to the caller.
    public interface QuerySetResult {
        // returns a list of movies (1 or more)
        void movieResult(List<Movie> movies);
    }

    public interface InsertResult {
        // returns a uri for created movie records
        void movieResult(Uri movieUri);
    }

    public interface QueryCountResult {
        // returns an integer count for queries that return a count
        void movieResult(int recordCount);
    }
    //===============================================================================



    //================================================================================
    // Create Operations
    //================================================================================
    public void addMovie(Movie movie,  InsertResult callback){

        //Consider first checking that movie is not in database prior to insertion


        Bundle queryBundle = new Bundle();
        queryBundle.putSerializable(OPERATION_KEY, Operations.CREATE);
        queryBundle.putParcelable(MOVIE_KEY, movie);

        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setInsertResult(callback);
        sqlTask.execute(queryBundle);

    }


    //================================================================================
    // Read/Query Operations
    //================================================================================
    public void getMovieById(long movieId, QuerySetResult callback){


        String selectionClause = MovieContract.MovieTable.COLUMN_MOVIE_ID + "=  ?";
        String[] selectionArgs = { Long.toString(movieId) };

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SELECTION_CLAUSE_KEY, selectionClause);
        queryBundle.putStringArray(SELECTION_ARGS_KEY, selectionArgs);
        queryBundle.putSerializable(OPERATION_KEY, Operations.QUERY_SET);

        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setQuerySetResult(callback);
        sqlTask.execute(queryBundle);

    }

    public void getMovieCount( QueryCountResult callback){

        // request only the id field
        String[] projection={
                MovieContract.MovieTable.COLUMN_MOVIE_ID
        };

        Bundle queryBundle = new Bundle();
        queryBundle.putSerializable(OPERATION_KEY, Operations.QUERY_COUNT);
        queryBundle.putStringArray(PROJECTION_ARGS_KEY, projection);
        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setQueryCountResult(callback);
        sqlTask.execute(queryBundle);

    }

    public void getAllMovies(QuerySetResult callback){
        Bundle queryBundle = new Bundle();
        queryBundle.putSerializable(OPERATION_KEY, Operations.QUERY_SET);
        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setQuerySetResult(callback);
        sqlTask.execute(queryBundle);
    }


    //================================================================================
    // Delete Operation
    //================================================================================
    public void deleteMovieById(long movieId, QueryCountResult callback){
        Bundle queryBundle = new Bundle();
        queryBundle.putLong(MOVIE_ID_KEY, movieId);
        queryBundle.putSerializable(OPERATION_KEY, Operations.DELETE);
        SqlTask sqlTask = new SqlTask(mContext);
        // delete returns an integer count
        sqlTask.setQueryCountResult(callback);
        sqlTask.execute(queryBundle);
    }


}
