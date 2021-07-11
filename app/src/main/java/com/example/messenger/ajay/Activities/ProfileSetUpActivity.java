package com.example.messenger.ajay.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.messenger.ajay.Models.User;
import com.example.messenger.ajay.databinding.ActivityProfileSetUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileSetUpActivity extends AppCompatActivity {

    ActivityProfileSetUpBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;

    Uri selectedImage;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSetUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Setting up the profile");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        binding.setupProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                setUpProfile();
            }
        });
    }

    private void setUpProfile() {
        String name = binding.nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            binding.nameEditText.setError("Name is required!");
            binding.nameEditText.requestFocus();
            return;
        }
        if (selectedImage != null) {
            StorageReference storageReference = firebaseStorage.getReference().child("Profiles").child(auth.getUid());
            storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                String userName = name;
                                String id = auth.getUid();
                                String number = auth.getCurrentUser().getPhoneNumber();

                                User user = new User(id, userName, imageUrl, number);

                                firebaseDatabase.getReference()
                                        .child("Users")
                                        .child(id)
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(ProfileSetUpActivity.this, MainActivity.class));
                                            }
                                        });
                            }
                        });
                    }
                }
            });
        } else {
            String imageUrl = "No Image";
            String userName = name;
            String id = auth.getUid();
            String number = auth.getCurrentUser().getPhoneNumber();

            User user = new User(id, userName, imageUrl, number);

            firebaseDatabase.getReference()
                    .child("Users")
                    .child(id)
                    .setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            startActivity(new Intent(ProfileSetUpActivity.this, MainActivity.class));
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 21);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                binding.profileImage.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}