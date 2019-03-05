package com.example.gerard.socialapp.view.fragment;

import com.google.firebase.firestore.Query;

public class NewestPostsFragments extends PostsFragment {

    public Query setQuery(){
        return  db.collection("posts").orderBy("date", Query.Direction.DESCENDING);
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
