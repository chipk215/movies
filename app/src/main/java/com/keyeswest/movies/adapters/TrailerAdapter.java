package com.keyeswest.movies.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;

import com.keyeswest.movies.R;
import com.keyeswest.movies.models.Trailer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrailerAdapter  extends RecyclerView.Adapter<TrailerAdapter.TrailerHolder>  {

    public interface OnItemClickListener{
        void onItemClick(Trailer trailer);
    }

    private final List<Trailer> mTrailerItems;
    private final OnItemClickListener mListener;


    public TrailerAdapter(List<Trailer> trailerItems, OnItemClickListener listener){
        mTrailerItems = trailerItems;
        mListener = listener;

    }

    @Override
    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new TrailerHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(TrailerHolder holder, int position) {
        holder.bind(mTrailerItems.get(position), mListener);

    }

    @Override
    public int getItemCount() {
        return mTrailerItems.size();
    }


    class TrailerHolder  extends RecyclerView.ViewHolder{

        @BindView(R.id.title_tv) TextView mTitleTextView;
        @BindView(R.id.type_tv) TextView mTypeTextView;
        @BindView(R.id.trailer_btn) ImageButton mPlayButton;

        TrailerHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_trailer, parent, false));
            ButterKnife.bind(this, itemView);

        }

        void bind(final Trailer trailerItem, final OnItemClickListener listener){
            mTitleTextView.setText(trailerItem.getName());
            mTypeTextView.setText(trailerItem.getType());

            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(trailerItem);
                }
            });

        }
    }
}
