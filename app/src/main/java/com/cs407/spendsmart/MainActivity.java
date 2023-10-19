package com.cs407.spendsmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private boolean passwordVis = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Password Visibility:
    public void passwordVisibility(View view) {
        EditText passWordField = findViewById(R.id.passwordTxt);
        if(passwordVis) {
            passWordField.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.lock_icon,0,R.drawable.eye_icon, 0);
            passWordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordVis = false;
        }
        else {
            passWordField.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.lock_icon,0,R.drawable.not_visible_icon, 0);
            passWordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordVis = true;
        }
    }

    // Navigate to SignUp Page
    public void toSignUp(View view) {
        Intent intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    // TODO: Navigate to ForgotPassword Page
    public void toForgotPassword(View view) {

    }
}