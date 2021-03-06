package com.example.gerard.socialapp.view.fragment;

import com.example.gerard.socialapp.view.activity.PostsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class UserPostsFragment extends PostsFragment{

    @Override
    public Query setQuery(){
        return  db.collection("posts").whereEqualTo("uid",FirebaseAuth.getInstance().getCurrentUser().getUid()).orderBy("date", Query.Direction.DESCENDING);
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
