package com.keyeswest.movies.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.keyeswest.movies.utilities.MovieFetcher;
import com.keyeswest.movies.R;

import com.keyeswest.movies.models.Movie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for recycler view list holding movies retrieved from TheMovieDB web site
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

    public interface OnItemClickListener{
        void onItemClick(Movie movie);
    }
    private final List<Movie> mMovieItems;
    private final OnItemClickListener mListener;


    public MovieAdapter(List<Movie> movieItems, OnItemClickListener listener){
        mMovieItems = movieItems;
        mListener = listener;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType){

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_movie_image,
                parent, false);

        return new MovieHolder(view);
    }


    @Override
    public void onBindViewHolder(MovieHolder movieHolder, int position){
        Movie movieItem = mMovieItems.get(position);
        movieHolder.bindMovieItem(movieItem, mListener);
    }

    @Override
    public int getItemCount(){
        return mMovieItems.size();
    }


    static class MovieHolder  extends RecyclerView.ViewHolder implements Target {

        private static final String TAG="MovieHolder";

        @BindView(R.id.item_image_view) ImageView mItemImageView;

        public MovieHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);

        }

        public void bindMovieItem(final Movie movieItem, final OnItemClickListener listener){
            String posterPath = movieItem.getPosterPath();
            String imagePath = MovieFetcher.getPosterPathURL(posterPath);
            Log.i(TAG, imagePath);
            if (movieItem.getPosterImage() == null) {
                Picasso.with(itemView.getContext()).load(imagePath).into(this);

            }else{
                // movie from database contains the bitmap image of the poster
                this.mItemImageView.setImageBitmap(movieItem.getPosterImage());
            }

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onItemClick(movieItem);
                }
            });
        }


        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mItemImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    }
}
