package com.example.allo_chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.allo_chat.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import Models.Users;

public class SignIn extends AppCompatActivity {

    private static final int REQ_ONE_TAP = 2;
    private ActivitySignInBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Please wait, validation in progress");

        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_ID))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        oneTapClient = Identity.getSignInClient(this);

        binding.signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.email.getText().toString().isEmpty() && !binding.password.getText().toString().isEmpty()) {
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        navigateToMain();
                                    } else {
                                        Toast.makeText(SignIn.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(SignIn.this, "Enter credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
        }

        binding.clicktosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignIn.this, Signup.class));
            }
        });

        binding.forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignIn.this, ForgotPassword.class));
            }
        });

        binding.google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnCompleteListener(this, new OnCompleteListener<BeginSignInResult>() {
                    @Override
                    public void onComplete(@NonNull Task<BeginSignInResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                startIntentSenderForResult(
                                        task.getResult().getPendingIntent().getIntentSender(),
                                        REQ_ONE_TAP,
                                        null,
                                        0,
                                        0,
                                        0);
                            } catch (IntentSender.SendIntentException e) {
                                Toast.makeText(SignIn.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignIn.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                if (data != null) {
                    SignInCredential googleCredential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = googleCredential.getGoogleIdToken();
                    if (idToken != null) {
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                Users users = new Users();
                                                users.setUserId(user.getUid());
                                                users.setUsername(user.getDisplayName());
                                                users.setProfilepic(Objects.requireNonNull(user.getPhotoUrl()).toString());
                                                firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(users);

                                                // Show toast message after successful sign-in and information saving
                                                Toast.makeText(SignIn.this, "Signed in with Google", Toast.LENGTH_SHORT).show();
                                            }
                                            navigateToMain();
                                        } else {
                                            Toast.makeText(SignIn.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            } catch (ApiException e) {
                Toast.makeText(SignIn.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignIn.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
