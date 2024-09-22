package com.example.allo_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.allo_chat.Adapters.Chatadapter;
import com.example.allo_chat.databinding.ActivityChatdetailsBinding;
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

    final int PICK_FILE_REQUEST_CODE = 1;
    String senderRoom, receiverRoom;

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

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

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
                binding.recyclerview.scrollToPosition(list.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        binding.send.setOnClickListener(view -> {
            String msg = binding.EnterMessage.getText().toString().trim();

            if (!msg.isEmpty()) {
                final MessageModel model = new MessageModel(senderId, msg, null, null, null);
                model.setTimestamp(new Date().getTime());
                sendMessage(senderRoom, receiverRoom, model);
                binding.EnterMessage.setText("");
            } else {
                Toast.makeText(Chatdetails.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        binding.pinButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
        });
    }

    private void sendMessage(String senderRoom, String receiverRoom, MessageModel model) {
        database.getReference().child("chats").child(senderRoom).push().setValue(model)
                .addOnSuccessListener(unused -> {
                    database.getReference().child("chats").child(receiverRoom).push().setValue(model);
                    Toast.makeText(Chatdetails.this, "Message sent", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            uploadFile(selectedFileUri);
        }
    }

    private void uploadFile(Uri fileUri) {
        StorageReference storageRef = storage.getReference().child("files/" + System.currentTimeMillis() + "_" + fileUri.getLastPathSegment());

        storageRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String fileUrl = uri.toString();
                String fileName = fileUri.getLastPathSegment();

                // Create a MessageModel with the file URL
                MessageModel model = new MessageModel(mAuth.getUid(), null, null, fileUrl, fileName);
                model.setTimestamp(new Date().getTime());

                // Send the message with the file URL
                sendMessage(senderRoom, receiverRoom, model);
                Toast.makeText(Chatdetails.this, "File sent", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(Chatdetails.this, "File upload failed", Toast.LENGTH_SHORT).show();
        });
    }
}
