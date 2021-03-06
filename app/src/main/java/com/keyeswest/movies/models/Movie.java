package com.keyeswest.movies.models;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Movie implements Parcelable {


    @SerializedName("id")
    private long mId;

    @SerializedName("original_title")
    private String mOriginalTitle;

    @SerializedName("poster_path")
    private String mPosterPath;

    @SerializedName("overview")
    private String mOverview;

    // This is also referred to in the requirements as user rating
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

    private Bitmap mPosterImage;

    private List<Trailer> mTrailers;

    @SuppressWarnings("unused")
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
        mId = in.readLong();
        mTrailers = new ArrayList<>();
        in.readTypedList(mTrailers, Trailer.CREATOR);

    }

    public Movie(){
        this(0L);
        mOriginalTitle = "";
        mPosterPath = "";
        mOverview = "";
        mVoteAverage = 0f;
        mReleaseDate="";
        mTitle = "";
        mPopularity = 0f;
        mVoteCount = 0;
        mVideo = false;
        mTrailers = new ArrayList<>();

    }

    public Movie(long movieId){
        mId = movieId;
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

    public int getVoteCount(){ return mVoteCount;}

    public void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    public void setVideo(Boolean video) {
        mVideo = video;
    }

    public boolean getVideo(){return mVideo;}

    public long getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
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
        dest.writeLong(mId);

    }

    public List<Trailer> getTrailers() {
        return mTrailers;
    }

    public void addTrailer(Trailer trailer){
        mTrailers.add(trailer);
    }

    public Bitmap getPosterImage() {
        return mPosterImage;
    }

    public void setPosterImage(Bitmap posterImage) {
        mPosterImage = posterImage;
    }


}
