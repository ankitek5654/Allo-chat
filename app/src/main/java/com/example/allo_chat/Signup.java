package com.example.allo_chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.allo_chat.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Objects;

import Models.Users;

public class Signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ActivitySignupBinding binding;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(Signup.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We're Creating Your Account");

        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(binding.username.getText().toString().isEmpty() || binding.email.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty())) {
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Users user = new Users(binding.username.getText().toString(), binding.email.getText().toString(), binding.password.getText().toString());
                                String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                                database.getReference().child("Users").child(id).setValue(user);
                                Toast.makeText(Signup.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Signup.this, SignIn.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(Signup.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(Signup.this, "Enter Credentials", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        binding.alreadyhaveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup.this, SignIn.class);
                startActivity(intent);
            }
        });
    }
}
