package com.keyeswest.movies.repos;


import android.annotation.SuppressLint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.keyeswest.movies.data.MovieContract;
import com.keyeswest.movies.data.MovieCursorWrapper;
import com.keyeswest.movies.models.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieRepo  {


    private static final int MOVIE_DATABASE_LOADER = 200;

    private static final String SELECTION_CLAUSE_KEY = "SELECTION_CLAUSE_KEY";
    private static final String SELECTION_ARGS_KEY = "SELECTION_ARGS_KEY";
    private static final String OPERATION_KEY = "OPERATION_KEY";
    private static final String MOVIE_KEY = "MOVIE_KEY";

    public enum Operations{ CREATE, READ, UPDATE, DELETE}


    private Context mContext;


    private MovieResult mGetMovieResult;
    private AddMovieResult mAddResult;


    public MovieRepo(Context context ){
        mContext = context;

    }



    public interface MovieResult{
        void movieResult(List<Movie> movies);
    }

    public interface AddMovieResult{
        void movieResult(Uri movieUri);
    }

    //================================================================================
    // Create
    //================================================================================
    public void addMovie(Movie movie,  AddMovieResult callback){

        // first check that movie is not in database

        mAddResult = callback;

        Bundle queryBundle = new Bundle();
        queryBundle.putSerializable(OPERATION_KEY, Operations.CREATE);
        queryBundle.putParcelable(MOVIE_KEY, movie);

        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setAddMovieResult(callback);
        sqlTask.execute(queryBundle);


    }


    private class SqlTask extends AsyncTask<Bundle,Void,SqlResult>{

        private Context mContext;
        private MovieResult mMovieResult;
        private AddMovieResult mAddMovieResult;


        @Override
        protected SqlResult doInBackground(Bundle... bundles) {
            Bundle args;
            if ((bundles != null) && (bundles.length == 1) ){
                args = bundles[0];
            }else{
                return null;
            }
            String selectionClause = args.getString(SELECTION_CLAUSE_KEY);
            String[] selectionArgs = args.getStringArray(SELECTION_ARGS_KEY);
            Operations operation = (Operations) args.get(OPERATION_KEY);

            switch (operation){
                case CREATE:
                    Movie movie = args.getParcelable(MOVIE_KEY);

                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieTable.COLUMN_MOVIE_ID, movie.getId());
                    values.put(MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                    values.put(MovieContract.MovieTable.COLUMN_USER_RATING, movie.getVoteAverage());
                    values.put(MovieContract.MovieTable.COLUMN_TITLE, movie.getTitle());

                    Uri uri = mContext.getContentResolver().insert(MovieContract.MovieTable.CONTENT_URI,values);
                    return new SqlResult(uri);

                case READ:
                    Cursor cursor = mContext.getContentResolver().query(
                            MovieContract.MovieTable.CONTENT_URI,
                                /* Columns; leaving this null returns every column in the table */
                            null,
                                /* Optional specification for columns in the projection clause above */
                            selectionClause,
                                /* Values for "where" clause */
                            selectionArgs,
                                /* Sort order to return in Cursor */
                            null);

                    //process the results
                    MovieCursorWrapper movieCursor = new MovieCursorWrapper(cursor);
                    List<Movie> movies = new ArrayList<>();
                    try{
                        movieCursor.moveToFirst();
                        while (!movieCursor.isAfterLast()){
                            movies.add(movieCursor.getMovie());
                            movieCursor.moveToNext();
                        }
                    } finally{
                        if (cursor != null){
                            cursor.close();
                        }
                    }

                    return new SqlResult(movies);


                case UPDATE:

                   return null;

                case DELETE:

                    return null;
            }

            return null;
     }


        @Override
        protected void onPostExecute(SqlResult sqlResult) {
            if (sqlResult.getMovies() != null) {
                mMovieResult.movieResult(sqlResult.getMovies());
            } else if (sqlResult.getResultUri() != null){
                mAddMovieResult.movieResult(sqlResult.getResultUri());
            }
        }

        public SqlTask(Context context){
            mContext = context;
        }

        public void setContext(Context context) {
            mContext = context;
        }

        public void setMovieResult(MovieResult movieResult) {
            mMovieResult = movieResult;
        }

        public void setAddMovieResult(AddMovieResult addMovieResult) {
            mAddMovieResult = addMovieResult;
        }
    }

    //================================================================================
    // Read
    //================================================================================
    public void getMovieById(long movieId, MovieResult callback){

        // Think about this some more wrt to overlapped calls - we are losing the connection
        // between the request and the callback - maybe a loader is ot the best component to use
        // we could store the callback in a hash table indexed by a mangled method name + movieId
        mGetMovieResult = callback;

        String selectionClause = MovieContract.MovieTable.COLUMN_MOVIE_ID + "=  ?";
        String[] selectionArgs = { Long.toString(movieId) };

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SELECTION_CLAUSE_KEY, selectionClause);
        queryBundle.putStringArray(SELECTION_ARGS_KEY, selectionArgs);
        queryBundle.putSerializable(OPERATION_KEY, Operations.READ);

        SqlTask sqlTask = new SqlTask(mContext);
        sqlTask.setMovieResult(callback);
        sqlTask.execute(queryBundle);


     //   Loader<List<Movie>> movieLoader = mLoaderManager.getLoader(MOVIE_DATABASE_LOADER);

     //   if (movieLoader == null){
      //      mLoaderManager.initLoader(MOVIE_DATABASE_LOADER, queryBundle, this);
      //  } else{
      //      mLoaderManager.restartLoader(MOVIE_DATABASE_LOADER,queryBundle, this);
      //  }


    }


    //================================================================================
    // Delete
    //================================================================================

}
