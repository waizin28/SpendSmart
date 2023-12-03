package com.cs407.spendsmart;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

import io.github.muddz.styleabletoast.StyleableToast;

public class ScanExpenseFragment extends Fragment {

    private FirebaseFirestore db;
    private MaterialButton inputImageBtn;
    private MaterialButton recognizeTextBtn;
    private ShapeableImageView imageIv;
    private TextView dateLabel, totalCostLabel;
    private EditText dateInput, totalCostInput;

    private Button confirmBtn;
    private AddingDialog addingDialog;

    private Spinner spinnerInput;

    private Dialog receiptPrompt;

    private ReceiptInfo receiptInfo;

    private static final String TAG = "MAIN_TAG";
    private Uri imageUri = null;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;

    private TextRecognizer textRecognizer;

    private LoadingDialog loadingDialog;
    public ScanExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_expense, container, false);

        inputImageBtn = view.findViewById(R.id.inputImageBtn);
        imageIv = view.findViewById(R.id.imageIv);
        dateInput = view.findViewById(R.id.dateInput);
        totalCostInput = view.findViewById(R.id.totalCostInput);

        dateLabel = view.findViewById(R.id.dateLabel);
        totalCostLabel = view.findViewById(R.id.totalCostLabel);
        dateLabel.setVisibility(View.GONE);
        totalCostLabel.setVisibility(View.GONE);

