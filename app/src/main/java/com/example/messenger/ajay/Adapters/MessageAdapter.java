package com.example.messenger.ajay.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.ajay.Models.Message;
import com.example.messenger.ajay.R;
import com.example.messenger.ajay.databinding.MsgReceiveBinding;
import com.example.messenger.ajay.databinding.MsgSendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    ArrayList<Message> messages;
    Context context;

    public static final int MSG_SENT = 1;
    public static final int MSG_RECEIVE = 2;

    String senderRoom, receiverRoom;

    public MessageAdapter(ArrayList<Message> messages, Context context, String senderRoom, String receiverRoom) {
        this.messages = messages;
        this.context = context;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.msg_send, parent, false);
            return new SendMessageViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.msg_receive, parent, false);
        return new ReceiveMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int[] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass() == SendMessageViewHolder.class) {
                SendMessageViewHolder sendMessageViewHolder = (SendMessageViewHolder) holder;
                sendMessageViewHolder.binding.feeling.setImageResource(reactions[pos]);
                sendMessageViewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ReceiveMessageViewHolder receiveMessageViewHolder = (ReceiveMessageViewHolder) holder;
                receiveMessageViewHolder.binding.feeling.setImageResource(reactions[pos]);
                receiveMessageViewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == SendMessageViewHolder.class) {
            SendMessageViewHolder sendMessageViewHolder = (SendMessageViewHolder) holder;
            sendMessageViewHolder.binding.message.setText(message.getMessage());

            if (message.getFeeling() >= 0) {
                sendMessageViewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                sendMessageViewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                sendMessageViewHolder.binding.feeling.setVisibility(View.GONE);
            }

            sendMessageViewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        } else {
            ReceiveMessageViewHolder receiveMessageViewHolder = (ReceiveMessageViewHolder) holder;
            receiveMessageViewHolder.binding.message.setText(message.getMessage());
            if (message.getFeeling() >= 0) {
                receiveMessageViewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                receiveMessageViewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                receiveMessageViewHolder.binding.feeling.setVisibility(View.GONE);
            }

            receiveMessageViewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return MSG_SENT;
        }
        return MSG_RECEIVE;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendMessageViewHolder extends RecyclerView.ViewHolder {

        MsgSendBinding binding;

        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MsgSendBinding.bind(itemView);
        }
    }

    public class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {

        MsgReceiveBinding binding;

        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MsgReceiveBinding.bind(itemView);
        }
    }
}
