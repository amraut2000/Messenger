package com.example.messenger.ajay.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.messenger.ajay.databinding.ActivityMobileNumberBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class MobileNumberActivity extends AppCompatActivity {

    ActivityMobileNumberBinding binding;

    FirebaseAuth auth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMobileNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(MobileNumberActivity.this, MainActivity.class));
            finish();
        }

        getSupportActionBar().hide();

        binding.mobileNumberEditText.requestFocus();

        binding.proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceed();
            }
        });

    }

    private void proceed() {
        String number = binding.mobileNumberEditText.getText().toString().trim();
        if (number.isEmpty()) {
            binding.mobileNumberEditText.setError("Mobile number is required");
            binding.mobileNumberEditText.requestFocus();
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + number.toString(),
                60,
                TimeUnit.SECONDS,
                MobileNumberActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                        progressDialog.hide();
                        Toast.makeText(MobileNumberActivity.this, "Failed to send OTP,Try again", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull @NotNull String verificationId, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        progressDialog.hide();
                        Intent intent = new Intent(MobileNumberActivity.this, OTP_Activity.class);
                        intent.putExtra("number", number);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                    }
                }
        );

    }
}