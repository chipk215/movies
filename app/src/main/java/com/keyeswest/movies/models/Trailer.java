package com.keyeswest.movies.models;


import com.google.gson.annotations.SerializedName;

public class Trailer {

    @SerializedName("id")
    private String mId;

    @SerializedName("iso_639_1")
    private String mISO639;

    @SerializedName("iso_3166_1")
    private String mISO3166;

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
}
