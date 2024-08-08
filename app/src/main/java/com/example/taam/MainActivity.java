package com.example.taam;

import android.net.Uri;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.Settings;

import android.util.Log;


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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// For requesting permissions


public class MainActivity extends AppCompatActivity {

    private boolean isAdmin;
    private MainCardsAdapter mainCardsAdapter;

    private ArrayList<Item> itemDataSet;
    private LoginDialog loginDialog;
    private SearchDialog searchDialog;
    private ReportDialog reportDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Database Manager Instance
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        // Get the admin status from intent
        isAdmin = getIntent().getBooleanExtra("admin_status", false);
        Log.d("[TAAM]", "isAdmin: " + isAdmin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //==================================================================================

        // Create dialog used for login
        loginDialog = new LoginDialog(this);
        Button adminBtn = findViewById(R.id.adminLoginButton);

        TextView titleText = findViewById(R.id.titleTextView);
        Button buttonAdd = findViewById(R.id.addButton);
        Button buttonRemove = findViewById(R.id.removeButton);
        Button buttonReport = findViewById(R.id.reportButton);
        if (isAdmin) {
            titleText.setText(R.string.admin_screen_title);
            buttonAdd.setVisibility(View.VISIBLE);
            buttonRemove.setVisibility(View.VISIBLE);
            buttonReport.setVisibility(View.VISIBLE);
        }

        // Only enable admin buttons if we are admin
        LinearLayout adminLayout = findViewById(R.id.adminFeaturesLayout);
        for (int i = 0; i < adminLayout.getChildCount(); i++) {
            View child = adminLayout.getChildAt(i);
            child.setEnabled(isAdmin);
        }

        // If we are logged in, change button so we can log out
        if (isAdmin) adminBtn.setText(R.string.back_text);
        adminBtn.setOnClickListener(v -> {
            if (isAdmin) { switchAdminStatus(false); }
            else { loginDialog.show(); }
        });

        // Create search dialog to be shown on button press
        searchDialog = new SearchDialog(this);
        Button searchBtn = findViewById(R.id.searchButton);
        searchBtn.setOnClickListener(v -> {
            if(searchDialog.isSearch()){
                searchDialog.searchRestart();
                searchDialog.setSearch(false);
            }
            searchDialog.show();
        });

        // Open new activity for adding items
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        // Show all items in the main screen
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemDataSet = new ArrayList<>();
        mainCardsAdapter = new MainCardsAdapter(itemDataSet);
        recyclerView.setAdapter(mainCardsAdapter);

        databaseManager.getDbRef().child("Items").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemDataSet.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);
                    assert item != null;
                    itemDataSet.add(item);
                    mainCardsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { /* Handle possible errors*/ }
        });

        // Show all the selected items in the view activity
        Button viewBtn = findViewById(R.id.viewButton);
        viewBtn.setOnClickListener(view -> {
            if (mainCardsAdapter.getCheckedItems().isEmpty()) { return; }
            Intent intent = new Intent(MainActivity.this, ViewItemActivity.class);
            intent.putExtra("checkedItems", mainCardsAdapter.getCheckedItems());
            Log.d("[TAAM]", "Passing array: " + mainCardsAdapter.getCheckedItems().size());
            startActivity(intent);
        });

        // Remove all the selected items
        buttonRemove.setOnClickListener(v -> {
            if(mainCardsAdapter.getCheckedItems().isEmpty()) return;
            // Make alert confirmation
            new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete the selected items?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    databaseManager.deleteItems(mainCardsAdapter.getCheckedItems(), getApplicationContext());
                    mainCardsAdapter.clearCheckedItems();
                }).setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
        });


        // Dialog to generate report
        reportDialog = new ReportDialog(this);
        Button reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Environment.isExternalStorageManager()) { requestStoragePermission(); }
                reportDialog.show(itemDataSet);
            }
        });

    }


    public void switchAdminStatus(boolean setAdmin) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("admin_status", setAdmin);
        finish();
        startActivity(intent);
    }

    private void requestStoragePermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
            startActivityForResult(intent, 2296);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, 2296);
        }
    }

}


