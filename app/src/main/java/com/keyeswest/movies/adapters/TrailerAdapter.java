package com.keyeswest.movies.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.keyeswest.movies.R;
import com.keyeswest.movies.models.Trailer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrailerAdapter  extends RecyclerView.Adapter<TrailerAdapter.TrailerHolder>  {

    private final List<Trailer> mTrailerItems;
    private final Activity mActivity;

    public TrailerAdapter(List<Trailer> trailerItems, Activity activity){
        mTrailerItems = trailerItems;
        mActivity = activity;
    }

    @Override
    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);

        return new TrailerHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(TrailerHolder holder, int position) {
        holder.bind(mTrailerItems.get(position));

    }

    @Override
    public int getItemCount() {
        return mTrailerItems.size();
    }




    static class TrailerHolder  extends RecyclerView.ViewHolder{

        @BindView(R.id.title_tv) TextView mTitleTextView;
        @BindView(R.id.type_tv) TextView mTypeTextView;
        @BindView(R.id.trailer_btn) ImageButton mPlayButton;

        public TrailerHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_trailer, parent, false));
            ButterKnife.bind(this, itemView);

        }

        public void bind(final Trailer trailerItem){
            mTitleTextView.setText(trailerItem.getName());
            mTypeTextView.setText(trailerItem.getType());
            mPlayButton = new ImageButton(itemView.getContext());
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"Click", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
