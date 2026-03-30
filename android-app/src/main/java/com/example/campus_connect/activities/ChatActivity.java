/*Till now the chat activity is only loading the posts */
package com.example.campus_connect.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.campus_connect.R;
import com.example.campus_connect.adapter.PostAdapter;
import com.example.campus_connect.api.PostService;
import com.example.campus_connect.config.ApiConfig;
import com.example.campus_connect.model.Comment;
import com.example.campus_connect.model.CreatePostRequest;
import com.example.campus_connect.model.Post;
import com.example.campus_connect.network.RetrofitClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText postInput;
    Button postButton;
    private PostAdapter adapter;
    private List<Post> postList;
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        recyclerView = findViewById(R.id.postRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postInput = findViewById(R.id.postInput);
        postButton = findViewById(R.id.postButton);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        postList = new ArrayList<>();
        swipeRefresh.setOnRefreshListener(() -> {

            loadPosts();

        });

        adapter = new PostAdapter(
                postList,
                null
        );

        recyclerView.setAdapter(adapter);

        /*
        below 3 lines of code and  getDummyPosts() function are temporarily for testing
        otherwise, only one line below loadPosts(), it itself calls post adapter and populates the view with posts.
         */
        postButton.setOnClickListener(v -> {

            String content = postInput.getText().toString();

            if(content.isEmpty())
                return;

            SharedPreferences prefs =
                    getSharedPreferences("CampusApp", MODE_PRIVATE);

            int userId = prefs.getInt("user_id", -1);

            CreatePostRequest request =
                    new CreatePostRequest(userId, content);

            PostService service =
                    RetrofitClient.getInstance().create(PostService.class);

            service.createPost(request).enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call,
                                       Response<Void> response) {

                    postInput.setText("");

                    Post newPost = new Post();

                    newPost.setUsername("You");
                    newPost.setContent(content);
                    newPost.setUpvotes(0);
                    newPost.setHas_upvoted(false);
                    newPost.setCreated_at("now");
                    newPost.setComments(new ArrayList<>());

// add to top

                    postList.add(0, newPost);

                    adapter.notifyItemInserted(0);

// scroll only if you want

                    recyclerView.scrollToPosition(0);
                }

                @Override
                public void onFailure(Call<Void> call,
                                      Throwable t) {

                    t.printStackTrace();
                }
            });

        });

        loadPosts();
    }

    private void loadPosts() {

        PostService service = RetrofitClient.getInstance().create(PostService.class);

        SharedPreferences prefs =
                getSharedPreferences("CampusApp", MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);

        Call<List<Post>> call =
                service.getPosts(userId);

        call.enqueue(new Callback<List<Post>>() {

            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if(response.isSuccessful()) {

                    List<Post> posts = response.body();
                    /*
                    Here we are getting the post list from the backend response
                    and below we are creating a new PostAdapter object with the parametrised constructor
                     giving it the list of posts
                     */

                    /*
                    load posts is getting the feed created with the help of postadapter
                     and it also passes a function to postadapter, that whenever some event
                     occurs on your screen(comment, upvote) call this function
                     and the implementation of the function is such that it calls loadposts again.
                     Thus, loadposts calls postadapter to create a feed and
                     that feed has buttons that call loadposts again.
                     */
                    PostAdapter adapter = new PostAdapter(
                            posts,
                            () -> loadPosts()
                    );
                    recyclerView.setAdapter(adapter);
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

                t.printStackTrace();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

}