package com.example.socion.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socion.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.List;


public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.UserPostViewHolder> {
    private List<Post> postList;
    private String username;
    private String email;
    private String avatar_url;
    private Context ctx;
    DatabaseReference dbr;

    public UserPostAdapter(Context ctx, List<Post> postList) {
        this.ctx = ctx;
        this.postList = postList;
        this.dbr = FirebaseDatabase.getInstance().getReference().child("posts");
    }

    @NonNull
    @Override
    public UserPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_post_item_on_profile, parent, false);
        return new UserPostViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserPostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.textCaption.setText(post.getCaption());
        if(!post.getUserAvatar().isEmpty() && !post.isAnonymous()){
            Glide.with(ctx)
                .load(post.getUserAvatar())
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.postProfilePic);
        }
        if(post.isAnonymous()){
            holder.textUserName.setText("Anonymous");
            holder.textUserEmail.setText("*****************");
        }else{
            holder.textUserName.setText(post.getUserName());
            holder.textUserEmail.setText(post.getUserEmail());
        }
        Glide.with(ctx)
                .load(post.getPostImgUrl())
                .into(holder.postImage);
        holder.deleteBtn.setOnClickListener(v -> showDeleteConfirmationDialog(post.getPostId()));
        holder.textLikeCount.setText(post.getLikesCount()+" Likes");
    }

    private void showDeleteConfirmationDialog(String postId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbr.child(postId).removeValue();
                    postList.remove(getPositionForPostId(postId));
                    notifyItemRemoved(getPositionForPostId(postId));
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private int getPositionForPostId(String postId) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).getPostId().equals(postId)) {
                return i;
            }
        }
        return -1; // Not found
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class UserPostViewHolder extends RecyclerView.ViewHolder {
        TextView textCaption , textLikeCount , textUserName ,textUserEmail;
        ImageView postImage, postProfilePic;
        ImageButton deleteBtn;


        public UserPostViewHolder(@NonNull View itemView) {
            super(itemView);
            textCaption = itemView.findViewById(R.id.user_post_caption);
            textUserName = itemView.findViewById(R.id.user_post_name);
            textUserEmail = itemView.findViewById(R.id.user_post_email);
            textLikeCount = itemView.findViewById(R.id.user_post_likes_count);
            postImage = itemView.findViewById(R.id.user_post_image);
            postProfilePic = itemView.findViewById(R.id.user_post_profile_pic);
            deleteBtn = itemView.findViewById(R.id.user_post_delete_btn);
        }
    }
}
