package com.example.socion.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.socion.R;
import com.example.socion.databinding.FragmentHomeBinding;
import com.example.socion.utils.Post;
import com.example.socion.utils.PostAdapter;
import com.example.socion.utils.UserPostAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PostAdapter publicPostAdapter;
    private PostAdapter anonPostAdapter;
    private DatabaseReference dbr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbr = FirebaseDatabase.getInstance().getReference().child("posts");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);


        List<Post> publicPostList = new ArrayList<>();
        List<Post> anonPostList = new ArrayList<>();

        publicPostAdapter = new PostAdapter(binding.getRoot().getContext(), publicPostList);
        anonPostAdapter = new PostAdapter(binding.getRoot().getContext(), anonPostList);

        LinearLayoutManager publicLayoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        publicLayoutManager.setReverseLayout(true);
        publicLayoutManager.setStackFromEnd(true);

        LinearLayoutManager anonLayoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        anonLayoutManager.setReverseLayout(true);
        anonLayoutManager.setStackFromEnd(true);

        binding.publicPosts.setLayoutManager(publicLayoutManager);
        binding.publicPosts.setAdapter(publicPostAdapter);

        binding.anonymousPosts.setLayoutManager(anonLayoutManager);
        binding.anonymousPosts.setAdapter(anonPostAdapter);




        dbr.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        anonPostList.clear();
                        publicPostList.clear();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            if (post != null) {
                                post.setPostId(postSnapshot.getKey());
                                if(post.isAnonymous()){
                                    anonPostList.add(post);
                                }else{
                                    publicPostList.add(post);
                                }
                            }
                        }
                        publicPostAdapter.notifyDataSetChanged();
                        anonPostAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });






        binding.homeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.home_radiobtn1){
                binding.publicPosts.setVisibility(View.VISIBLE);
                binding.anonymousPosts.setVisibility(View.GONE);
            }else if(checkedId == R.id.home_radiobtn2){
                binding.publicPosts.setVisibility(View.GONE);
                binding.anonymousPosts.setVisibility(View.VISIBLE);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}