//        receiptPrompt = new Dialog(getContext());

        ImageView backButton = view.findViewById(R.id.scanExpenseBackButton);

        db = FirebaseFirestore.getInstance();

        confirmBtn = view.findViewById(R.id.confirmBtn);
        loadingDialog = new LoadingDialog(getActivity());
        addingDialog = new AddingDialog(getActivity());

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        progressDialog = new ProgressDialog(getContext());
//        progressDialog.setTitle("Please wait");
//        progressDialog.setCanceledOnTouchOutside(false);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of ExpenseFragment
                ExpenseFragment expenseFragment = new ExpenseFragment();

                // Perform the fragment replacement
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, expenseFragment); // Use the correct container ID
                transaction.commit();
            }
        });
        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageInputDialog();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReceiptPrompt();
            }
        });

        return view;
    }

    public static ReceiptInfo parseReceipt(String receiptText) {
        ReceiptInfo receiptInfo = new ReceiptInfo();

        // Regular expression patterns for date and total cost
        Pattern datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b");
        Pattern totalCostPattern = Pattern.compile("\\b(\\$?\\d+\\.\\d{2})\\b");

        Matcher dateMatcher = datePattern.matcher(receiptText);
        if (dateMatcher.find()) {
            receiptInfo.setDate(dateMatcher.group(1));
        }

        // Find the last occurrence of total purchase amount
        Matcher totalPurchaseAmountMatcher = totalCostPattern.matcher(receiptText);
        double lastTotalPurchaseAmount = 0.0;

        while (totalPurchaseAmountMatcher.find()) {
            lastTotalPurchaseAmount = Double.parseDouble(totalPurchaseAmountMatcher.group());
        }

        receiptInfo.setTotalCost(lastTotalPurchaseAmount);

        return receiptInfo;
    }

    static class ReceiptInfo {
        private String date;
        private double totalCost;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date.replace("-", "/");;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }
    }

    private void recognizeTextFromImage() {
//        progressDialog.setMessage("Preparing image...");
//        progressDialog.show();
        loadingDialog.startLoadingDialog();

        try {
            InputImage inputImage = InputImage.fromFilePath(getContext(),imageUri);
//            progressDialog.setMessage("Recognizing text...");
            Task<Text> text = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
//                    progressDialog.dismiss();
                    loadingDialog.dismissDialog();
                    String recognizedText = text.getText();
                    receiptInfo = parseReceipt(recognizedText);
                    dateLabel.setVisibility(View.VISIBLE);
                    totalCostLabel.setVisibility(View.VISIBLE);
                    dateInput.setText(receiptInfo.getDate());
                    totalCostInput.setText("$" + String.format("%.2f",receiptInfo.getTotalCost()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(),"Failed recognizing text due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(getContext(),"Failed preparing image due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageInputDialog() {
        PopupMenu popupMenu = new PopupMenu(getContext(),inputImageBtn);
        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == 1){
                    if(checkCameraPermissions()){
                        pickImageCamera();
                    }else{
                        requestCameraPermission();
                    }
                }else if(id == 2){
                    if(checkStoragePermission()){
                        pickImageGallery();
                    }else{
                        requestStoragePermission();
                    }
                }

                return true;
            }
        });
    }

    private String getCurrentUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }

    private void openReceiptPrompt(){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View mview = getLayoutInflater().inflate(R.layout.receipt_prompt_layout,null);
//        spinnerInput = mview.findViewById(R.id.categoryInput);


//        receiptPrompt.setContentView(R.layout.receipt_prompt_layout);
//        receiptPrompt.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView imageViewClose = mview.findViewById(R.id.close);
        Button confirm = mview.findViewById(R.id.submitBtn);

        // Setup spinner
        spinnerInput = mview.findViewById(R.id.categoryInput);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.expense_categories, R.layout.scan_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInput.setAdapter(adapter);

        mBuilder.setView(mview);
        mBuilder.setCancelable(false);
        AlertDialog alertDialog = mBuilder.create();

        spinnerInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Do something with the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            }
        });

        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                addingDialog.dismissDialog();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save chosen category to database
                addingDialog.startLoadingDialog();

                if (receiptInfo == null) {
                    alertDialog.dismiss();
                    addingDialog.dismissDialog();
                    StyleableToast.makeText(getActivity()," Please scan the image again  ",R.style.error).show();
                    return;
                }

                String category = spinnerInput.getSelectedItem().toString();
                String date = receiptInfo.getDate();
                String email = getCurrentUserEmail(); // Implement this method to get the logged-in user's email

                if (date.isEmpty() || email.isEmpty()) {
                    alertDialog.dismiss();
                    Toast.makeText(getContext(), "Please scan the image again", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> transaction = new HashMap<>();
                transaction.put("category", category);
                transaction.put("amount", receiptInfo.getTotalCost());
                transaction.put("date", date);
                transaction.put("email", email);

                db.collection("transactions")
                        .add(transaction)
                        .addOnSuccessListener(documentReference -> {
                            addingDialog.dismissDialog();
                            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();

                            // Reset all the variables
                            spinnerInput.setSelection(0);
                            receiptInfo.setTotalCost(0);
                            receiptInfo.setDate("");

                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            DocumentReference dr = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                            dr.update("transactions", FieldValue.arrayUnion(documentReference.getId()));
                        })
                        .addOnFailureListener(e -> {
                            addingDialog.dismissDialog();
                            Toast.makeText(getContext(), "Error adding expense", Toast.LENGTH_SHORT).show();
                        });

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
//        receiptPrompt.show();
    }

    private void pickImageGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleyActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleyActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    // we will recevied image if picked
                    if(result.getResultCode() == RESULT_OK){
                        // image picked
                        imageUri = result.getData().getData();
                        // set to image view
                        imageIv.setImageURI(imageUri);
                        recognizeTextFromImage();
                    }else{
                        Toast.makeText(getContext(), "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Description");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // receive image if taken from camera
                    if(result.getResultCode() == RESULT_OK){
                        imageIv.setImageURI(imageUri);
                        recognizeTextFromImage();
                    }else{
                        Toast.makeText(getContext(),"Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(),storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean cameraResult = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return cameraResult && storageResult;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(),cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        pickImageCamera();
                    }else{
                        Toast.makeText(getContext(),"Camera and Storage Permission Required",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),"Cancelled",Toast.LENGTH_SHORT).show();

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        // launch gallery intent
                        pickImageGallery();
                    }else{
                        Toast.makeText(getContext(),"Storage permission is required",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

}