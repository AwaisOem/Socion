package com.example.socion.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Post {
    private String userName , userEmail , userAvatar , caption , postId , postImgUrl;
    private boolean isAnonymous;
    private int likesCount = 0;
    private Map<String, Boolean> likedBy = new HashMap<>();

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }

    public Post() {
    }

    public Post(String userName ,String userEmail ,String userAvatar, String caption, String postImgUrl, boolean isAnonymous) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userAvatar = userAvatar;
        this.caption = caption;
        this.postImgUrl = postImgUrl;
        this.isAnonymous = isAnonymous;
    }
    public Post(String userName ,String userEmail ,String userAvatar,  String caption, String postImgUrl, boolean isAnonymous,  Map<String, Boolean> likedBy) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userAvatar = userAvatar;
        this.caption = caption;
        this.postImgUrl = postImgUrl;
        this.isAnonymous = isAnonymous;
        this.likedBy = likedBy;
    }


    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setPostImgUrl(String postImgUrl) {
        this.postImgUrl = postImgUrl;
    }
    public String getCaption() {
        return caption;
    }
    public String getPostImgUrl() {
        return postImgUrl;
    }
    public boolean isAnonymous() {
        return isAnonymous;
    }
}