package com.example.taam;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taam.structures.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private FirebaseStorage fs;

    private ArrayList<Item> itemDataSet;
    private CardsAdapter cardsAdapter;
    protected static Button clickViewButton;
    protected static ArrayList<Item> posNumberChecked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
        DatabaseReference dbRef = db.getReference("Items");

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        posNumberChecked = new ArrayList<>();
        itemDataSet = new ArrayList<>();
        cardsAdapter = new CardsAdapter(itemDataSet);
        cardsAdapter.setMaxLine(6);
        recyclerView.setAdapter(cardsAdapter);
        clickViewButton = findViewById(R.id.buttonView);
        clickViewButton.setVisibility(View.INVISIBLE);


        dbRef.addValueEventListener(new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemDataSet.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);
                    assert item != null;
                    Log.d("[TAAM]", "Data: " + item.getName());

                    itemDataSet.add(item);
                    cardsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    public void buttonPopupView(View view){
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View viewPopupWindow = layoutInflater.inflate(R.layout.item_view_layout, null);

        // Find and set up the RecyclerView in the popup window layout
        RecyclerView recyclerItemsView = viewPopupWindow.findViewById(R.id.recycler_item_view);
        if (recyclerItemsView != null) {
            recyclerItemsView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            Log.e("MainActivity", "RecyclerView not found in popup window layout");
        }
        CardsAdapter selectedCards = new CardsAdapter(posNumberChecked);
        assert recyclerItemsView != null;
        recyclerItemsView.setAdapter(selectedCards);
        // Show the popup window
        PopupWindow popupWindow = new PopupWindow(viewPopupWindow, 900, 1800, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }
}