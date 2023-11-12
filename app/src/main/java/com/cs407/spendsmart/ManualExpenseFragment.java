package com.cs407.spendsmart;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ManualExpenseFragment extends Fragment {

    private Spinner spinnerInput;
    private EditText manualInputDate;
    private EditText manualExpenseAmount;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual_expense, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Back button functionality
        ImageView backButton = view.findViewById(R.id.manualExpenseBackButton);
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

        // Date Picker
        manualInputDate = view.findViewById(R.id.manualInputDate);
        manualInputDate.setFocusable(false); // Disable focus
        manualInputDate.setClickable(true); // Make it clickable

        // Initialize the date set listener
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Month is 0 based in Calendar
                month = month + 1;
                String date = dayOfMonth + "/" + month + "/" + year;
                manualInputDate.setText(date);
            }
        };

        // Set click listener on the date EditText
        manualInputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });


        // Setup Spinner
        spinnerInput = view.findViewById(R.id.manualCategoryInput);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.expense_categories, R.layout.spinner_item); // Use your custom layout here
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInput.setAdapter(adapter);


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
                // Another interface callback
            }
        });

        // Setup the amount input
        manualExpenseAmount = view.findViewById(R.id.manualExpenseAmount);

        // Setup the confirm button
        Button confirmButton = view.findViewById(R.id.manualExpenseConfirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpenseToDatabase();
            }
        });

        return view;
    }
    private void addExpenseToDatabase() {
        String category = spinnerInput.getSelectedItem().toString();
        String amountString = manualExpenseAmount.getText().toString();
        String date = manualInputDate.getText().toString();
        String email = getCurrentUserEmail(); // Implement this method to get the logged-in user's email

        // Validate the input
        if (category.isEmpty() || amountString.isEmpty() || date.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new transaction map
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("category", category);
        transaction.put("amount", amount);
        transaction.put("date", date);
        transaction.put("email", email);

        // Add a new document with a generated ID
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
                    // Reset the input fields
                    spinnerInput.setSelection(0);
                    manualExpenseAmount.setText("");
                    manualInputDate.setText("");
                    // Update user transactions with transaction id.
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    DocumentReference dr = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                    dr.update("transactions", FieldValue.arrayUnion(documentReference.getId()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding expense", Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
         return mAuth.getCurrentUser().getEmail();

    }
}
