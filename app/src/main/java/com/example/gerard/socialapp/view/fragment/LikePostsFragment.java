package com.example.gerard.socialapp.view.fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;


public class LikePostsFragment extends PostsFragment {

    @Override
    public Query setQuery(){
        return  db.collection("posts").orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING).whereEqualTo("likes."+FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }
}
