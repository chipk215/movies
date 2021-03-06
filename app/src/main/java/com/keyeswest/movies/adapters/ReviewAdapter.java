package com.keyeswest.movies.adapters;


import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.keyeswest.movies.R;
import com.keyeswest.movies.models.Review;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler list view holding movie reviews retrieved from TheMovieDB web site
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    public interface OnFullReviewClickListener{
        void onItemClick(String reviewURL);
    }

    private final List<Review> mReviewList;
    private final OnFullReviewClickListener mReviewCallback;


    public ReviewAdapter(List<Review> reviewList, OnFullReviewClickListener fullReviewCallback){
        mReviewList = reviewList;
        mReviewCallback = fullReviewCallback;

    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ReviewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        holder.bind(mReviewList.get(position));
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    class ReviewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.review_content_tv) TextView mContentTextView;
        @BindView(R.id.more_details_btn) Button mMoreDetailsButton;

        ReviewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_review, parent, false));

            ButterKnife.bind(this, itemView);
            mMoreDetailsButton
                    .setPaintFlags(mMoreDetailsButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        }

        void bind(final Review reviewItem){
            mContentTextView.setText(reviewItem.getContent());

            mMoreDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReviewCallback.onItemClick(reviewItem.getUrl());
                }
            });
        }

    }
}
