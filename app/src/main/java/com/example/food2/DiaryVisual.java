package com.example.food2;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food2.Common.Common;
import com.example.food2.Interface.MyCallback;
import com.example.food2.Model.Entries;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class DiaryVisual extends AppCompatActivity {
    FloatingActionButton btnAddBackDateVisual;
    TextView displayDateVisual, diaryTotalCo2PerDayVisual, backToTheList;
    long passDate;
    long datePickerTimestampVisual;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    FirebaseFirestore firebaseFirestore;
    PieChart pieChartOne, pieChartTwo;
    // private final String[] str = {"high", "very high", "medium", "low"};

    long secondsInADay = 24 * 60 * 60;
    private final String currentEmail = Common.currentUser.getEmail();
    private final DecimalFormat df = new DecimalFormat("#.#");

    ArrayList<Double> arrayListPerLevel = new ArrayList<>();
    ArrayList<String> arrString = new ArrayList<>();

    ArrayList<Double> arrayListPerLevelFood = new ArrayList<>();
    ArrayList<String> arrStringFood = new ArrayList<>();

    int count = 0;
    int countFood = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < 4; i++) {
            arrayListPerLevel.add(0.0);
            arrString.add("empty");
        }

        for (int i = 0; i < 4; i++) {
            arrayListPerLevelFood.add(0.0);
            arrStringFood.add("empty");
        }

        setContentView(R.layout.activity_diary_visual);
        btnAddBackDateVisual = findViewById(R.id.btnAddBackDateVisual);
        displayDateVisual = findViewById(R.id.dateVisual);
        diaryTotalCo2PerDayVisual = findViewById(R.id.diaryTotalCo2PerDayVisual);
        backToTheList = findViewById(R.id.backToTheList);
        firebaseFirestore = FirebaseFirestore.getInstance();
        pieChartOne = findViewById(R.id.piechartOne);
        pieChartTwo = findViewById(R.id.piechartTwo);

        if(getIntent() != null)
            passDate = getIntent().getLongExtra("passDate", passDate);

        if(passDate != 0) {
            Log.d(TAG, "passDate not zero: " + passDate);
            DateFormat format = new SimpleDateFormat("d/M/yyyy");
            format.setTimeZone(TimeZone.getTimeZone("GMT+1"));
            String stringPassedDate = format.format(passDate * 1000);
            displayDateVisual.setText(stringPassedDate);
            calculateTotalCo2(passDate);
            sum(passDate);
            sumFood(passDate);
        }

        btnAddBackDateVisual.setOnClickListener(v -> {
            Intent loadCategoryFromDiaryVisual = new Intent(DiaryVisual.this, CategoryList.class);
            loadCategoryFromDiaryVisual.putExtra("passDate", passDate);
            startActivity(loadCategoryFromDiaryVisual);
        });
        backToTheList.setOnClickListener(v -> {
            Intent backToDiary = new Intent(DiaryVisual.this, Diary.class);
            backToDiary.putExtra("passDate", passDate);
            startActivity(backToDiary);
        });
        displayDateVisual.setOnClickListener(v -> {
            Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT+1"));
            final Calendar cal = c.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    DiaryVisual.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateSetListener, year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });
        dateSetListener = (datePick, year, month, dayOfMonth) -> {
            // january is 0
            month = month + 1;
            Log.d(TAG, "onDateSet: dd/mm/year: " + dayOfMonth + "/" + month + "/" + year);
            String date = dayOfMonth + "/" + month + "/" + year;
            displayDateVisual.setText(date);
            datePickerTimestampVisual = getMilliFromDate(date) / 1000;
            Log.d(TAG, "timestamp from date picker: " + datePickerTimestampVisual);
            passDate = datePickerTimestampVisual;

            btnAddBackDateVisual.setOnClickListener(v -> {
                Intent loadCategoryFromDiaryVisual = new Intent(DiaryVisual.this, CategoryList.class);
                loadCategoryFromDiaryVisual.putExtra("passDate", passDate);
                startActivity(loadCategoryFromDiaryVisual);
            });
            backToTheList.setOnClickListener(v -> {
                Intent backToDiary = new Intent(DiaryVisual.this, Diary.class);
                backToDiary.putExtra("passDate", passDate);
                startActivity(backToDiary);
            });

            calculateTotalCo2(passDate);
            sum(passDate);
            sumFood(passDate);
        };
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumFood(long passDate) {

        sumLowFood(passDate, data -> {
            Log.d(TAG, "check: dataLow: " + data);
            chartTwo(data, "low");
        });

        sumMediumFood(passDate, data -> {
            Log.d(TAG, "check: dataMedium: " + data);
            chartTwo(data, "medium");
        });

        sumHighFood(passDate, data -> {
            Log.d(TAG, "check: dataHigh: " + data);
            chartTwo(data, "high");
        });

        sumVeryHighFood(passDate, data -> {
            Log.d(TAG, "check: dataVeryHigh: " + data);
            chartTwo(data, "very high");
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sum(long passDate) {
        sumLow(passDate, data -> {
            Log.d(TAG, "check: dataLow: " + data);
            chartOne(data, "low");
        });

        sumMedium(passDate, data -> {
            Log.d(TAG, "check: dataMedium: " + data);
            chartOne(data, "medium");
        });


        sumHigh(passDate, data -> {
            Log.d(TAG, "check: dataHigh: " + data);
            chartOne(data, "high");
        });

        sumVeryHigh(passDate, data -> {
            Log.d(TAG, "check: dataVeryHigh: " + data);
            chartOne(data, "very high");
        });
    }

    private void chartOne(Double db, String string) {
        if (count < 3) {{
            if (string.equals("low")){
                arrayListPerLevel.set(0, db);
                arrString.set(0, string);
                count += 1;
                Log.d(TAG, "check: count under 3: " + count);
            }
            if (string.equals("medium")){
                arrayListPerLevel.set(1, db);
                arrString.set(1, string);
                count += 1;
                Log.d(TAG, "check: count under 3: " + count);
            }
            if (string.equals("high")){
                arrayListPerLevel.set(2, db);
                arrString.set(2, string);
                count += 1;
                Log.d(TAG, "check: count under 3: " + count);
            }
            if (string.equals("very high")){
                arrayListPerLevel.set(3, db);
                arrString.set(3, string);
                count += 1;
                Log.d(TAG, "check: count under 3: " + count);
            }
        }
            Log.d(TAG, "check: arraylist size under 4: " + arrayListPerLevel.size());
        } else if (count == 3){
            Log.d(TAG, "check: count on 3: " + count);
            if (string.equals("low")){
                arrayListPerLevel.set(0, db);
                arrString.set(0, string);
                count += 1;
            }
            if (string.equals("medium")){
                arrayListPerLevel.set(1, db);
                arrString.set(1, string);
                count += 1;
            }
            if (string.equals("high")){
                arrayListPerLevel.set(2, db);
                arrString.set(2, string);
                count += 1;
            }
            if (string.equals("very high")){
                arrayListPerLevel.set(3, db);
                arrString.set(3, string);
                count += 1;
            }
            Log.d(TAG, "check: arraylist size 4: " + arrayListPerLevel.size());
            setUpPieChartOne();
            loadPieChartDataOne(arrayListPerLevel);
            count = 0;
            arrayListPerLevel.clear();
            arrString.clear();
            for (int i = 0; i < 4; i++) {
                arrayListPerLevel.add(0.0);
                arrString.add("empty");
            }
        }
    }

    private void setUpPieChartOne() {
        pieChartOne.setDrawHoleEnabled(true);
        pieChartOne.setUsePercentValues(true);
        pieChartOne.setEntryLabelTextSize(12);
        pieChartOne.setEntryLabelColor(Color.BLACK);
        pieChartOne.getDescription().setEnabled(false);
        pieChartOne.setCenterText("CO2 gramms");
        pieChartOne.setCenterTextSize(20);


        Legend l = pieChartOne.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void loadPieChartDataOne(ArrayList<Double> arrayList) {
        List<PieEntry> pieEntries = new ArrayList<>();
        Log.d(TAG, "check: arrayList piechart1 " + arrayList);
        for (int i = 0; i < 4; i++) {
                    pieEntries.add(new PieEntry(Double.valueOf(arrayList.get(i)).floatValue(), (arrString.get(i))));
                    Log.d(TAG, "check: pieEntries1 in the loop " + pieEntries);
        }

        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.parseColor("#FF7CB342"));
        colours.add(Color.parseColor("#FFFFC107"));
        colours.add(Color.parseColor("#FFE53935"));
        colours.add(Color.parseColor("#FFB71C1C"));

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(colours);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChartOne));
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(12f);

        pieChartOne.setData(data);
        pieChartOne.invalidate();

        // animation
        pieChartOne.animateY(1400, Easing.EaseInOutQuad);
    }

    private void chartTwo(Double db, String string) {
        if (countFood < 3) {{
            if (string.equals("low")){
                arrayListPerLevelFood.set(0, db);
                arrStringFood.set(0, string);
                countFood += 1;
                Log.d(TAG, "check: count under 3: " + countFood);
            }
            if (string.equals("medium")){
                arrayListPerLevelFood.set(1, db);
                arrStringFood.set(1, string);
                countFood += 1;
                Log.d(TAG, "check: count under 3: " + countFood);
            }
            if (string.equals("high")){
                arrayListPerLevelFood.set(2, db);
                arrStringFood.set(2, string);
                countFood += 1;
                Log.d(TAG, "check: count under 3: " + countFood);
            }
            if (string.equals("very high")){
                arrayListPerLevelFood.set(3, db);
                arrStringFood.set(3, string);
                countFood += 1;
                Log.d(TAG, "check: count under 3: " + countFood);
            }
        }
            Log.d(TAG, "check: arraylist size under 4: " + arrayListPerLevelFood.size());
        } else if (countFood == 3){
            Log.d(TAG, "check: count on 3: " + countFood);
            if (string.equals("low")){
                arrayListPerLevelFood.set(0, db);
                arrStringFood.set(0, string);
                countFood += 1;
            }
            if (string.equals("medium")){
                arrayListPerLevelFood.set(1, db);
                arrStringFood.set(1, string);
                countFood += 1;
            }
            if (string.equals("high")){
                arrayListPerLevelFood.set(2, db);
                arrStringFood.set(2, string);
                countFood += 1;
            }
            if (string.equals("very high")){
                arrayListPerLevelFood.set(3, db);
                arrStringFood.set(3, string);
                countFood += 1;
            }
            Log.d(TAG, "check: arraylist size 4: " + arrayListPerLevelFood.size());
            setUpPieChartTwo();
            Log.d(TAG, "check: arrStringFood line 356 " + arrStringFood);
            loadPieChartDataTwo(arrayListPerLevelFood);
            countFood = 0;
            arrayListPerLevelFood.clear();
            arrStringFood.clear();
            for (int i = 0; i < 4; i++) {
                arrayListPerLevelFood.add(0.0);
                arrStringFood.add("empty");
            }
        }
    }

    private void setUpPieChartTwo() {
        pieChartTwo.setDrawHoleEnabled(true);
        pieChartTwo.setUsePercentValues(true);
        pieChartTwo.setEntryLabelTextSize(12);
        pieChartTwo.setEntryLabelColor(Color.BLACK);
        pieChartTwo.getDescription().setEnabled(false);
        pieChartTwo.setCenterText("food gramms");
        pieChartTwo.setCenterTextSize(20);

        Legend l = pieChartTwo.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void loadPieChartDataTwo(ArrayList<Double> arrayList) {
        List<PieEntry> pieEntriesFood = new ArrayList<>();
        Log.d(TAG, "check: arrayList piechart2 " + arrayList);
        for (int i = 0; i < arrayList.size(); i++) {
                pieEntriesFood.add(new PieEntry(Double.valueOf(arrayList.get(i)).floatValue(), (arrStringFood.get(i))));
                Log.d(TAG, "check: pieEntries2 in the loop " + pieEntriesFood);
        }

        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.parseColor("#FF7CB342"));
        colours.add(Color.parseColor("#FFFFC107"));
        colours.add(Color.parseColor("#FFE53935"));
        colours.add(Color.parseColor("#FFB71C1C"));

        PieDataSet dataSetFood = new PieDataSet(pieEntriesFood, "");
        dataSetFood.setColors(colours);

        PieData dataFood = new PieData(dataSetFood);
        dataFood.setDrawValues(true);
        dataFood.setValueFormatter(new PercentFormatter(pieChartOne));
        dataFood.setValueTextColor(Color.BLACK);
        dataFood.setValueTextSize(12f);

        pieChartTwo.setData(dataFood);
        pieChartTwo.invalidate();

        // animation
        pieChartTwo.animateY(1400, Easing.EaseInOutQuad);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculateTotalCo2(long date) {
        ArrayList<Double> arrayListVisualTotal = new ArrayList<>();
        firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).
                whereGreaterThanOrEqualTo("time", date).
                whereLessThan("time", date + secondsInADay).
                get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    Entries entries = document.toObject(Entries.class);
                    String str = entries.getCO2();
                    double converted = Math.round((Double.parseDouble(str)) * 10.0) / 10.0;
                    Log.d(TAG, "per item co2 as an int " + converted);
                    arrayListVisualTotal.add(converted);
                }
                Log.d(TAG, "arrayList " + arrayListVisualTotal);
                double totalCo2 = arrayListVisualTotal.stream().mapToDouble(f -> f).sum();
                diaryTotalCo2PerDayVisual.setText(df.format(totalCo2));
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumLow(long date, MyCallback myCallback) {
        ArrayList<Double> arrLow = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "low");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    double low = Math.round((Double.parseDouble(Objects.requireNonNull(document.get("co2")).toString())) * 10.0) / 10.0;
                    arrLow.add(low);
                }
                double sumLow = arrLow.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumLow);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumMedium(long date, MyCallback myCallback) {
        ArrayList<Double> arrMedium = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "medium");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    double medium = Math.round((Double.parseDouble(Objects.requireNonNull(document.get("co2")).toString())) * 10.0) / 10.0;
                    arrMedium.add(medium);
                }
                double sumMedium = arrMedium.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumMedium);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumHigh(long date, MyCallback myCallback) {
        ArrayList<Double> arrHigh = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "high");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    double high = Math.round((Double.parseDouble(Objects.requireNonNull(document.get("co2")).toString())) * 10.0) / 10.0;
                    arrHigh.add(high);
                }
                double sumHigh = arrHigh.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumHigh);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumVeryHigh(long date, MyCallback myCallback) {
        ArrayList<Double> arrVeryHigh = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "very high");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    double veryHigh = Math.round((Double.parseDouble(Objects.requireNonNull(document.get("co2")).toString())) * 10.0) / 10.0;
                    arrVeryHigh.add(veryHigh);
                }
                double sumVeryHigh = arrVeryHigh.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumVeryHigh);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumLowFood(long date, MyCallback myCallback) {
        ArrayList<Integer> arrLowFood = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "low");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    int low = Integer.parseInt(Objects.requireNonNull(document.get("quantity")).toString());
                    arrLowFood.add(low);
                }
                double sumLowFood = arrLowFood.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumLowFood);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumMediumFood(long date, MyCallback myCallback) {
        ArrayList<Integer> arrMediumFood = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "medium");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    int medium = Integer.parseInt(Objects.requireNonNull(document.get("quantity")).toString());
                    arrMediumFood.add(medium);
                }
                double sumMediumFood = arrMediumFood.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumMediumFood);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumHighFood(long date, MyCallback myCallback) {
        ArrayList<Integer> arrHighFood = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "high");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    int high = Integer.parseInt(Objects.requireNonNull(document.get("quantity")).toString());
                    arrHighFood.add(high);
                }
                double sumHighFood = arrHighFood.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumHighFood);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sumVeryHighFood(long date, MyCallback myCallback) {
        ArrayList<Integer> arrVeryHighFood = new ArrayList<>();
        Query query = firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).whereGreaterThanOrEqualTo("time", date).whereLessThan("time", date + secondsInADay).whereEqualTo("level", "very high");
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    int veryHigh = Integer.parseInt(Objects.requireNonNull(document.get("quantity")).toString());
                    arrVeryHighFood.add(veryHigh);
                }
                double sumVeryHighFood = arrVeryHighFood.stream().mapToDouble(f -> f).sum();
                myCallback.onCallback(sumVeryHighFood);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    private long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }
}