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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class SearchDialog {

    String[] choices = {"Clear", "Lot#", "Name", "Category", "Period"};
    private final Dialog searchDialog;

    AutoCompleteTextView scategory;
    ArrayAdapter<String> categoryItems;

    AutoCompleteTextView speriod;
    ArrayAdapter<String> periodItems;

    AutoCompleteTextView sortopt;
    ArrayAdapter<String> sortChoices;

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

        sortopt = searchDialog.findViewById(R.id.SortText);
        sortChoices = new ArrayAdapter<>(activity, R.layout.search_list, choices);

        sortopt.setAdapter(sortChoices);

        sortopt.setOnItemClickListener((adapterView, view, i, l) -> {
            String item3 = adapterView.getItemAtPosition(i).toString();
            if((sortopt.getText().toString()).equals("Clear")){
                sortopt.setText("");
                sortopt.setHint("Optional Sort By");
            }
            Toast.makeText(activity, "Item: " + item3, Toast.LENGTH_SHORT).show();
        });

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

                    sortItems(items, sortopt.getText().toString());

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
            items.removeIf(item -> !isSimilar(x, item.getName()));
        }
    }
    public void removePeriod(@NonNull ArrayList<Item> items, String x) {
        if(!x.isEmpty()) {
            items.removeIf(item -> !x.equals(item.getPeriod()));
        }
    }
    public void removeCategory(@NonNull ArrayList<Item> items, String x) {
        if(!x.isEmpty()) {
            items.removeIf(item -> !x.equals(item.getCategory()));
        }
    }

    public void searchRestart(){

        slotnum.setText("");
        sname.setText("");
        scategory.setText("");
        speriod.setText("");
        sortopt.setText("");

        slotnum.clearFocus();
        sname.clearFocus();

        if (scategory != null) {
            scategory.clearFocus();
        }

        if (speriod != null) {
            speriod.clearFocus();
        }

        if (sortopt != null) {
            sortopt.clearFocus();
        }

        assert scategory != null;
        scategory.setAdapter(categoryItems);
        speriod.setAdapter(periodItems);
        sortopt.setAdapter(sortChoices);
        searchDialog.dismiss();
    }

    public void sortItems(ArrayList<Item> items, String c) {
        if (c.equals(choices[1])) {
            items.sort(Comparator.comparing(Item::getLotNumber));
        } else if (c.equals(choices[2])) {
            items.sort(Comparator.comparing(Item::getName));
        } else if (c.equals(choices[3])){
            items.sort(Comparator.comparing(Item::getCategory));
        }  else if (c.equals(choices[4])) {
            items.sort(Comparator.comparing(Item::getPeriod));
        }
    }

    public static boolean isSimilar(String x, String y) {


        String big;
        String small;
        int n = 0, m = 0;

        x = removeSpaces(x);
        y = removeSpaces(y);
        x = x.toLowerCase();
        y = y.toLowerCase();

        if(x.length()>=y.length()){
            m = y.length();
            n = x.length();
            big = x;
            small = y;
        } else {
            m = x.length();
            n = y.length();
            big = y;
            small = x;
        }

        boolean Sub = big.toLowerCase().contains(small.toLowerCase());
        if(Sub){
            return true;
        }

        int j = 0;

        for (int i = 0; i < n && j < m; i++) {
            if (small.charAt(j) == big.charAt(i)) {
                j++;
            }
        }

        return (j == m && Math.abs(m-n)<=4);
    }

    public static String removeSpaces(String str) {
        if (str == null) {
            return null;
        }
        return str.replace(" ", "");
    }
}
