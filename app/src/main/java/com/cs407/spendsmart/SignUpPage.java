package com.cs407.spendsmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpPage extends AppCompatActivity {

    private boolean passwordVis = false;
    private boolean confirmPassVis = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
    }

    public void clickSignUp(View view) {
        boolean formFilled = true;
        EditText name = findViewById(R.id.nameInput);
        EditText username = findViewById(R.id.usernameInput);
        EditText email = findViewById(R.id.emailInput);
        EditText password = findViewById(R.id.passwordInput);
        EditText confirm = findViewById(R.id.confirmPassInput);

        if(name.getText().toString().equals("") || !name.getText().toString().contains(" ")){
            formFilled = false;
            TextView errorTxt= findViewById(R.id.nameErrorTxt);
            errorTxt.setText("Both First and Last Name Required");
            errorTxt.setVisibility(View.VISIBLE);
        } else{
            findViewById(R.id.nameErrorTxt).setVisibility(View.INVISIBLE);
        }
        if(username.getText().toString().equals("")){
            formFilled = false;
            TextView errorTxt= findViewById(R.id.usernameErrorTxt);
            errorTxt.setText("Username Required");
            errorTxt.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.usernameErrorTxt).setVisibility(View.INVISIBLE);
        }
        if(email.getText().toString().equals("")){
            formFilled = false;
            TextView errorTxt= findViewById(R.id.emailErrorTxt);
            errorTxt.setText("Email Required");
            errorTxt.setVisibility(View.VISIBLE);
        }
        else if(!email.getText().toString().contains("@")){
            formFilled = false;
            TextView errorTxt= findViewById(R.id.emailErrorTxt);
            errorTxt.setText("Invalid Email");
            errorTxt.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.emailErrorTxt).setVisibility(View.INVISIBLE);
        }
        if(password.getText().toString().equals("")){
            formFilled = false;
            TextView errorTxt= findViewById(R.id.passwordErrorTxt);
            errorTxt.setText("Password Required");
            errorTxt.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.passwordErrorTxt).setVisibility(View.INVISIBLE);
        }
        if(!password.getText().toString().equals(confirm.getText().toString())){
            formFilled = false;
            TextView errorTxt= findViewById(R.id.confirmErrorTxt);
            errorTxt.setText("Passwords do not match");
            errorTxt.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.confirmErrorTxt).setVisibility(View.INVISIBLE);
        }

        if(formFilled){
            enterUser();
        }
    }

    public void enterUser() {
        Toast.makeText(this, "Sign Up Success", Toast.LENGTH_SHORT).show();
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