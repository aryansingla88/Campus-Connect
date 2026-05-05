/*Till now the chat activity is only loading the posts */
package com.example.campusconnect.phase1.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.campusconnect.R;
import com.example.campusconnect.adapter.PostAdapter;
import com.example.campusconnect.phase1.model.CreatePostRequest;
import com.example.campusconnect.phase1.model.Post;
import com.example.campusconnect.network.PostService;
import com.example.campusconnect.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.widget.Toolbar;

public class ChatActivity extends AppCompatActivity {
    /*
    LOGOUT Logic:

    1) Clears saved login (SharedPreferences)
    2) Opens LoginActivity
    3) Prevents user from returning using Back button
     */

    RecyclerView recyclerView;
    EditText postInput;
    Button postButton;
    private PostAdapter adapter;
    private List<Post> postList;
    SwipeRefreshLayout swipeRefresh;
    private LinearLayout refreshOverlay;
    private LottieAnimationView refreshAnimation;
    private long animationStartTime = 0;
    private static final long MIN_ANIMATION_DURATION = 600; // 4 seconds
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh) {

            showRefreshAnimation();

            swipeRefresh.setRefreshing(true);

            loadPosts();

            return true;
        }

        if (id == R.id.action_logout) {

            logoutUser();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // WORKING OF REFRESH ANIMATION(animation plays 0.6 sec minimum):-
//    Scenario 1 — Fast backend (0.5 sec)
//
//    Animation starts
//    Response arrives at 0.5 sec
//    System waits remaining 0.1 sec
//    Animation disappears at 0.6 sec
//
//    Scenario 2 — Slow backend (6 sec)
//
//    Animation starts
//    Response arrives at 6 sec
//    Animation disappears immediately at response arrival

    private void showRefreshAnimation() {

        refreshOverlay.setVisibility(View.VISIBLE);

        refreshAnimation.playAnimation();

        animationStartTime = System.currentTimeMillis();

    }

    private void hideRefreshAnimation() {

        long elapsed =
                System.currentTimeMillis() - animationStartTime;

        long remaining =
                MIN_ANIMATION_DURATION - elapsed;

        if (remaining > 0) {

            refreshOverlay.postDelayed(() -> {

                refreshAnimation.cancelAnimation();
                refreshOverlay.setVisibility(View.GONE);

            }, remaining);

        } else {

            refreshAnimation.cancelAnimation();
            refreshOverlay.setVisibility(View.GONE);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        refreshOverlay = findViewById(R.id.refreshOverlay);
        refreshAnimation = findViewById(R.id.refreshAnimation);
        recyclerView = findViewById(R.id.postRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postInput = findViewById(R.id.postInput);
        postButton = findViewById(R.id.postButton);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        postList = new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(() -> {

            showRefreshAnimation();
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


            PostService service = RetrofitClient.getPostService();

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
                    Log.d("DEBUG", "postList size: " + postList.size());
                    loadPosts();
// scroll only if you want

                    recyclerView.scrollToPosition(0);
                }

                @Override
                public void onFailure(Call<Void> call,
                                      Throwable t) {

                    t.printStackTrace();
                    Toast.makeText(ChatActivity.this, "backend didn't respond!", Toast.LENGTH_SHORT).show();
                }
            });

        });

        loadPosts();
    }
    private void logoutUser() {

        // 1) Clear saved login

        SharedPreferences prefs =
                getSharedPreferences("CampusApp", MODE_PRIVATE);

        prefs.edit().clear().apply();

        // 2) Go to Login screen

        Intent intent =
                new Intent(
                        ChatActivity.this,
                        LoginActivity.class
                );

        // 3) Prevent returning with back button

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }

    private void loadPosts() {

        PostService service = RetrofitClient.getPostService();

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
                hideRefreshAnimation();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

                t.printStackTrace();
                hideRefreshAnimation();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

}