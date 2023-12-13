package com.cs407.spendsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionListAdapter extends RecyclerView.Adapter {
    private final Context mContext;
    private final List<Transaction> mTransactionList;

    public TransactionListAdapter(Context context, List<Transaction> transactionList) {
        mContext = context;
        mTransactionList = transactionList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_ui, parent, false);
        return new TransactionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Transaction transaction = mTransactionList.get(position);
        ((TransactionHolder)holder).bind(transaction);
    }

    @Override
    public int getItemCount() { return mTransactionList.size(); }

    private class TransactionHolder extends RecyclerView.ViewHolder {
        TextView show_category, show_amt, show_date;

        TransactionHolder(View itemView) {
            super(itemView);
            show_category = (TextView) itemView.findViewById(R.id.trans_cat);
            show_amt = (TextView) itemView.findViewById(R.id.trans_amt);
            show_date = (TextView) itemView.findViewById(R.id.trans_date);
        }

        void bind( Transaction transaction) {
            show_category.setText(transaction.getCategory());
            show_amt.setText(String.format("$%.2f",transaction.getAmount()));
            show_date.setText(transaction.getDate().toString());
        }
    }
}
