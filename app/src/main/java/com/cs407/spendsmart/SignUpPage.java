package com.cs407.spendsmart;

import static android.Manifest.permission.READ_MEDIA_IMAGES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import io.getstream.avatarview.AvatarView;

public class SignUpPage extends AppCompatActivity {

    private boolean passwordVis = false;
    private boolean confirmPassVis = false;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    ActivityResultLauncher<Intent> imageLauncher;
    Map<String,Object> user = new HashMap<>();
    EditText email;
    boolean picSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        AvatarView avatarView = findViewById(R.id.avatarView);
        avatarView.setAvatarInitialsBackgroundColor(ContextCompat.getColor(this, R.color.dark_green));
        avatarView.setAvatarInitials(" ");

        email = findViewById(R.id.emailInput);
        picSelected = false;

        // Initializes activity for choosing profile picture.
        imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // On Choosing Picture:
                    if(result.getResultCode() == RESULT_OK) {
                        picSelected = true;
                        assert result.getData() != null;
                        Uri imageUri = result.getData().getData();
                        avatarView.setAvatarInitials(null);
                        avatarView.setImageURI(imageUri);

                        // TODO: Store profile pic in cloud storage and reference from database?
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = storageRef.child("profile_pics/"+email.getText().toString());
                        avatarView.setDrawingCacheEnabled(true);
                        avatarView.buildDrawingCache();
                        Bitmap bitmap = ((BitmapDrawable) avatarView.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        // Start upload task
                        UploadTask uploadTask = imageRef.putBytes(data);
                        uploadTask.addOnProgressListener(snapshot -> {
                            ProgressBar bar = findViewById(R.id.picProgress);
                            bar.setVisibility(View.VISIBLE);
                            bar.setProgress((int)(((float)snapshot.getBytesTransferred()/data.length)*100));
                        });
                        uploadTask.addOnSuccessListener(taskSnapshot -> findViewById(R.id.picProgress).setVisibility(View.INVISIBLE));
                    }
                }
        );
    }

    public void clickSignUp(View view) {
        boolean formFilled = true;
        EditText name = findViewById(R.id.nameInput);
        EditText username = findViewById(R.id.usernameInput);
        EditText password = findViewById(R.id.passwordInput);
        EditText confirm = findViewById(R.id.confirmPassInput);

        if(name.getText().toString().equals("") || !name.getText().toString().contains(" ")){
            formFilled = false;
            findViewById(R.id.nameErrorTxt).setVisibility(View.VISIBLE);
        }
        else { findViewById(R.id.nameErrorTxt).setVisibility(View.INVISIBLE); }
        if(username.getText().toString().equals("")){
            formFilled = false;
            findViewById(R.id.usernameErrorTxt).setVisibility(View.VISIBLE);
        }
        else { findViewById(R.id.usernameErrorTxt).setVisibility(View.INVISIBLE); }
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
        }
        else { findViewById(R.id.emailErrorTxt).setVisibility(View.INVISIBLE); }
        if(password.getText().toString().equals("")){
            formFilled = false;
            findViewById(R.id.passwordErrorTxt).setVisibility(View.VISIBLE);
        }
        else { findViewById(R.id.passwordErrorTxt).setVisibility(View.INVISIBLE); }
        if(!password.getText().toString().equals(confirm.getText().toString())){
            formFilled = false;
            findViewById(R.id.confirmErrorTxt).setVisibility(View.VISIBLE);
        }
        else { findViewById(R.id.confirmErrorTxt).setVisibility(View.INVISIBLE); }

        // Store user info in database:
        if(formFilled){
            user.put("name", name.getText().toString());
            user.put("username", username.getText().toString());
            user.put("transactions", new HashMap<String,Object>());
            if(picSelected){
                user.put("profile_pic","profile_pics/"+email.getText().toString());
            }
            else {
                user.put("profile_pic",null);
            }
            // Firebase Authentication - create new user with email and password.
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if(task.isSuccessful()) {
                            mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build());
                            database.collection("users").document(email.getText().toString()).set(user).addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Information Saved", Toast.LENGTH_SHORT).show());
                            Intent intent = new Intent(SignUpPage.this, LogInActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(SignUpPage.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void clickProfilePic(View view) {
        if(email.getText().toString().equals("")){
            TextView errorTxt= findViewById(R.id.emailErrorTxt);
            errorTxt.setText("Email Required");
            errorTxt.setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.emailErrorTxt).setVisibility(View.INVISIBLE);
            if(Build.VERSION.SDK_INT > 23) {
                if(ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{READ_MEDIA_IMAGES}, 1);
                }
                else { selectPic(); }
            }
            else { selectPic(); }
        }

    }
    public void selectPic() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageLauncher.launch(intent);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPic();
        }
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
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }
}