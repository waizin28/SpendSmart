package com.cs407.spendsmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

public class SignUpPage extends AppCompatActivity {

    private boolean passwordVis = false;
    private boolean confirmPassVis = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
    }

    // Password Visibility2:
    public void passwordVisibility2(View view) {
        EditText passWordField = findViewById(R.id.passwordInput);
        if(passwordVis) {
            passWordField.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.eye_icon, 0);
            passWordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordVis = false;
        }
        else {
            passWordField.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.not_visible_icon, 0);
            passWordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordVis = true;
        }
    }

    // Password Visibility3:
    public void passwordVisibility3(View view) {
        EditText passWordField = findViewById(R.id.confirmPassInput);
        if(confirmPassVis) {
            passWordField.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.eye_icon, 0);
            passWordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPassVis = false;
        }
        else {
            passWordField.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.not_visible_icon, 0);
            passWordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPassVis = true;
        }
    }

    public void backToSignIn(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}