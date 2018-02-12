package com.keyeswest.movies.models;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Trailer implements Parcelable {

    private static final String YOU_TUBE_WATCH_URL= "https://www.youtube.com/watch";
    private static final String VIDEO_PARAMETER = "v";

    @SerializedName("id")
    private String mId;

    @SerializedName("iso_639_1")
    private String mISO639;

    @SerializedName("iso_3166_1")
    private String mISO3166;

    // This is a YouTube video key
    @SerializedName("key")
    private String mKey;

    @SerializedName("name")
    private String mName;

    @SerializedName("site")
    private String mSite;

    @SerializedName("size")
    private int mSize;

    @SerializedName("type")
    private String mType;



    public static final Parcelable.Creator<Trailer> CREATOR
            = new Parcelable.Creator<Trailer>() {

        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    public Trailer(){
        mId="";
        mISO639="";
        mISO3166="";
        mKey="";
        mName="";
        mSite="";
        mSize=0;
        mType="";
    }

    private Trailer(Parcel in){
        mId = in.readString();
        mISO639 = in.readString();
        mISO3166 = in.readString();
        mKey = in.readString();
        mName = in.readString();
        mSite = in.readString();
        mSize = in.readInt();
        mType = in.readString();
    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getISO639() {
        return mISO639;
    }

    public void setISO639(String ISO639) {
        mISO639 = ISO639;
    }

    public String getISO3166() {
        return mISO3166;
    }

    public void setISO3166(String ISO3166) {
        mISO3166 = ISO3166;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getSite() {
        return mSite;
    }

    public void setSite(String site) {
        mSite = site;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mISO639);
        dest.writeString(mISO3166);
        dest.writeString(mKey);
        dest.writeString(mName);
        dest.writeString(mSite );
        dest.writeInt(mSize);
        dest.writeString(mType);
    }

    /**
     * The video key returned by theMovieDP api is a YouTube key. To play the video a corresponding
     * YouTube URI must be generated.
     * @return the YouTube URI associated with the video
     */
    public Uri getVideoUri(){
        Uri uri = Uri.parse(YOU_TUBE_WATCH_URL).buildUpon()
                .appendQueryParameter(VIDEO_PARAMETER, getKey())
                .build();

        return uri;

    }
}
