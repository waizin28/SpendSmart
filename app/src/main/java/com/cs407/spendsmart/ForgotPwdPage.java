package com.cs407.spendsmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

public class ForgotPwdPage extends AppCompatActivity {

    private TextInputLayout textInputUsername, textInputEmail;
    private Button recover_btn;

    private TextView redirectSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pwd_page);

        textInputUsername = findViewById(R.id.forgot_userInput);
        textInputEmail = findViewById(R.id.forgot_emailInput);
        redirectSignIn = findViewById(R.id.redirect_signIn);

        recover_btn = findViewById(R.id.recoverPwdBtn);
        recover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmInput(view);
            }
        });

        redirectSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateEmail(){
        String email = textInputEmail.getEditText().getText().toString().trim();
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

    private boolean validateUserName(){
        String username  = textInputUsername.getEditText().getText().toString().trim();
        if(username.isEmpty()){
            textInputUsername.setError("Field can't be empty");
            return false;
        }else{
            textInputUsername.setErrorEnabled(false);
        }
        return true;
    }
    public void confirmInput(View v){

        if(!!validateUserName()){
            return;
        }

        if(!validateEmail()){
            return;
        }


    }
}