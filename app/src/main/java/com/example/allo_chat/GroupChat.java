package com.example.allo_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.allo_chat.Adapters.Chatadapter;
import com.example.allo_chat.databinding.ActivityGroupChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

import Models.MessageModel;

public class GroupChat extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST_CODE = 1;
    ActivityGroupChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        final String senderId = mAuth.getUid();

        binding.back.setOnClickListener(view -> {
            Intent intent = new Intent(GroupChat.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final Chatadapter chatadapter = new Chatadapter(messageModels, this);
        binding.recyclerview.setAdapter(chatadapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerview.setLayoutManager(layoutManager);

        final String groupChatPath = "Group-Chat";
        final String usersPath = "Users";

        database.getReference().child(groupChatPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModels.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    MessageModel model = snapshot1.getValue(MessageModel.class);
                    messageModels.add(model);
                }
                chatadapter.notifyDataSetChanged();
                binding.recyclerview.scrollToPosition(messageModels.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupChat.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });

        binding.send.setOnClickListener(view -> {
            final String message = binding.EnterMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                database.getReference().child(usersPath).child(senderId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String senderName = dataSnapshot.getValue(String.class);
                        MessageModel model = new MessageModel(senderId, message, senderName, null, null);
                        model.setTimestamp(new Date().getTime());
                        binding.EnterMessage.setText("");

                        database.getReference().child(groupChatPath).push().setValue(model)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(GroupChat.this, "Message sent", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(GroupChat.this, "Failed to get sender name", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.pinButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            uploadFile(fileUri);
        }
    }

    private void uploadFile(Uri fileUri) {
        StorageReference storageRef = storage.getReference().child("files/" + System.currentTimeMillis() + "_" + fileUri.getLastPathSegment());

        storageRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String fileUrl = uri.toString();
                String fileName = fileUri.getLastPathSegment();

                MessageModel model = new MessageModel(mAuth.getUid(), null, null, fileUrl, fileName);
                model.setTimestamp(new Date().getTime());

                database.getReference().child("Group-Chat").push().setValue(model)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(GroupChat.this, "File sent", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(GroupChat.this, "File upload failed", Toast.LENGTH_SHORT).show();
        });
    }
}
