package com.example.allo_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.allo_chat.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import Models.Users;

public class Settings extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.etStatus.getText().toString().isEmpty() && !binding.txtUsername.getText().toString().isEmpty()) {
                    String status = binding.etStatus.getText().toString();
                    String username = binding.txtUsername.getText().toString();

                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("username", username);
                    obj.put("status", status);

                    database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
                    Toast.makeText(Settings.this, "Profile updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Settings.this, "Enter username and status", Toast.LENGTH_SHORT).show();
                }
            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                if (users != null) {
                    binding.txtUsername.setText(users.getUsername());
                    binding.etStatus.setText(users.getStatus());
                    if (users.getProfilepic() != null && !users.getProfilepic().isEmpty()) {
                        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.avatar3).into(binding.profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sFile = data.getData();
            binding.profileImage.setImageURI(sFile);

            final StorageReference reference = storage.getReference().child("profile_image").child(FirebaseAuth.getInstance().getUid());
            reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();

                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilepic").setValue(imageUrl)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Settings.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(Settings.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                }
            }).addOnFailureListener(e -> Toast.makeText(Settings.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }
}
