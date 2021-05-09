package com.example.chatbook.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatbook.R;
import com.example.chatbook.Model.Status;
import com.example.chatbook.Adapter.TopStatusAdapter;
import com.example.chatbook.Model.User;
import com.example.chatbook.Model.UserStatus;
import com.example.chatbook.Adapter.UsersAdapter;
import com.example.chatbook.databinding.ActivityMainBinding;
import com.example.chatbook.sendNotification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private int STORAGE_PERMISSION_CODE=123;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

       dialog = new ProgressDialog(this);
       dialog.setMessage("Uploading Image....");
       dialog.setCancelable(false);
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            //Toast.makeText(this, "Thanks For Giving Permission Your Data is Secure", Toast.LENGTH_SHORT).show();
        }else {
            requestAllPermissions();
        }


        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user =  snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        usersAdapter = new UsersAdapter(this,users);
        statusAdapter = new TopStatusAdapter(this, userStatuses);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statuLst.setLayoutManager(linearLayoutManager);
        binding.recylerView.setAdapter(usersAdapter);
        binding.statuLst.setAdapter(statusAdapter);
        binding.recylerView.showShimmerAdapter();
        binding.statuLst.showShimmerAdapter();
        updateToken(FirebaseInstanceId.getInstance().getToken());
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        users.add(user);
                }

                binding.recylerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               
            }
        });
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot:storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                    binding.statuLst.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Sorry", Toast.LENGTH_SHORT).show();
            }
        });
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.Status:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,75);
                        break;
                }
                return false;
            }
        });
    }

    private void requestAllPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this).setTitle("Grant Those Permission").setMessage("Read_Contact,Camera and Read_External_Storage").setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                }
            }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] + grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED){
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if(data.getData() != null){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference =  storage.getReference().child("status").child(date.getTime() + "");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(date.getTime());
                                    HashMap<String, Object> obj = new HashMap<>();
                                    obj.put("name",userStatus.getName());
                                    obj.put("profileImage",userStatus.getProfileImage());
                                    obj.put("lastUpdated",userStatus.getLastUpdated());

                                    String imageUrl = uri.toString();

                                    Status status = new Status(imageUrl,userStatus.getLastUpdated());

                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).child("statuses").push().setValue(status);

                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }
    public void updateToken(String token){
         DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(FirebaseAuth.getInstance().getUid()).setValue(mToken);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
        String currentId  = FirebaseAuth.getInstance().getUid();

        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",user.getUid());
            editor.apply();
        }
    }

    @Override
    protected void onPause() {
        String currentId  = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("offline");
        super.onPause();
}

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        //super.onBackPressed();
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.Search:
                Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Settings:
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.LogOut:
                signOut();
                break;
            case R.id.Invite:
                onShareClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(this, "SignOut Successfully", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(this, "SignOut not Successfully", Toast.LENGTH_SHORT).show();
        }
    }
    private void onShareClicked() {

        String link = "https://drive.google.com/file/d/1K7RdjXZyqNwLh21bpERSQD4Pzpm0MgKV/view?usp=sharing";

        Uri uri = Uri.parse(link);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link.toString());
        intent.putExtra(Intent.EXTRA_TITLE, "ChatBook");

        startActivity(Intent.createChooser(intent, "Share Link"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}