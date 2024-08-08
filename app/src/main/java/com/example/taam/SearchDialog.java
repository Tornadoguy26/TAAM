package com.example.taam;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taam.structures.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class SearchDialog {

    private final Dialog searchDialog;

    AutoCompleteTextView scategory;
    ArrayAdapter<String> categoryItems;

    AutoCompleteTextView speriod;
    ArrayAdapter<String> periodItems;

    private boolean isSearch = false;
    private final EditText slotnum, sname;

    ArrayList<Item> items = new ArrayList<>();

    public SearchDialog(AppCompatActivity activity) {

        searchDialog = new Dialog(activity);
        searchDialog.setContentView(R.layout.search_input);
        Objects.requireNonNull(searchDialog.getWindow()).setBackgroundDrawable((new ColorDrawable(Color.TRANSPARENT)));
        searchDialog.setCancelable(false);

        Button searchCancelBTN = searchDialog.findViewById(R.id.BackButton);
        Button searchResultBTN = searchDialog.findViewById(R.id.ResultButton);

        scategory = searchDialog.findViewById(R.id.LotCategory);
        categoryItems = new ArrayAdapter<>(activity, R.layout.search_list, DatabaseManager.categories);

        scategory.setAdapter(categoryItems);

        scategory.setOnItemClickListener((adapterView, view, i, l) -> {
            String item = adapterView.getItemAtPosition(i).toString();
            if((scategory.getText().toString()).equals("Clear")){
                scategory.setText("");
                scategory.setHint("Select Category");
            }
            Toast.makeText(activity, "Item: " + item, Toast.LENGTH_SHORT).show();
        });

        speriod = searchDialog.findViewById(R.id.LotPeriod);
        periodItems = new ArrayAdapter<>(activity, R.layout.search_list, DatabaseManager.periods);

        speriod.setAdapter(periodItems);

        speriod.setOnItemClickListener((adapterView, view, i, l) -> {
            String item2 = adapterView.getItemAtPosition(i).toString();
            if((speriod.getText().toString()).equals("Clear")){
                speriod.setText("");
                speriod.setHint("Select Period");
            }
            Toast.makeText(activity, "Item: " + item2, Toast.LENGTH_SHORT).show();
        });

        slotnum = searchDialog.findViewById(R.id.LotNum);
        sname = searchDialog.findViewById(R.id.LotName);

        searchCancelBTN.setOnClickListener(v -> searchRestart());
        searchResultBTN.setOnClickListener(v -> {
            isSearch=true;
            items.clear();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/").getReference("Items");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Item item = snapshot.getValue(Item.class);
                        items.add(item);
                    }

                    int num = 0;
                    boolean state = false;

                    if(!slotnum.getText().toString().isEmpty()){
                        num = Integer.parseInt(slotnum.getText().toString());
                    } else {
                        state = true;
                    }

                    removeLot(items, num, state);
                    removeName(items, sname.getText().toString());
                    removePeriod(items, speriod.getText().toString());
                    removeCategory(items, scategory.getText().toString());
                    Intent intent = new Intent(activity, ViewItemActivity.class);
                    intent.putExtra("checkedItems", items);
                    activity.startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", databaseError.getMessage());
                }
            });
            searchDialog.dismiss();
        });
    }

    public boolean isSearch() { return isSearch; }
    public void setSearch(boolean isSearch) { this.isSearch = isSearch; }

    public void show() {
        searchDialog.show();
    }

    public void removeLot(@NonNull ArrayList<Item> items, int x, boolean state){
        if(!state) {
            items.removeIf(item -> x != item.getLotNumber());
        }
    }
    public void removeName(@NonNull ArrayList<Item> items, String x){
        if(!x.isEmpty()) {
            items.removeIf(item -> !item.getName().toLowerCase().contains(x.toLowerCase()));
        }
    }
    public void removePeriod(@NonNull ArrayList<Item> items, String x) {
        if(!x.isEmpty()) {
            items.removeIf(item -> !x.equalsIgnoreCase(item.getPeriod()));
        }
    }
    public void removeCategory(@NonNull ArrayList<Item> items, String x) {
        if(!x.isEmpty()) {
            items.removeIf(item -> !x.equalsIgnoreCase(item.getCategory()));
        }
    }

    public void searchRestart(){

        slotnum.setText("");
        sname.setText("");
        scategory.setText("");
        speriod.setText("");

        slotnum.clearFocus();

        sname.clearFocus();

        if (scategory != null) {
            scategory.clearFocus();
        }

        if (speriod != null) {
            speriod.clearFocus();
        }

        assert scategory != null;
        scategory.setAdapter(categoryItems);
        speriod.setAdapter(periodItems);
        searchDialog.dismiss();
    }
}
