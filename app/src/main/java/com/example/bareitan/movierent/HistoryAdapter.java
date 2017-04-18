package com.example.bareitan.movierent;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.R.color.holo_orange_light;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<RentItem> mRentItemList;
    private Context mContext;

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        protected TextView mMovieName;
        protected TextView mRentDate;
        protected TextView mReturnDate;
        protected CardView mCardView;
        public HistoryViewHolder(View v) {
            super(v);
            mMovieName = (TextView) v.findViewById(R.id.movie_name);
            mRentDate = (TextView) v.findViewById(R.id.rent_date);
            mReturnDate = (TextView) v.findViewById(R.id.return_date);
            mCardView = (CardView) v.findViewById(R.id.card);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(Context context, List<RentItem> rentData) {
        mContext = context;
        mRentItemList = rentData;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_history, null);
        HistoryViewHolder viewHolder = new HistoryViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        RentItem rentItem = mRentItemList.get(position);
        holder.mMovieName.setText(rentItem.getMovieName());
        holder.mRentDate.setText(rentItem.getRentDate().toString());
        holder.mReturnDate.setText(rentItem.getReturnDate().toString());
        if(rentItem.getReturnDate()==""){
            holder.mCardView.setCardBackgroundColor(Color.parseColor("#ff99cc00"));

        }else{
            holder.mCardView.setCardBackgroundColor(Color.YELLOW);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (null != mRentItemList ?  mRentItemList.size() : 0);
    }
}