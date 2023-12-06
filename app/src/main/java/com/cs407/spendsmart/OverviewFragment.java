package com.cs407.spendsmart;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OverviewFragment extends Fragment {
    PieChart pieChart;
    CardView cardView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<PieEntry> entries;
    float total;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChart = requireActivity().findViewById(R.id.piechart);
        cardView = requireView().findViewById(R.id.transaction_card);
        initPieChart();
        loadCharts();
        pieChart.setOnChartValueSelectedListener(new pieChartListener());
    }

    private void initPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setDragDecelerationFrictionCoef(0.8f);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieChart.setHoleRadius(70f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setCenterTextSize(30f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
    }

    private void loadCharts() {
        Map<String,Float> map = new HashMap<>();
        String label = "Categories";

        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> transactions = (ArrayList<String>)documentSnapshot.get("transactions");
            assert transactions != null;
            for(String trans: transactions){
                DocumentReference transactionDoc = db.collection("transactions").document(trans);
                transactionDoc.get().addOnSuccessListener(docSnapshot -> {
                    String category = (String) docSnapshot.get("category");
                    Double amount = (Double) docSnapshot.get("amount");
                    if(amount == null){
                        return;
                    }
                    if(map.containsKey(category)){
                        map.put(category, map.get(category)+amount.floatValue());
                    }
                    else {
                        map.put(category, amount.floatValue());
                    }

                    entries = new ArrayList<>();
                    for(String key: map.keySet()){
                        entries.add(new PieEntry(map.get(key), key));
                    }

                    // Pie Chart:
                    int[] colorArray = Arrays.copyOfRange(getResources().getIntArray(R.array.chartColors), 0, entries.size());
                    ArrayList<Integer> colors = new ArrayList<>();
                    for(int color: colorArray){
                        colors.add(color);
                    }

                    PieDataSet dataSet = new PieDataSet(entries, label);
                    PercentFormatter formatter = new PercentFormatter();
                    dataSet.setValueFormatter(formatter);
                    dataSet.setValueTextSize(12f);
                    dataSet.setColors(colors);
                    PieData data = new PieData(dataSet);
                    data.setDrawValues(true);

                    total = 0;
                    for(PieEntry entry: entries){
                        total += entry.getValue();
                    }
                    pieChart.setCenterText(String.format("$%.2f",total));
                    pieChart.setData(data);
                    pieChart.invalidate();

                });
            }
        });
    }

    private class pieChartListener implements OnChartValueSelectedListener {

        @SuppressLint("DefaultLocale")
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            cardView = requireView().findViewById(R.id.transaction_card);
            cardView.setVisibility(View.VISIBLE);
            TextView selected_name = requireActivity().findViewById(R.id.trans_category);
            TextView selected_amount = requireActivity().findViewById(R.id.trans_amount);
            ProgressBar selected_percent = requireActivity().findViewById(R.id.trans_percent);
            selected_name.setText(entries.get((int)h.getX()).getLabel());
            selected_amount.setText(String.format("$%.2f",entries.get((int)h.getX()).getValue()));
            selected_percent.setProgress((int) (entries.get((int)h.getX()).getValue() / total * 100));
        }
        @Override
        public void onNothingSelected() {
            CardView cardView = requireView().findViewById(R.id.transaction_card);
            cardView.setVisibility(View.INVISIBLE);
        }
    }

}