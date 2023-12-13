package com.cs407.spendsmart;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private LineChart chart;
    ArrayList<Entry> entries;
    private TimeInterval selectedTimeInterval = TimeInterval.WEEK;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // enum for time intervals
    public enum TimeInterval {
        WEEK("Weekly"),
        MONTH("Monthly"),
        YEAR("Yearly");

        private String label;

        TimeInterval(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        chart = view.findViewById(R.id.chart); // Initialize chart from layout
        setupChart(); // Method to configure chart settings
        updateChartData(selectedTimeInterval); // Update chart with default time interval

        // Set up buttons or tabs for time interval selection here
        // e.g., view.findViewById(R.id.buttonDay).setOnClickListener(v -> updateChartData(TimeInterval.DAY));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button allTransactions = requireActivity().findViewById(R.id.trans_btn);
        Log.d("HELP", ""+allTransactions);
        allTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                AllTransactionsFragment allTransactionsFragment = new AllTransactionsFragment();

                transaction.replace(R.id.fragment_container, allTransactionsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void setupChart() {
        // Customize chart settings here
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);

        // Hide grid lines
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);

        // Hide right Y-axis
        chart.getAxisRight().setEnabled(false);

        // Customize left Y-axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);

        // Customize X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        // Disable description text
        chart.getDescription().setEnabled(false);

        // Disable legend
        chart.getLegend().setEnabled(false);

        // Add an empty data object
        chart.setData(new LineData());
        chart.animateX(1400, Easing.EasingOption.EaseInOutQuad);
    }

    private void updateChartData(TimeInterval interval) {
        Map<String,Float> map = new HashMap<>();
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> transactions = (ArrayList<String>)documentSnapshot.get("transactions");
            assert transactions != null;
            for(String trans: transactions){
                DocumentReference transactionDoc = db.collection("transactions").document(trans);
                transactionDoc.get().addOnSuccessListener(docSnapshot -> {
                    String category = (String) docSnapshot.get("category");
                    Double amount = (Double) docSnapshot.get("amount");
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = formatter.parse(docSnapshot.get("date").toString());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if (amount == null) {
                        return;
                    }
                    Log.i("info", new Date((String) docSnapshot.get("date")).toString());
                    if (map.containsKey(category)) {
                        map.put(category, map.get(category) + amount.floatValue());
                    } else {
                        map.put(category, amount.floatValue());
                    }
                    entries = new ArrayList<>();
                    int i = 0;
                    for (String key : map.keySet()) {
                        entries.add(new Entry(i, map.get(key)));
                        ++i;
                    }
                    LineDataSet dataSet = new LineDataSet(entries, "Spending Data");
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet.setDrawFilled(true);
                    dataSet.setDrawCircles(false);
                    dataSet.setLineWidth(5f);
                    dataSet.setColor(Color.rgb(240, 238, 70)); // Customize line color

                    // Set the gradient drawable for the fill
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.gradient_fill);
                    dataSet.setFillDrawable(drawable);

                    // Set the custom XAxis value formatter
                    IAxisValueFormatter xAxisFormatter = new MyXAxisValueFormatter(interval, date);
                    chart.getXAxis().setValueFormatter(xAxisFormatter);

                    // Set the data and update the chart
                    LineData lineData = new LineData(dataSet);
                    chart.setData(lineData);
                    chart.invalidate(); // Refresh the chart
                });
            }
        });

        // Example data points
//        entries.add(new Entry(0f, 50f));
//        entries.add(new Entry(1f, 80f));
//        entries.add(new Entry(2f, 60f));
//        entries.add(new Entry(3f, 70f));
    }
    private class MyXAxisValueFormatter implements IAxisValueFormatter {
        private Date date;
        private final SimpleDateFormat weekFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        private final SimpleDateFormat yearFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);

        private TimeInterval interval;

        public MyXAxisValueFormatter(TimeInterval interval, Date date) {
            this.interval = interval;
            this.date = date;
        }
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // Convert float value to date string based on time interval
//            long millis = (long) value;
//            Date date = new Date(millis);
            switch (interval) {
                case WEEK:
                    return weekFormat.format(date);
                case MONTH:
                    return monthFormat.format(date);
                case YEAR:
                    return yearFormat.format(date);
                default:
                    return "";
            }
        }
    }
}
