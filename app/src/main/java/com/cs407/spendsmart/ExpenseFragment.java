package com.cs407.spendsmart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ExpenseFragment extends Fragment {

    private Button addManualButton;
    private Button addScanReceiptButton;
    private Button addBankStatementButton;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize buttons
        addManualButton = view.findViewById(R.id.addManualButton);
        addScanReceiptButton = view.findViewById(R.id.addScanReceiptButton);
        addBankStatementButton = view.findViewById(R.id.addBankStatementButton);

        // Set click listeners
        addManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the Toast message with a fragment transaction
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // Create a new instance of ManualExpenseFragment
                ManualExpenseFragment manualExpenseFragment = new ManualExpenseFragment();

                // Replace the current fragment with the new one
                fragmentTransaction.replace(R.id.fragment_container, manualExpenseFragment);

                // Add the transaction to the back stack if you want to navigate back
                fragmentTransaction.addToBackStack(null);

                // Commit the transaction
                fragmentTransaction.commit();
            }
        });


        addScanReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the scan receipt button click
                Toast.makeText(getActivity(), "Scan Receipt Clicked", Toast.LENGTH_SHORT).show();
                // Implement your logic here
            }
        });

        addBankStatementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the upload bank statement button click
                Toast.makeText(getActivity(), "Upload Bank Statement Clicked", Toast.LENGTH_SHORT).show();
                // Implement your logic here
            }
        });
    }
}
