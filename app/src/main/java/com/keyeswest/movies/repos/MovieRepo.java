package com.keyeswest.movies.repos;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.keyeswest.movies.data.MovieContract;
import com.keyeswest.movies.models.Movie;
import com.keyeswest.movies.tasks.SqlTask;

import java.util.List;


public class MovieRepo  {
    private static final String TAG= "MovieRepo";

    public static final String SELECTION_CLAUSE_KEY = "SELECTION_CLAUSE_KEY";
    public static final String SELECTION_ARGS_KEY = "SELECTION_ARGS_KEY";
    public static final String OPERATION_KEY = "OPERATION_KEY";
    public static final String MOVIE_KEY = "MOVIE_KEY";
    public static final String MOVIE_ID_KEY = "MOVIE_ID_KEY";

    public enum Operations{ CREATE, READ, UPDATE, DELETE}


    private Context mContext;



    public MovieRepo(Context context ){
        mContext = context;

    }


    public interface QueryResult {
        void movieResult(List<Movie> movies);
    }

    public interface InsertResult {
        void movieResult(Uri movieUri);
    }

    public interface DeleteResult{
        void movieResult(int recordsDeleted);
    }

    //================================================================================
    // Create
    //================================================================================
    public void addMovie(Movie movie,  InsertResult callback){

        //TODO first check that movie is not in database



        Bundle queryBundle = new Bundle();
        queryBundle.putSerializable(OPERATION_KEY, Operations.CREATE);
        queryBundle.putParcelable(MOVIE_KEY, movie);

        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setInsertResult(callback);
        sqlTask.execute(queryBundle);

    }


    //================================================================================
    // Read
    //================================================================================
    public void getMovieById(long movieId, QueryResult callback){


        String selectionClause = MovieContract.MovieTable.COLUMN_MOVIE_ID + "=  ?";
        String[] selectionArgs = { Long.toString(movieId) };

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SELECTION_CLAUSE_KEY, selectionClause);
        queryBundle.putStringArray(SELECTION_ARGS_KEY, selectionArgs);
        queryBundle.putSerializable(OPERATION_KEY, Operations.READ);

        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setQueryResult(callback);
        sqlTask.execute(queryBundle);

    }


    //================================================================================
    // Delete
    //================================================================================
    public void deleteMovieById(long movieId, DeleteResult callback){
        Bundle queryBundle = new Bundle();
        queryBundle.putLong(MOVIE_ID_KEY, movieId);
        queryBundle.putSerializable(OPERATION_KEY, Operations.DELETE);
        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setDeleteResult(callback);
        sqlTask.execute(queryBundle);
    }


}
