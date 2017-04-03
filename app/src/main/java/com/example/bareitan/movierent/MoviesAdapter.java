package com.example.bareitan.movierent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by bareitan on 31/03/2017.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>{

    private List<MovieItem> movieItemList;
    private Context mContext;
    final private ListItemClickListener mOnClickListener;

    public MoviesAdapter(Context context, List<MovieItem> movieItemList, ListItemClickListener listener){
        this.movieItemList = movieItemList;
        this.mContext = context;
        mOnClickListener = listener;
    }
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_movie, null);
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        MovieItem movieItem = movieItemList.get(position);

        if(!TextUtils.isEmpty(movieItem.getThumbnail())) {
            Picasso.with(mContext).load(movieItem.getThumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imageView);
        }
        holder.textView.setText(movieItem.getName());
    }

    @Override
    public int getItemCount() {
        return (null != movieItemList ?  movieItemList.size() : 0);
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{
        protected ImageView imageView;
        protected TextView textView;

        public MovieViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.textView = (TextView) view.findViewById(R.id.title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
