package com.example.socion.ui.create;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.socion.R;
import com.example.socion.databinding.FragmentCreateBinding;
import com.example.socion.utils.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

public class CreateFragment extends Fragment {

    private FragmentCreateBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri post_image_url = null;
    private StorageReference mStorageRef;
    private ActivityResultLauncher<Intent> mGetContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorageRef = FirebaseStorage.getInstance().getReference("upload");

        // Initialize ActivityResultLauncher
        mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        post_image_url = data.getData();
                        ImageView pi= binding.getRoot().findViewById(R.id.post_image);
                        pi.setImageURI(post_image_url);
                    }
                }
        );

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateBinding.inflate(inflater, container, false);


        // Event Listeners
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch anonSwitch = binding.getRoot().findViewById(R.id.post_anonymous);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView pe = binding.getRoot().findViewById(R.id.post_profile_email);
        TextView pn = binding.getRoot().findViewById(R.id.post_profile_name);
        anonSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                if(user!=null){
                    pn.setText(user.getDisplayName());
                    pe.setText(user.getEmail());
                }else{
                    pn.setText("Your Name");
                    pe.setText("example@gmail.com");
                }
            }else{
                pn.setText("Anonymous");
                pe.setText("****************");
            }
        });
        binding.getRoot().findViewById(R.id.upload_post_image).setOnClickListener(v -> openFileChooser());


        Button post_button = binding.getRoot().findViewById(R.id.post_button);
        post_button.setOnClickListener(v -> {
            if(post_image_url ==null){
                Toast t = DynamicToast.makeError(requireContext() , "Upload Any Image First");
                t.setGravity(Gravity.TOP , 0 , 0);
                t.show();
                return;
            }
            EditText pc = binding.getRoot().findViewById(R.id.post_captions);
            String captions = String.valueOf(pc.getText());
            if(user!=null){
                postToFirebase(post_image_url , captions , anonSwitch.isChecked() , user,post_button);
            }else{
                Toast t = DynamicToast.makeError(requireContext() , "Authenticate First");
                t.setGravity(Gravity.TOP , 0 , 0);
                t.show();
            }
        });

        return binding.getRoot();
    }

    private void resetFields(){
        EditText pc = binding.getRoot().findViewById(R.id.post_captions);
        pc.setText("");
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch anonSwitch = binding.getRoot().findViewById(R.id.post_anonymous);
        anonSwitch.setChecked(true);
        post_image_url = null;
        ImageView pi= binding.getRoot().findViewById(R.id.post_image);
        pi.setImageURI(null);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mGetContent.launch(intent);
    }

    private void postToFirebase(Uri mImageUri,String captions , boolean isAnonymous , FirebaseUser userObj, Button post_button) {
        if (mImageUri != null) {
            startLoading();
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {

                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            Post post = new Post(userObj.getDisplayName(), userObj.getEmail(), userObj.getPhotoUrl()!=null ? userObj.getPhotoUrl().toString():"", captions, uri.toString(), isAnonymous);
                            FirebaseDatabase.getInstance().getReference().child("posts")
                                    .push().setValue(post)
                                    .addOnSuccessListener(unused -> {
                                        Toast t = DynamicToast.makeSuccess(requireContext() , "Successfully Posted");
                                        t.setGravity(Gravity.TOP , 0 , 0);
                                        t.show();
                                        resetFields();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast t = DynamicToast.makeSuccess(requireContext() , "Error in Posting");
                                        t.setGravity(Gravity.TOP , 0 , 0);
                                        t.show();
                                    }).addOnCompleteListener(task -> stopLoading());
                        }).addOnFailureListener(e -> {
                            Log.e("CreateFragment","GetDownloadUrlError"+e.getMessage());
                            Toast t = DynamicToast.makeError(requireContext() , "URL Error");
                            t.setGravity(Gravity.TOP , 0 , 0);
                            t.show();
                            stopLoading();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CreateFragment","ImageUploadError"+e.getMessage());
                        Toast t = DynamicToast.makeError(requireContext() , "Upload Error");
                        t.setGravity(Gravity.TOP , 0 , 0);
                        t.show();
                        stopLoading();
                    });
        } else {
            Toast t = DynamicToast.makeWarning(requireContext() , "No file selected");
            t.setGravity(Gravity.TOP , 0 , 0);
            t.show();
        }
    }


    private void startLoading(){
        binding.postButton.setEnabled(false);
        binding.postButton.setVisibility(View.GONE);
        binding.postProgressBar.setVisibility(View.VISIBLE);
    }
    private void stopLoading(){
        binding.postButton.setVisibility(View.VISIBLE);
        binding.postButton.setEnabled(true);
        binding.postProgressBar.setVisibility(View.GONE);
    }
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(requireActivity().getContentResolver().getType(uri));
    }
} 