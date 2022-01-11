package com.example.food2;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food2.Common.Common;
import com.example.food2.Model.Entries;
import com.example.food2.ViewHolder.DiaryViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import java.util.Objects;
import java.util.TimeZone;

public class Diary extends AppCompatActivity {

    FloatingActionButton btnAddBackDate;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    private TextView displayDate, visualize, diaryTotalCo2PerDay;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    Spinner spinner;

    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter adapter;

    String currentEmail = Common.currentUser.getEmail();
    long passDate;
    long datePickerTimestamp;

    long secondsInADay = 24 * 60 * 60;
    String[] menu = { "diary", "profile"};

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        Log.d(TAG, "current email: " + currentEmail);

        // firebase firestore
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.listEntries);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        spinner = findViewById(R.id.spinner);
        displayDate = findViewById(R.id.date);
        diaryTotalCo2PerDay = findViewById(R.id.diaryTotalCo2PerDay);

        visualize = findViewById(R.id.visualize);

        btnAddBackDate = findViewById(R.id.btnAddBackDate);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),menu[position] , Toast.LENGTH_LONG).show();
                if (parent.getSelectedItem() == "profile"){
                    Intent startProfile = new Intent(Diary.this, Profile.class);
                    startProfile.putExtra("passDate", passDate);
                    startActivity(startProfile);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,menu);
        //aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        //spinner.setAdapter(aa);

        ArrayAdapter ad = new ArrayAdapter(this, R.layout.spinner_item, menu);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);


        if(getIntent() != null)
            passDate = getIntent().getLongExtra("passDate", passDate);

        if(passDate != 0) {
            Log.d(TAG, "passDate not zero: " + passDate);
            DateFormat format = new SimpleDateFormat("d/M/yyyy");
            format.setTimeZone(TimeZone.getTimeZone("GMT+1"));
            String stringPassedDate = format.format(passDate * 1000);
            displayDate.setText(stringPassedDate);
            loadEntries(currentEmail, passDate);
        }

        displayDate.setOnClickListener(v -> {
            Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT+1"));
            final Calendar cal = c.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    Diary.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateSetListener, year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();});

        dateSetListener = (datePick, year, month, dayOfMonth) -> {
            // january is 0 and december is 11 which is why:
            month = month + 1;
            Log.d(TAG, "onDateSet: dd/mm/year: " + dayOfMonth + "/" + month + "/" + year);
            String date = dayOfMonth + "/" + month + "/" + year;
            displayDate.setText(date);
            datePickerTimestamp = getMilliFromDate(date)/1000;
            Log.d(TAG, "timestamp from date picker: " + datePickerTimestamp);
            passDate = datePickerTimestamp;
            loadEntries(currentEmail, passDate);

            btnAddBackDate.setOnClickListener(v -> {
                Intent loadCategoryFromDiary = new Intent(Diary.this, CategoryList.class);
                loadCategoryFromDiary.putExtra("passDate", passDate);
                startActivity(loadCategoryFromDiary);
            });


            visualize.setOnClickListener(v -> {
                Intent loadVisuals = new Intent(Diary.this, DiaryVisual.class);
                loadVisuals.putExtra("passDate", passDate);
                Log.d(TAG, "passDate on line 143 " + passDate);
                startActivity(loadVisuals);
            });
        };

        //loadEntries(currentEmail, datePickerTimestamp);
        btnAddBackDate.setOnClickListener(v -> {
            Intent loadCategoryFromDiary = new Intent(Diary.this, CategoryList.class);
            loadCategoryFromDiary.putExtra("passDate", passDate);
            Log.d(TAG, "passDate on line 149 " + passDate);
            startActivity(loadCategoryFromDiary);
        });

        visualize.setOnClickListener(v -> {
            Intent loadVisuals = new Intent(Diary.this, DiaryVisual.class);
            loadVisuals.putExtra("passDate", passDate);
            Log.d(TAG, "passDate on line 160 " + passDate);
            startActivity(loadVisuals);
        });

    }

    private long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try{
            date = formatter.parse(dateFormat);
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadEntries(String email, long time) {
        Query query = FirebaseFirestore.
                getInstance().
                collection("entries").
                whereEqualTo("email", email).
                whereGreaterThanOrEqualTo("time", time).
                whereLessThan("time", time + secondsInADay);
        FirestoreRecyclerOptions options =
                new FirestoreRecyclerOptions.Builder<Entries>().
                        setQuery(query, Entries.class).
                        build();

        calculateTotalCo2(time);

        adapter = new FirestoreRecyclerAdapter<Entries, DiaryViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull DiaryViewHolder holder, int position, @NonNull Entries model) {

                holder.diaryItemName.setText(model.getFoodName());
                holder.diaryItemTotalGrammOfFood.setText(model.getQuantity());
                holder.diaryTotalCo2PerItem.setText(model.getCO2());
                holder.diaryItemName.setTextColor(Color.parseColor(model.getColour()));

                holder.setItemClickListener((view, position1, isLongCLick) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Diary.this);
                    String[] options1 = {"add again", "delete"};
                    builder.setItems(options1, (dialog, which) -> {
                        if (which == 0) {
                            // update
                            String foodClicked = model.getFoodId();
                            // String foodClicked = entriesList.get(position).getFoodId();
                            Intent updateFood = new Intent(Diary.this, FoodDetail.class);
                            updateFood.putExtra("FoodID", foodClicked);
                            updateFood.putExtra("passDate", passDate);
                            Log.d(TAG, "putExtra FoodID " + foodClicked);
                            Log.d(TAG, "passDate after update " + passDate);
                            startActivity(updateFood);
                        }
                        if (which == 1) {
                            deleteItem(holder.getAbsoluteAdapterPosition());
                            Intent reOpen = new Intent (Diary.this, Diary.class);
                            reOpen.putExtra("passDate", passDate);
                            Log.d(TAG, "passDate on line 207 " + passDate);
                            reOpen.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(reOpen);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                });
            }

            public void deleteItem(int position) {
                getSnapshots().getSnapshot(position).getReference().delete();
            }

            @NonNull
            @Override
            public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.diary_layout, parent, false);
                return new DiaryViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculateTotalCo2(long date) {
        ArrayList<Double> arrayListTotal = new ArrayList<>();
        firebaseFirestore.collection("entries").whereEqualTo("email", currentEmail).
                whereGreaterThanOrEqualTo("time", date).
                whereLessThan("time", date + secondsInADay).
                get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    // Log.d(TAG, "document.get(co2) " + document.get("co2"));
                    Entries entries = document.toObject(Entries.class);
                    String str = entries.getCO2();
                    double converted = Double.parseDouble(str);
                    Log.d(TAG, "per item co2 as an int " + converted);
                    arrayListTotal.add(converted);
                }
                Log.d(TAG, "arrayList " + arrayListTotal);
                double totalCo2 = arrayListTotal.stream().mapToDouble(f -> f).sum();
                DecimalFormat df = new DecimalFormat("#.#");
                diaryTotalCo2PerDay.setText(df.format(totalCo2));
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
}

