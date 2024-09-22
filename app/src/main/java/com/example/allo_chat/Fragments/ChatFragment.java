package com.example.allo_chat.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.allo_chat.Adapters.UsersAdapter;
import com.example.allo_chat.R;
import com.example.allo_chat.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Models.Users;

public class ChatFragment extends Fragment {

    public ChatFragment() {
    }

    FragmentChatBinding binding;
    ArrayList <Users> list=new ArrayList<>();
    FirebaseDatabase firebaseDatabase;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding=FragmentChatBinding.inflate(inflater,container,false);
        firebaseDatabase=FirebaseDatabase.getInstance();
        UsersAdapter usersAdapter=new UsersAdapter(list,getContext());
        binding.recyclerview.setAdapter(usersAdapter);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        binding.recyclerview.setLayoutManager(layoutManager);

        firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Users users=dataSnapshot.getValue(Users.class);
                    users.setUserId(dataSnapshot.getKey());
                    if(!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                        list.add(users);
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        return binding.getRoot();
    }
}