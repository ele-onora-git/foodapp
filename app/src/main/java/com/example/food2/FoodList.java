package com.example.food2;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.food2.Interface.ItemClickListener;
import com.example.food2.Model.Food;
import com.example.food2.ViewHolder.FoodListViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView recyclerFoodList;
    RecyclerView.LayoutManager layoutManager;

    String categoryId="";
    long passDate;

    FirebaseRecyclerAdapter<Food, FoodListViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        // Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");
        foodList.keepSynced(true);

        recyclerFoodList = findViewById(R.id.recyclerFoodList);
        recyclerFoodList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerFoodList.setLayoutManager(layoutManager);

        // get intent here
        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryId");
            passDate = getIntent().getLongExtra("passDate", passDate);}

        if(!categoryId.isEmpty() && passDate != 0){
            loadListFood(categoryId);
        }
        // check if the correct (the clicked one) category is being passed
        Log.d("TAG", "Check category " +categoryId);
    }

    private void loadListFood(String categoryId) {

        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>().
                        setQuery(foodList.orderByChild("FoodID").equalTo(categoryId),
                                Food.class).
                        build();

        adapter = new FirebaseRecyclerAdapter<Food,
                FoodListViewHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull FoodListViewHolder holder,
                                         int position, @NonNull Food model) {
                holder.foodName.setText(model.getName());

                // insert image
                Picasso.with(getBaseContext()).load(model.getImage()).fit().centerCrop().into(holder.foodImageView);

                final Food local = model;

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodID", adapter.getRef(position).getKey());
                        foodDetail.putExtra("passDate", passDate);
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.category_item, parent, false);
                return new FoodListViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerFoodList.setAdapter(adapter);
    }
}