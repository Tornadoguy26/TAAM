package com.example.taam;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;
import android.Manifest;


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

// For requesting permissions
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class MainActivity extends AppCompatActivity {

    // LOGIN =================
    private EditText auser, apassword;

    private Dialog logindialog;
    private MainPresenter mainPresenter;
    // =======================

    private boolean isAdmin;

    private ArrayList<Item> itemDataSet;
    private MainCardsAdapter cardsAdapter;

    // GENERATE REPORT =================
    private static final int PERMISSION_REQUEST_CODE = 786;
    private Spinner reportSpinner;
    private EditText reportSearch;
    private CheckBox reportCheckBox;

    private MainCardsAdapter mainCardsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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
        mainPresenter = new MainPresenter(this);

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
            mainPresenter.login(user);
        });

        togglevis.setOnCheckedChangeListener((v, flag) -> {
            if(flag){
                apassword.setTransformationMethod(null);
            } else {
                apassword.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        Button b = findViewById(R.id.addButton);
        b.setOnClickListener(v -> {
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

        Button reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog reportDialog = new Dialog(MainActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View reportView = inflater.inflate(R.layout.report_layout, null);
                reportDialog.setContentView(reportView);

                Button reportCancel = reportView.findViewById(R.id.reportCancelButton);
                reportCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reportDialog.dismiss();
                    }
                });

                reportSearch = reportView.findViewById(R.id.reportSearchInput);

                reportCheckBox = reportView.findViewById(R.id.reportCheckBox);

                TextView reportCheckDesc = reportView.findViewById(R.id.reportCheckBoxDesc);

                reportSpinner = reportView.findViewById(R.id.reportSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                        R.array.report_options, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                reportSpinner.setAdapter(adapter);
                reportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        if (!selectedItem.equals("Select All Items")) {
                            reportSearch.setVisibility(View.VISIBLE);
                            reportSearch.setHint("Enter " + selectedItem.toLowerCase());
                        } else {
                            reportSearch.setVisibility(View.GONE);
                        }
                        if (selectedItem.equals("Lot Number") || selectedItem.equals("Name")) {
                            reportCheckDesc.setVisibility(View.GONE);
                            reportCheckBox.setChecked(false);
                            reportCheckBox.setVisibility(View.GONE);
                        } else {
                            reportCheckDesc.setVisibility(View.VISIBLE);
                            reportCheckBox.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });

                Button reportGenerate = reportView.findViewById(R.id.reportGenerateButton);
                reportGenerate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Permission is not granted
                            Log.d("PermissionDebug", "Requesting permission");
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_REQUEST_CODE);
                            Log.d("PermissionDebug", "Requested permission");
                        } else {
                            mainPresenter.generateReport(reportSpinner.getSelectedItem().toString(),
                                    reportSearch.getText().toString(), reportCheckBox.isChecked(), itemDataSet);
                        }
                    }
                });
                reportDialog.show();
            }
        });

    }

    public void ReportPopUp() {

        Button viewBtn = findViewById(R.id.viewButton);
        viewBtn.setOnClickListener(view -> {
            if (mainCardsAdapter.getCheckedItems().isEmpty()) { return; }
            Intent intent = new Intent(MainActivity.this, ViewItemActivity.class);
            intent.putExtra("checkedItems", mainCardsAdapter.getCheckedItems());
            Log.d("[TAAM]", "Passing array: " + mainCardsAdapter.getCheckedItems().size());
            startActivity(intent);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PermissionDebug", "Request code: " + requestCode);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionDebug", "Permission granted");
                mainPresenter.generateReport(reportSpinner.getSelectedItem().toString(),
                        reportSearch.getText().toString(), reportCheckBox.isChecked(), itemDataSet);
            } else {
                Log.d("PermissionDebug", "Permission denied");
                Toast.makeText(this, "Permission denied. Cannot create PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
