package com.example.allo_chat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allo_chat.Chatdetails;
import com.example.allo_chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Models.Users;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{


    ArrayList list;
    Context context;

    public UsersAdapter(ArrayList list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     View view= LayoutInflater.from(context).inflate(R.layout.sample_show_user,parent,false);
     return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user= (Users) list.get(position);
        Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.avatar3).into(holder.image);
        holder.username.setText(user.getUsername());

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        database.getReference().child("chats").child(FirebaseAuth.getInstance().getUid()+ user.getUserId()).orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        holder.lastmessage.setText(snapshot1.child("message").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, Chatdetails.class);
                intent.putExtra("UserId",user.getUserId());
                intent.putExtra("Profile_pic",user.getProfilepic());
                intent.putExtra("Username",user.getUsername());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView username,lastmessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        image=itemView.findViewById(R.id.profile_image);
        username=itemView.findViewById(R.id.username_list);
        lastmessage=itemView.findViewById(R.id.last_message);


        }
    }
}
