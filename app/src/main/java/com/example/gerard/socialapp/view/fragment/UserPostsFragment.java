package com.example.gerard.socialapp.view.fragment;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class UserPostsFragment extends PostsFragment {
    @Override
    Query setQuery(){
        return  db.collection("posts").orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING).whereEqualTo("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
}
