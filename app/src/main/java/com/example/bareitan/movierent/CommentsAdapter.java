package com.example.bareitan.movierent;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import static android.view.View.GONE;


/**
 * Created by bareitan on 31/03/2017.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentsItemList;
    private Context mContext;
    private AdapterCallback mAdapterCallback;

    public CommentsAdapter(Context context, List<Comment> commentsItemList, AdapterCallback callback) {
        this.commentsItemList = commentsItemList;
        this.mContext = context;
        this.mAdapterCallback = callback;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, null);
        CommentViewHolder viewHolder = new CommentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        final Comment comment = commentsItemList.get(position);

        holder.text.setText("\"" + comment.getText() + "\"");
        holder.ratingBar.setRating(comment.getRating());
        holder.user.setText(comment.getUser());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapterCallback.onMethodCallback(comment.getCommentId());
            }
        });

        if (!isUserCommentOrAdmin(comment)) {
            holder.deleteButton.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != commentsItemList ? commentsItemList.size() : 0);
    }

    public Boolean isUserCommentOrAdmin(Comment comment) {
        Boolean result = false;
        String PREFS_LOGIN = "LoginPrefsFile";
        String PREF_USER_ID = "userid";

        SharedPreferences pref_user = mContext.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        int userId = pref_user.getInt(PREF_USER_ID, -1);

        String PREFS_ADMIN = "AdminPrefsFile";
        String PREF_IS_ADMIN = "isAdmin";

        SharedPreferences pref_admin = mContext.getSharedPreferences(PREFS_ADMIN, Context.MODE_PRIVATE);
        boolean isAdmin = pref_admin.getBoolean(PREF_IS_ADMIN, false);

        if (comment.getUserId() == userId || isAdmin)
            result = true;

        return result;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        protected TextView text;
        protected AppCompatRatingBar ratingBar;
        protected TextView user;
        protected Button deleteButton;

        public CommentViewHolder(View view) {
            super(view);
            this.text = (TextView) view.findViewById(R.id.comment_text);
            this.ratingBar = (AppCompatRatingBar) view.findViewById(R.id.rating_bar);
            this.user = (TextView) view.findViewById(R.id.user);
            this.deleteButton = (Button) view.findViewById(R.id.delete_button);
        }

    }
}

