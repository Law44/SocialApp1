package com.example.gerard.socialapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.gerard.socialapp.GlideApp;
import com.example.gerard.socialapp.R;
import com.example.gerard.socialapp.model.Post;
import com.example.gerard.socialapp.view.PostViewHolder;
import com.example.gerard.socialapp.view.activity.MediaActivity;
import com.example.gerard.socialapp.view.activity.PostsActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;



public abstract class PostsFragment extends Fragment implements PostsActivity.QueryChangeListener {
    public DatabaseReference mReference;
    public FirebaseUser mUser;
    FirebaseFirestore db;
    FirestoreRecyclerAdapter adapter;
    RecyclerView recycler;
    FirestoreRecyclerOptions<Post> options;
    View view;
    Button refresh;
    int nuevos;
    boolean firstTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_posts, container, false);
        refresh = view.findViewById(R.id.btn_refresh);

        mReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();
        recycler = view.findViewById(R.id.rvPosts);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.onQueryChange("");
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0){
                    refresh.setVisibility(View.INVISIBLE);
                }
            }
        });
        return view;

    }


    Query setQuery(){
        return  db.collection("posts").orderBy("date", Query.Direction.DESCENDING);
    }


    Query setSearchQuery(String query){
        return  db.collection("posts").whereEqualTo("author", query).orderBy("date", Query.Direction.DESCENDING);
    }


    @Override
    public void onQueryChange(String query) {

        if (query.isEmpty()) {
            options = new FirestoreRecyclerOptions.Builder<Post>()
                    .setQuery(setQuery(),Post.class)
                    .setLifecycleOwner(this)
                    .build();
        } else {
            options = new FirestoreRecyclerOptions.Builder<Post>()
                    .setQuery(setSearchQuery(query), Post.class)
                    .setLifecycleOwner(this)
                    .build();
        }


        adapter = new FirestoreRecyclerAdapter<Post, PostViewHolder>(options) {

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
            }

            @Override
            public void onChildChanged(@NonNull ChangeEventType type, @NonNull DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
                if (type == ChangeEventType.ADDED){
                    if (recycler.canScrollVertically(View.SCROLL_AXIS_VERTICAL)) {
                        nuevos++;
                        refresh.setVisibility(View.VISIBLE);
                        if (nuevos > 1){
                            refresh.setText(String.valueOf(nuevos + " tweets nuevos"));
                        }
                        else {
                            refresh.setText(String.valueOf(nuevos + " tweet nuevo"));
                        }
                        refresh.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                nuevos = 0;
                                refresh.setVisibility(View.INVISIBLE);
                                recycler.scrollToPosition(adapter.getItemCount() - 1);
                                recycler.scrollToPosition(0);
                            }
                        });
                    }
                }
            }

            @Override
            protected void onBindViewHolder(final PostViewHolder viewHolder, final int position, final Post post) {

                viewHolder.author.setText(post.author);
                GlideApp.with(PostsFragment.this).load(post.authorPhotoUrl).circleCrop().into(viewHolder.photo);

                if (post.likes.containsKey(mUser.getUid())) {
                    viewHolder.like.setImageResource(R.drawable.heart_on);
                    viewHolder.numLikes.setTextColor(getResources().getColor(R.color.red));
                } else {
                    viewHolder.like.setImageResource(R.drawable.heart_off);
                    viewHolder.numLikes.setTextColor(getResources().getColor(R.color.grey));
                }

                viewHolder.content.setText(post.content);

                if (post.mediaUrl != null) {
                    viewHolder.image.setVisibility(View.VISIBLE);
                    if ("audio".equals(post.mediaType)) {
                        viewHolder.image.setImageResource(R.drawable.audio);
                    } else {
                        GlideApp.with(PostsFragment.this).load(post.mediaUrl).centerCrop().into(viewHolder.image);

                    }
                    viewHolder.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), MediaActivity.class);
                            intent.putExtra("mediaUrl", post.mediaUrl);
                            intent.putExtra("mediaType", post.mediaType);
                            startActivity(intent);
                        }
                    });
                } else {
                    viewHolder.image.setVisibility(View.GONE);
                }

                viewHolder.numLikes.setText(String.valueOf(post.likes.size()));


                viewHolder.likeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!post.likes.containsKey(mUser.getUid())) {
                            post.likes.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                            db.collection("posts").document(getSnapshots().getSnapshot(position).getId()).set(post);
                        } else {
                            post.likes.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            db.collection("posts").document(getSnapshots().getSnapshot(position).getId()).set(post);
                        }
                    }
                });
            }
        };
        recycler.setAdapter(adapter);


    }


}
