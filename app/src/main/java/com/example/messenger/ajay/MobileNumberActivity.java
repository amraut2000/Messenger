package com.example.messenger.ajay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.messenger.ajay.databinding.ActivityMobileNumberBinding;

public class MobileNumberActivity extends AppCompatActivity {

    ActivityMobileNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMobileNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.mobileNumberEditText.requestFocus();

        binding.proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceed();
            }
        });

    }

    private void proceed(){
        String number=binding.mobileNumberEditText.getText().toString().trim();
        if(number.isEmpty()){
            binding.mobileNumberEditText.setError("Mobile number is required");
            binding.mobileNumberEditText.requestFocus();
            return;
        }
        Intent intent=new Intent(MobileNumberActivity.this,OTP_Activity.class);
        intent.putExtra("number",number);
        startActivity(intent);
    }
}