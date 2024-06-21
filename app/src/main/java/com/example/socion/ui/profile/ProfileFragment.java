package com.example.socion.ui.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.socion.LoginActivity;
import com.example.socion.MainActivity;
import com.example.socion.R;
import com.example.socion.databinding.FragmentProfileBinding;
import com.example.socion.utils.MySharedPreferences;
import com.example.socion.utils.Post;
import com.example.socion.utils.PostAdapter;
import com.example.socion.utils.UserPostAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProfileFragment extends Fragment {
    private DatabaseReference dbr;
    private FragmentProfileBinding binding;
    private UserPostAdapter userPostAdapter;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbr= FirebaseDatabase.getInstance().getReference().child("posts");
        mAuth = FirebaseAuth.getInstance();

    }
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            binding.profileName.setText(user.getDisplayName());
            binding.profileEmail.setText(user.getEmail());
            if(user.getPhotoUrl()!=null){
                Glide.with(binding.getRoot().getContext())
                        .load(user.getPhotoUrl().toString())
                        .circleCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(binding.profileAvatar);
            }



            List<Post> postList = new ArrayList<>();
            userPostAdapter = new UserPostAdapter(binding.getRoot().getContext(), postList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            binding.profileUserPosts.setLayoutManager(layoutManager);
            binding.profileUserPosts.setAdapter(userPostAdapter);



            TextView postCountText= binding.postCountText;
//            TextView postCountText= binding.getRoot().findViewById(R.id.post_count_text);

            dbr.orderByChild("userEmail").equalTo(user.getEmail())
                    .addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Post post = postSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setPostId(postSnapshot.getKey());
                            postList.add(post);
                        }
                    }
                    userPostAdapter.notifyDataSetChanged();
                    postCountText.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });


            fetchBookmarkedPosts();
        }


        // Event Listeners

        binding.profileRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.profile_radio_button1){
                binding.profileUserSaved.setVisibility(View.GONE);
                binding.profileUserPosts.setVisibility(View.VISIBLE);
            }else if(checkedId == R.id.profile_radio_button2){
                binding.profileUserSaved.setVisibility(View.VISIBLE);
                binding.profileUserPosts.setVisibility(View.GONE);
            }
        });
        return binding.getRoot();
    }
    public void fetchBookmarkedPosts() {
        MySharedPreferences sharedPreferences = new MySharedPreferences(binding.getRoot().getContext());
        List<String> savedPostIdsList = new ArrayList<>(sharedPreferences.getBookmarkedPostIds());
        List<Post> savedPostsList = new ArrayList<>();

        PostAdapter savedPostAdapter = new PostAdapter(binding.getRoot().getContext(), savedPostsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.profileUserSaved.setLayoutManager(layoutManager);
        binding.profileUserSaved.setAdapter(savedPostAdapter);

        dbr.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedPostsList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && savedPostIdsList.contains(postSnapshot.getKey())) {
                            post.setPostId(postSnapshot.getKey());
                            savedPostsList.add(post);
                    }
                }
                savedPostAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(binding.getRoot().getContext(),LoginActivity.class));
            mAuth.signOut();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

