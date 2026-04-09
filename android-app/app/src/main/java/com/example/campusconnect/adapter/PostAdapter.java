package com.example.campusconnect.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusconnect.R;
import com.example.campusconnect.model.Comment;
import com.example.campusconnect.model.CommentRequest;
import com.example.campusconnect.model.Post;
import com.example.campusconnect.model.UpvoteRequest;
import com.example.campusconnect.network.PostService;
import com.example.campusconnect.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    /*
    here we are extending RecyclerView.Adapter class
    (Adapter class is an abstract classes)
    what it does is, it tells RECYCLER VIEW THAT-
    1. How to create item views
    2. How to bind data to those views
    3. How many items exist
     */

    /*
    NOTE*****

    for Adapter class you must implement three methods-
    1. PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    2. void onBindViewHolder(PostViewHoler holder, int position)
    3. int getItemCount()
    */

    public interface OnPostActionListener {
        void onPostUpdated();

    }
    private List<Post> postList;
    private OnPostActionListener listener;


    public PostAdapter(
            List<Post> postList,
            OnPostActionListener listener
    ) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
        here we are creating the view holder for each post

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post,parent,flase);
        the job of LayoutInflater class is to get xml file and create objects of all elements present in it like text view, editText
         and then create the javaobject of the layout(here item_post.xml), false argument means that
          donot attach the item_post layout to the parent right now.

        */

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);

        return new PostViewHolder(view);
        /*
        IMPORANT: The variable 'view' above is the java object of one single post
         */
    }
    /*
    RecyclerView works like this:

    Example: 100 posts, screen shows only 10

    onCreateViewHolder() → called ~10–15 times

    NOT 100 times

👉 Because RecyclerView reuses views (recycling)
     */

    @Override
    /*
    onBindViewHolder: It binds data on every single view/post.
     */
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Post post = postList.get(position);
        if (post.isHas_upvoted()) {
            holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.purple_500));
        } else {
            holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.gray));
        }


        holder.commentRecycler.setNestedScrollingEnabled(false);

        holder.username.setText(post.getUsername());
        holder.content.setText(post.getContent());
        holder.upvotes.setText(post.getUpvotes() + " upvotes");
        holder.postTime.setText(post.getCreated_at());

        // Load comments
        List<Comment> comments = post.getComments();

        CommentAdapter commentAdapter = new CommentAdapter(comments);
        //commentRecycler is a RecyclerView type object
        holder.commentRecycler.setLayoutManager(
                new LinearLayoutManager(holder.itemView.getContext())
        );
        //commentRecycler and commentAdapter work together to display comments
        holder.commentRecycler.setAdapter(commentAdapter);
        holder.commentButton.setOnClickListener(v -> {

            String text =
                    holder.commentInput.getText().toString();

            if (text.isEmpty())
                return;

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            "CampusApp",
                            Context.MODE_PRIVATE
                    );

            int userId = ((SharedPreferences) prefs).getInt("user_id", -1);

            int postId = post.getId();

            CommentRequest request =
                    new CommentRequest(userId, postId, text);

            PostService service =
                    RetrofitClient.getInstance()
                            .create(PostService.class);

            service.createComment(request)
                    .enqueue(new Callback<Void>() {

                        @Override
                        public void onResponse(Call<Void> call,
                                               Response<Void> response) {

                            holder.commentInput.setText("");

                            // create new comment locally

                            Comment newComment = new Comment();

                            newComment.setUsername("You");
                            newComment.setContent(text);

                            // add to list

                            post.getComments().add(newComment);

                            // refresh only this post

                            notifyItemChanged(holder.getAdapterPosition());

                        }

                        @Override
                        public void onFailure(Call<Void> call,
                                              Throwable t) {

                            t.printStackTrace();
                        }
                    });

        });
        holder.upvoteButton.setOnClickListener(v -> {

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            "CampusApp",
                            Context.MODE_PRIVATE
                    );

            int userId = prefs.getInt("user_id", -1);

            int postId = post.getId();

            UpvoteRequest request =
                    new UpvoteRequest(userId, postId);

            PostService service = RetrofitClient.getPostService();

            service.upvotePost(request)
                    .enqueue(new Callback<Void>() {

                        @Override
                        public void onResponse(Call<Void> call,
                                               Response<Void> response) {

                            if(response.isSuccessful()) {

                                if(post.isHas_upvoted()) {

                                    post.setHas_upvoted(false);
                                    post.setUpvotes(post.getUpvotes() - 1);

                                    holder.upvoteButton.setColorFilter(
                                            ContextCompat.getColor(
                                                    context,
                                                    R.color.gray
                                            )
                                    );

                                } else {

                                    post.setHas_upvoted(true);
                                    post.setUpvotes(post.getUpvotes() + 1);

                                    holder.upvoteButton.setColorFilter(
                                            ContextCompat.getColor(
                                                    context,
                                                    R.color.purple_500
                                            )
                                    );
                                }

                                holder.upvotes.setText(
                                        post.getUpvotes() + " upvotes"
                                );

                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call,
                                              Throwable t) {

                            t.printStackTrace();
                        }
                    });

        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //below RecyclerView.ViewHolder, the ViewHolder is a class defined inside the RecyclerView class
    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView content;
        TextView upvotes;
        TextView postTime;

        RecyclerView commentRecycler;

        EditText commentInput;
        Button commentButton;

        ImageView upvoteButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.postUsername);
            content = itemView.findViewById(R.id.postContent);
            upvotes = itemView.findViewById(R.id.postUpvotes);
            postTime = itemView.findViewById(R.id.postTime);

            commentRecycler = itemView.findViewById(R.id.commentRecycler);

            commentInput = itemView.findViewById(R.id.commentInput);
            commentButton = itemView.findViewById(R.id.commentButton);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);

        }
    }
}