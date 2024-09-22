package com.example.allo_chat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allo_chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Models.MessageModel;

public class Chatadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<MessageModel> list;
    Context context;
    int sender_view_type = 1, receiver_view_type = 2;

    public Chatadapter(ArrayList<MessageModel> messageModels, Context context ,String receiverId) {
        this.list = messageModels;
        this.context = context;
    }

    public Chatadapter(ArrayList<MessageModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == sender_view_type) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = list.get(holder.getAdapterPosition());

        holder.itemView.setOnLongClickListener(view -> {
            if (messageModel.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Do you really want to delete this message?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseDatabase.getInstance().getReference().child("chats")
                                    .child(messageModel.getMessageId())
                                    .setValue(null);

                            list.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
            return true;
        });

        if (messageModel.getFileUrl() != null && !messageModel.getFileUrl().isEmpty()) {
            String fileName = messageModel.getFileName();
            SpannableString spannableString = new SpannableString("File: " + fileName);
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageModel.getFileUrl()));
                    context.startActivity(browserIntent);
                }
            }, 0, spannableString.length(), 0);

            if (holder instanceof SenderViewHolder) {
                ((SenderViewHolder) holder).sendermsg.setText(spannableString);
                ((SenderViewHolder) holder).sendermsg.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                ((ReceiverViewHolder) holder).receivermsg.setText(spannableString);
                ((ReceiverViewHolder) holder).receivermsg.setMovementMethod(LinkMovementMethod.getInstance());
            }

            Date date = new Date(messageModel.getTimestamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm a");
            String strDate = simpleDateFormat.format(date);
            if (holder instanceof SenderViewHolder) {
                ((SenderViewHolder) holder).sendertime.setText(strDate);
            } else {
                ((ReceiverViewHolder) holder).receivertime.setText(strDate);
            }
        } else {
            String messageText = messageModel.getMessage();
            if (holder instanceof SenderViewHolder) {
                ((SenderViewHolder) holder).sendermsg.setText(messageText);
                ((SenderViewHolder) holder).sendertime.setText(new SimpleDateFormat("H:mm a").format(new Date(messageModel.getTimestamp())));
            } else {
                ((ReceiverViewHolder) holder).receivermsg.setText(messageText);
                ((ReceiverViewHolder) holder).receivertime.setText(new SimpleDateFormat("H:mm a").format(new Date(messageModel.getTimestamp())));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return sender_view_type;
        } else {
            return receiver_view_type;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView sendermsg, sendertime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sendermsg = itemView.findViewById(R.id.sender_text);
            sendertime = itemView.findViewById(R.id.sender_time);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receivermsg, receivertime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receivermsg = itemView.findViewById(R.id.receiver_txt);
            receivertime = itemView.findViewById(R.id.receiver_time);
        }
    }
}
