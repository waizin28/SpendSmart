package com.cs407.spendsmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPwdActivity extends AppCompatActivity {

    private TextInputLayout textInputEmail;
    private Button recover_btn;
    FirebaseAuth mAuth;
    private TextView redirectSignIn;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pwd);

        textInputEmail = findViewById(R.id.forgot_emailInput);
        redirectSignIn = findViewById(R.id.redirect_signIn);
        mAuth = FirebaseAuth.getInstance();

        recover_btn = findViewById(R.id.recoverPwdBtn);
        recover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = textInputEmail.getEditText().getText().toString().trim();
                if(validateEmail()){
                    ResetPassword();
                }
            }
        });

        redirectSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LogInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ResetPassword() {
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ForgotPwdActivity.this, "Reset Password email sent", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPwdActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage = "Failed to reset password. " + e.getMessage();
                Toast.makeText(ForgotPwdActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateEmail(){
        email = textInputEmail.getEditText().getText().toString().trim();
        if(email.isEmpty()){
            textInputEmail.setError("Field can't be empty");
            return false;
        }else if(!email.contains("@")){
            textInputEmail.setError("Invalid email");
            return false;
        }else{
            textInputEmail.setErrorEnabled(false);
        }
        return true;
    }
}