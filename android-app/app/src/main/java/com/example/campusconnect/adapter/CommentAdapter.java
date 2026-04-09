package com.example.campusconnect.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusconnect.R;
import com.example.campusconnect.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);

        return new CommentViewHolder(view);
    }

    @Override
    /*
    onBindViewHolder() is called for each visible item, and the position is assigned
    automatically by RecyclerView based on the item's index in the comment list.
     */
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        Comment comment = commentList.get(position);

        holder.commentText.setText(
                comment.getUsername() + ": " + comment.getContent()
        );
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView commentText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.commentText);
        }
    }
}