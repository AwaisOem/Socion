package com.example.socion.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    DatabaseReference dbr;
    private Context ctx;
    FirebaseUser user;

    MySharedPreferences sharedPreferences;

    public PostAdapter(Context ctx, List<Post> postList) {
        this.ctx = ctx;
        this.postList = postList;
        user = FirebaseAuth.getInstance().getCurrentUser();
        dbr = FirebaseDatabase.getInstance().getReference().child("posts");
        sharedPreferences = new MySharedPreferences(ctx);

    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_on_home, parent, false);
        return new PostViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.textCaption.setText(post.getCaption());
        Glide.with(ctx)
                .load(post.getPostImgUrl())
                .into(holder.postImage);



        if (post.getLikedBy() != null && post.getLikedBy().containsKey(user.getUid())) {
            holder.likeBtn.setImageResource(R.drawable.heart_solid);
        } else {
            holder.likeBtn.setImageResource(R.drawable.heart_regular);
        }
        holder.textLikeCount.setText((post.getLikesCount()) + " Likes");
        holder.likeBtn.setOnClickListener(v -> {
            int newLikesCount = post.getLikesCount();
            if (post.getLikedBy() == null || !post.getLikedBy().containsKey(user.getUid())) {
                newLikesCount++;
                // Update likes count and add user to likedBy (if enabled)
                dbr.child(post.getPostId()).child("likesCount").setValue(newLikesCount);
                if (post.getLikedBy() != null) {
                    post.getLikedBy().put(user.getUid(), true);
                    dbr.child(post.getPostId()).child("likedBy").setValue(post.getLikedBy());
                }
            } else {
                newLikesCount--;
                // Update likes count and remove user from likedBy (if enabled)
                dbr.child(post.getPostId()).child("likesCount").setValue(newLikesCount);
                if (post.getLikedBy() != null) {
                    post.getLikedBy().remove(user.getUid());
                    dbr.child(post.getPostId()).child("likedBy").setValue(post.getLikedBy());
                }
            }
            holder.textLikeCount.setText(String.valueOf(newLikesCount) + " Likes");
        });



        if(post.isAnonymous()){
            holder.userDetails.setVisibility(View.GONE);
        }else{
            if(!post.getUserAvatar().isEmpty()){
                Glide.with(ctx)
                        .load(post.getUserAvatar())
                        .circleCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(holder.postProfilePic);
            }
            holder.textUserName.setText(post.getUserName());
            holder.textUserEmail.setText(post.getUserEmail());


            holder.saveBtn.setImageResource(
                    sharedPreferences.getBookmarkedPostIds().contains(post.getPostId())?
                    R.drawable.bookmark_solid : R.drawable.bookmark_regular);


            holder.saveBtn.setOnClickListener(v -> {
                boolean isBookmarked = sharedPreferences.getBookmarkedPostIds().contains(post.getPostId());
                    sharedPreferences.setBookmarked(post.getPostId(), !isBookmarked);
                    holder.saveBtn.setImageResource(isBookmarked ?
                            R.drawable.bookmark_regular : R.drawable.bookmark_solid);
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textCaption , textLikeCount , textUserName ,textUserEmail;
        ImageView postImage, postProfilePic;
        ImageButton likeBtn,saveBtn,shareBtn;
        LinearLayout userDetails;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textCaption = itemView.findViewById(R.id.post_caption);
            textLikeCount = itemView.findViewById(R.id.post_likes_count);
            textUserName = itemView.findViewById(R.id.post_name);
            textUserEmail = itemView.findViewById(R.id.post_email);
            postImage = itemView.findViewById(R.id.post_image);
            postProfilePic = itemView.findViewById(R.id.post_profile_pic);
            saveBtn = itemView.findViewById(R.id.post_save_btn);
            likeBtn = itemView.findViewById(R.id.post_like_btn);
            shareBtn = itemView.findViewById(R.id.post_share_btn);
            userDetails = itemView.findViewById(R.id.post_user_details);
        }
    }
}
