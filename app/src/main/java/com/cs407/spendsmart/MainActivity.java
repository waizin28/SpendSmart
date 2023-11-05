package com.cs407.spendsmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private boolean passwordVis = false;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and go straight to homepage
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.d("HELP", ""+mAuth.getCurrentUser().getEmail());
            startActivity(new Intent(this, TestHomePage.class));
        }
    }

    public void onSignIn(View view) {
        EditText email = findViewById(R.id.emailTxt);
        EditText password = findViewById(R.id.passwordTxt);
        if(email.getText().toString().equals("")){
            findViewById(R.id.emailErrorTxt2).setVisibility(View.VISIBLE);
        }
        else if(password.getText().toString().equals("")) {
            findViewById(R.id.passwordErrorTxt2).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.emailErrorTxt2).setVisibility(View.INVISIBLE);
            findViewById(R.id.passwordErrorTxt2).setVisibility(View.INVISIBLE);
            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                startActivity(new Intent(MainActivity.this, TestHomePage.class));
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
    public void toSignUp(View view) { startActivity(new Intent(this, SignUpPage.class)); }

    // Navigate to ForgotPassword Page
    public void toForgotPassword(View view) { startActivity(new Intent(this, ForgotPwdPage.class)); }
}