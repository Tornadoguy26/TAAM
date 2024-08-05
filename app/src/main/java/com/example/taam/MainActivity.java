package com.example.taam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.example.taam.structures.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    // LOGIN =================
    private EditText auser, apassword;

    private Dialog logindialog;
    private LoginPresenter loginPresenter;
    // =======================

    private boolean isAdmin;

    private ArrayList<Item> itemDataSet;
    private MainCardsAdapter mainCardsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Database Manager Instance
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        isAdmin = getIntent().getBooleanExtra("admin_status", false);
        Log.d("[TAAM]", "isAdmin: " + isAdmin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button adminBTN = findViewById(R.id.adminLoginButton);

        logindialog = new Dialog(this);
        logindialog.setContentView(R.layout.login_screen);
        logindialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        logindialog.setCancelable(false);

        Button adminCancelBTN = logindialog.findViewById(R.id.BackButton);
        Button adminLoginBTN = logindialog.findViewById(R.id.LogButton);
        loginPresenter = new LoginPresenter(this);

        auser = logindialog.findViewById(R.id.LogUsername);
        apassword = logindialog.findViewById(R.id.LogPassword);
        CheckBox togglevis = logindialog.findViewById(R.id.PasswordVis);

        TextView titleText = findViewById(R.id.titleTextView);
        if (isAdmin) titleText.setText(R.string.admin_screen_title);

        LinearLayout adminLayout = findViewById(R.id.adminFeaturesLayout);
        for (int i = 0; i < adminLayout.getChildCount(); i++) {
            View child = adminLayout.getChildAt(i);
            child.setEnabled(isAdmin);
        }

        if (isAdmin) adminBTN.setText(R.string.back_text);
        adminBTN.setOnClickListener(v -> {
            if (isAdmin) { switchAdminStatus(false); }
            else { logindialog.show(); }
        });

        adminCancelBTN.setOnClickListener(v -> {
            auser.setText("");
            apassword.setText("");
            togglevis.setChecked(false);
            TextView loginStatus = logindialog.findViewById(R.id.LogIncorrect);
            loginStatus.setText("");
            logindialog.dismiss();
        });

        adminLoginBTN.setOnClickListener(v -> {
            String email = auser.getText().toString().trim();
            String password = apassword.getText().toString().trim();
            User user = new User(email, password);
            loginPresenter.login(user);
        });

        togglevis.setOnCheckedChangeListener((v, flag) -> {
            if(flag){
                apassword.setTransformationMethod(null);
            } else {
                apassword.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        Button buttonAdd = findViewById(R.id.addButton);
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });


        FirebaseDatabase db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
        DatabaseReference dbRef = db.getReference("Items");

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemDataSet = new ArrayList<>();
        mainCardsAdapter = new MainCardsAdapter(itemDataSet);
        recyclerView.setAdapter(mainCardsAdapter);

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
                    mainCardsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });

        Button viewBtn = findViewById(R.id.viewButton);
        viewBtn.setOnClickListener(view -> {
            if (mainCardsAdapter.getCheckedItems().isEmpty()) { return; }
            Intent intent = new Intent(MainActivity.this, ViewItemActivity.class);
            intent.putExtra("checkedItems", mainCardsAdapter.getCheckedItems());
            Log.d("[TAAM]", "Passing array: " + mainCardsAdapter.getCheckedItems().size());
            startActivity(intent);
        });

        Button buttonRemove = findViewById(R.id.removeButton);
        buttonRemove.setOnClickListener(v -> {
            if(mainCardsAdapter.getCheckedItems().size() == 0) return;
            // make pop up confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete the selected items?")
            // database
            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                databaseManager.deleteItems(mainCardsAdapter.getCheckedItems());
            }).setNegativeButton(android.R.string.no, (dialog, which) -> {
                // User cancelled, do nothing
                dialog.dismiss();
            })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    public void onLoginSuccess(){
        TextView loginStatus = logindialog.findViewById(R.id.LogIncorrect);
        loginStatus.setText("Login Successful");
        loginStatus.setTextColor(Color.GREEN);
    }

    public void onLoginFailure(){
        TextView loginStatus = logindialog.findViewById(R.id.LogIncorrect);
        loginStatus.setText("Login Failed: invalid credentials");
        loginStatus.setTextColor(Color.RED);
    }

    public void switchAdminStatus(boolean setAdmin) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("admin_status", setAdmin);
        logindialog.dismiss(); finish();
        startActivity(intent);
    }


}
