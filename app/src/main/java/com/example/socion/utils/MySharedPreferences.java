package com.example.socion.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class MySharedPreferences {

    private static final String SHARED_PREFS_NAME = "BookmarkedPosts";
    private static final String BOOKMARKED_POSTS_KEY = "bookmarked_posts";

    private SharedPreferences sharedPreferences;

    public MySharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setBookmarked(String postId, boolean isBookmarked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(BOOKMARKED_POSTS_KEY, getBookmarkedPostIds(isBookmarked, postId));
        editor.apply();
    }

    public Set<String> getBookmarkedPostIds() {
        return sharedPreferences.getStringSet(BOOKMARKED_POSTS_KEY, new HashSet<>());
    }

    @SuppressLint("MutatingSharedPrefs")
    private Set<String> getBookmarkedPostIds(boolean addToSet, String postId) {
        Set<String> bookmarkedPosts = sharedPreferences.getStringSet(BOOKMARKED_POSTS_KEY, new HashSet<>());
        if (addToSet) {
            bookmarkedPosts.add(postId);
        } else {
            bookmarkedPosts.remove(postId);
        }
        return bookmarkedPosts;
    }
}
