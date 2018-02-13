package com.keyeswest.movies.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.keyeswest.movies.R;
import com.keyeswest.movies.models.Review;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    private List<Review> mReviewList;

    public ReviewAdapter(List<Review> reviewList){
        mReviewList = reviewList;
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
        }

        void bind(final Review reviewItem){
            mContentTextView.setText(reviewItem.getContent());
        }

    }
}
