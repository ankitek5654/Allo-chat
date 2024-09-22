package com.example.allo_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.allo_chat.Adapters.Chatadapter;
import com.example.allo_chat.databinding.ActivityChatdetailsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import Models.MessageModel;

public class Chatdetails extends AppCompatActivity {

    ActivityChatdetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    final int PICK_FILE = 25;
    String fileUrl = null;
    String fileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatdetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        final String senderId = mAuth.getUid();
        final String receiverId = getIntent().getStringExtra("UserId");
        String username = getIntent().getStringExtra("Username");
        String profile_pic = getIntent().getStringExtra("Profile_pic");

        binding.username.setText(username);
        Picasso.get().load(profile_pic).placeholder(R.drawable.avatar3).into(binding.profileImage);

        binding.back.setOnClickListener(view -> {
            Intent intent = new Intent(Chatdetails.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        final ArrayList<MessageModel> list = new ArrayList<>();
        final Chatadapter chatadapter = new Chatadapter(list, this, receiverId);

        binding.recyclerview.setAdapter(chatadapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerview.setLayoutManager(layoutManager);

        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        database.getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    MessageModel model = snapshot1.getValue(MessageModel.class);
                    model.setMessageId(snapshot1.getKey());
                    list.add(model);
                }
                chatadapter.notifyDataSetChanged();
                binding.recyclerview.scrollToPosition(list.size() - 1); // Scroll to the last message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        binding.send.setOnClickListener(view -> {
            String msg = binding.EnterMessage.getText().toString().trim();

            if (msg.isEmpty() && fileUrl == null) {
                Toast.makeText(Chatdetails.this, "Please enter a message or select a file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new message model
            final MessageModel model;
            if (fileUrl != null) {
                model = new MessageModel(senderId, "File: " + fileName, fileUrl);
            } else {
                model = new MessageModel(senderId, msg, null);
            }
            model.setTimestamp(new Date().getTime());

            // Send the message
            sendMessage(senderRoom, receiverRoom, model);

            // Clear input fields
            binding.EnterMessage.setText("");
            fileUrl = null;  // Reset the file URL after sending the message
            fileName = null;  // Reset the file name
        });

        binding.pinButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE);
        });
    }

    private void sendMessage(String senderRoom, String receiverRoom, MessageModel model) {
        database.getReference().child("chats").child(senderRoom).push().setValue(model)
                .addOnSuccessListener(unused -> {
                    database.getReference().child("chats").child(receiverRoom).push().setValue(model);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                uploadFile(selectedFileUri);
            } else {
                Toast.makeText(this, "File selection failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFile(Uri selectedFileUri) {
        final StorageReference reference = storage.getReference().child("chats/files")
                .child(System.currentTimeMillis() + "_" + selectedFileUri.getLastPathSegment());

        reference.putFile(selectedFileUri).addOnSuccessListener(taskSnapshot -> {
            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                fileUrl = uri.toString(); // Set fileUrl to be used in the send button
                fileName = selectedFileUri.getLastPathSegment(); // Get file name
                Toast.makeText(Chatdetails.this, "File selected. Press send to upload", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
