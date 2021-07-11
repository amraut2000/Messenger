package com.example.messenger.ajay.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.ajay.Models.Message;
import com.example.messenger.ajay.R;
import com.example.messenger.ajay.databinding.MsgReceiveBinding;
import com.example.messenger.ajay.databinding.MsgSendBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    ArrayList<Message> messages;
    Context context;

    public static final int MSG_SENT=1;
    public static final int MSG_RECEIVE=2;

    public MessageAdapter(ArrayList<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_SENT){
            View view= LayoutInflater.from(context).inflate(R.layout.msg_send,parent,false);
            return new SendMessageViewHolder(view);
        }
        View view=LayoutInflater.from(context).inflate(R.layout.msg_receive,parent,false);
        return new ReceiveMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message=messages.get(position);
        if(holder.getClass()==SendMessageViewHolder.class){
            SendMessageViewHolder sendMessageViewHolder=(SendMessageViewHolder) holder;
            sendMessageViewHolder.binding.message.setText(message.getMessage());
        }
        else{
            ReceiveMessageViewHolder receiveMessageViewHolder=(ReceiveMessageViewHolder) holder;
            receiveMessageViewHolder.binding.message.setText(message.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message=messages.get(position);
        if(FirebaseAuth.getInstance().getUid()==message.getSenderId()){
            return MSG_SENT;
        }
        return MSG_RECEIVE;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendMessageViewHolder extends RecyclerView.ViewHolder{

        MsgSendBinding binding;
        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=MsgSendBinding.bind(itemView);
        }
    }

    public class ReceiveMessageViewHolder extends RecyclerView.ViewHolder{

        MsgReceiveBinding binding;
        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=MsgReceiveBinding.bind(itemView);
        }
    }
}
