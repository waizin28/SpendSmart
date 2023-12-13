package com.cs407.spendsmart;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AllTransactionsFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private List<Transaction> transactionList = new ArrayList<>();
    private boolean listLoaded = false;
    TransactionListAdapter mTransactionAdapter;

    public AllTransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView back_btn = requireActivity().findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                HomeFragment homeFragment = new HomeFragment();

                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        if(!listLoaded){
            loadTransactions();
        }
        else if(listLoaded) {
            displayTransactions();
        }
        db.collection("transactions").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                if(value.getDocuments().size() > transactionList.size() && listLoaded){
                    loadTransactions();
                }
            }
        });

    }

    public void loadTransactions() {
        transactionList.clear();
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> transactions = (ArrayList<String>)documentSnapshot.get("transactions");
            assert transactions != null;
            for(String trans: transactions){
                DocumentReference transactionDoc = db.collection("transactions").document(trans);
                transactionDoc.get().addOnSuccessListener(docSnapshot -> {
                    String category = docSnapshot.get("category").toString();
                    Double amount = (Double) docSnapshot.get("amount");
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = formatter.parse(docSnapshot.get("date").toString());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    Transaction transaction = new Transaction(amount, category, date);
                    transactionList.add(transaction);

                    transactionList.sort(((transaction1, t1) -> {
                        if(transaction1.getDate().after(t1.getDate())){
                            return -1;
                        }
                        else if(transaction1.getDate().before(t1.getDate())){
                            return 1;
                        }
                        else{
                            return 0;
                        }
                    }));

                    displayTransactions();

                });
            }
        });
    }

    public void displayTransactions() {
        RecyclerView mTransactionRecycler = (RecyclerView) requireView().findViewById(R.id.transaction_recycler);
        mTransactionAdapter = new TransactionListAdapter(getContext(), transactionList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(false);
        mTransactionRecycler.setLayoutManager(linearLayoutManager);
        mTransactionRecycler.setAdapter(mTransactionAdapter);
    }
}