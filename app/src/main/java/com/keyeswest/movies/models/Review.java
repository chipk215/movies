package com.keyeswest.movies.models;


import com.google.gson.annotations.SerializedName;

public class Review {

    @SerializedName("id")
    private String mId;

    @SerializedName("author")
    private String mAuthor;

    @SerializedName("content")
    private String mContent;

    @SerializedName("url")
    private String mUrl;

    public Review(){
        mId= "";
        mAuthor = "";
        mContent = "";
        mUrl = "";
    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getUrl() {
        return mUrl;
    }

}
