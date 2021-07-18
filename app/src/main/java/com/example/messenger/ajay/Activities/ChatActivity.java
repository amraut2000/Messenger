package com.example.messenger.ajay.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.messenger.ajay.Adapters.MessageAdapter;
import com.example.messenger.ajay.Models.Message;
import com.example.messenger.ajay.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.SnapshotHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    MessageAdapter messageAdapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom, senderId, receiverId;

    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        receiverId = getIntent().getStringExtra("uid");
        senderId = FirebaseAuth.getInstance().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, this, senderRoom, receiverRoom);
        binding.chatRecyclerView.setAdapter(messageAdapter);

        binding.msgSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        loadMessages();


    }

    private void loadMessages() {
        firebaseDatabase.getReference().child("Chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage() {
        String messageTxt = binding.messageEditText.getText().toString();
        binding.messageEditText.setText("");
        Date date = new Date();
        Message message = new Message(messageTxt, senderId, date.getTime());

        String randomKey = firebaseDatabase.getReference().push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastMsg", message.getMessage());
        hashMap.put("lastMsgTime", date.getTime());

        firebaseDatabase.getReference().child("Chats").child(senderRoom).updateChildren(hashMap);
        firebaseDatabase.getReference().child("Chats").child(receiverRoom).updateChildren(hashMap);


        firebaseDatabase.getReference().child("Chats")
                .child(senderRoom)
                .child("messages")
                .child(randomKey)
                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                firebaseDatabase.getReference().child("Chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("lastMsg", message.getMessage());
                hashMap.put("lastMsgTime", date.getTime());

                firebaseDatabase.getReference().child("Chats").child(senderRoom).updateChildren(hashMap);
                firebaseDatabase.getReference().child("Chats").child(receiverRoom).updateChildren(hashMap);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}