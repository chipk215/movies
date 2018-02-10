package com.keyeswest.movies.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Movie implements Parcelable {

    @SerializedName("original_title")
    private String mOriginalTitle;

    @SerializedName("poster_path")
    private String mPosterPath;

    @SerializedName("overview")
    private String mOverview;

    @SerializedName("vote_average")
    private float mVoteAverage;

    @SerializedName("release_date")
    private String mReleaseDate;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("popularity")
    private float mPopularity;

    @SerializedName("vote_count")
    private int mVoteCount;

    @SerializedName("video")
    private Boolean mVideo;

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in){

        mOriginalTitle = in.readString();
        mPosterPath = in.readString();
        mOverview = in.readString();
        mVoteAverage = in.readFloat();
        mReleaseDate= in.readString();
        mTitle = in.readString();
        mPopularity = in.readFloat();
        mVoteCount = in.readInt();
        mVideo = in.readByte() != 0;

    }

    public Movie(){
        mOriginalTitle = "";
        mPosterPath = "";
        mOverview = "";
        mVoteAverage = 0f;
        mReleaseDate="";
        mTitle = "";
        mPopularity = 0f;
        mVoteCount = 0;
        mVideo = false;

    }



    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.mOriginalTitle = originalTitle;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        this.mPosterPath = posterPath;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        this.mOverview = overview;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.mReleaseDate = releaseDate;
    }

    public float getVoteAverage(){
        return mVoteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        mVoteAverage = voteAverage;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public float getPopularity() {
        return mPopularity;
    }

    public void setPopularity(float popularity) {
        mPopularity = popularity;
    }

    public void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    public void setVideo(Boolean video) {
        mVideo = video;
    }

    @Override
    public String toString(){
        return mOriginalTitle + "\n\n";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mOriginalTitle);
        dest.writeString(mPosterPath);
        dest.writeString(mOverview);
        dest.writeFloat(mVoteAverage);
        dest.writeString(mReleaseDate);
        dest.writeString(mTitle);
        dest.writeFloat(mPopularity);
        dest.writeInt(mVoteCount);
        dest.writeByte((byte)(mVideo ? 1:0));

    }
}
