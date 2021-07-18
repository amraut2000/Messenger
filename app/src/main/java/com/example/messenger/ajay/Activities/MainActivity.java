package com.example.messenger.ajay.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.messenger.ajay.Adapters.TopStatusAdapter;
import com.example.messenger.ajay.Adapters.UsersAdapter;
import com.example.messenger.ajay.Models.Status;
import com.example.messenger.ajay.Models.User;
import com.example.messenger.ajay.Models.UserStatus;
import com.example.messenger.ajay.R;
import com.example.messenger.ajay.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ArrayList<User> users;
    UsersAdapter usersAdapter;

    ArrayList<UserStatus> userStatuses;
    TopStatusAdapter topStatusAdapter;

    FirebaseDatabase firebaseDatabase;

    ProgressDialog progressDialog;

    User currentUser;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        currentUser=snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        users = new ArrayList<>();
        userStatuses = new ArrayList<>();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        progressDialog.show();

        usersAdapter = new UsersAdapter(users, this);
        binding.usersRecyclerView.setAdapter(usersAdapter);

        topStatusAdapter = new TopStatusAdapter(userStatuses, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusRecyclerView.setLayoutManager(layoutManager);
        binding.statusRecyclerView.setAdapter(topStatusAdapter);


        firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                        users.add(user);
                }
                usersAdapter.notifyDataSetChanged();
                progressDialog.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Something wrong happened!", Toast.LENGTH_SHORT).show();
            }
        });

        firebaseDatabase.getReference().child("Stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot: snapshot.getChildren()){
                        UserStatus userStatus=new UserStatus();
                        userStatus.setName(storySnapshot.child("name").getValue(String.class));
                        userStatus.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        userStatus.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses=new ArrayList<>();

                        for(DataSnapshot statusSnapshot: storySnapshot.child("Statuses").getChildren()){
                            Status status=statusSnapshot.getValue(Status.class);
                            statuses.add(status);
                        }
                        userStatus.setStatuses(statuses);
                        userStatuses.add(userStatus);
                    }
                    topStatusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.status:
                        Intent intent=new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,22);
                        break;

                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null && data.getData()!=null){
            progressDialog.show();

            FirebaseStorage storage=FirebaseStorage.getInstance();
            Date date=new Date();
            StorageReference reference=storage.getReference().child("Status").child(date.getTime()+"");
            reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UserStatus userStatus=new UserStatus();
                                userStatus.setName(currentUser.getName());
                                userStatus.setProfileImage(currentUser.getProfileImage());
                                userStatus.setLastUpdated(date.getTime());

                                HashMap<String,Object> obj=new HashMap<>();
                                obj.put("name",userStatus.getName());
                                obj.put("profileImage",userStatus.getProfileImage());
                                obj.put("lastUpdated",userStatus.getLastUpdated());

                                String imageUrl=uri.toString();
                                Status status=new Status(imageUrl,userStatus.getLastUpdated());

                                firebaseDatabase.getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                        .updateChildren(obj);

                                firebaseDatabase.getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                        .child("Statuses").push().setValue(status);


                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "settings clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite:
                Toast.makeText(this, "Invite clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.groups:
                Toast.makeText(this, "Groups clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}