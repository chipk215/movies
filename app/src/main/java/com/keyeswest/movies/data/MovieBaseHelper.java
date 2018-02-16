package com.keyeswest.movies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieBaseHelper extends SQLiteOpenHelper {

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    private static final String CREATE_TABLE = "CREATE TABLE ";

    private static final String DATABASE_NAME = "MovieBase.db";

    private static final int DATABASE_VERSION = 1;

    public MovieBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Allow null values for all fields except primary key and user rating since theMovieDB
        // API specs these fields as optional (require the application to force a value for rating)
        final String SQL_CREATE_MOVIE_TABLE =
                CREATE_TABLE + MovieContract.MovieTable.TABLE_NAME +
                        " (" +
                        MovieContract.MovieTable.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY," +
                        MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE + " TEXT," +
                        MovieContract.MovieTable.COLUMN_TITLE + " TEXT," +
                        MovieContract.MovieTable.COLUMN_POSTER + " BLOB," +
                        MovieContract.MovieTable.COLUMN_SYNOPSIS + " TEXT," +
                        MovieContract.MovieTable.COLUMN_USER_RATING + " REAL NOT NULL," +
                        // Save date as TEXT for now revisit if querying by TEXT date is not supported
                        MovieContract.MovieTable.COLUMN_RELEASE_DATE + " TEXT" +
                        "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        sqLiteDatabase.execSQL(DROP_TABLE + MovieContract.MovieTable.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
