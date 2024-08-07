package com.example.taam;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.net.Uri;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
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
import android.provider.Settings;

import android.util.Log;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;


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
    private PdfPresenter pdfPresenter;
    // =======================

    // SEARCH =================

    private boolean isSearch = false;
    private EditText slotnum, sname;

    private Dialog searchdialog;
    ArrayList<Item> items = new ArrayList<>();

    String[] categories = {"Clear", "Jade", "Paintings", "Calligraphy", "Rubbings", "Bronze",
            "Brass and Copper", "Gold and Silvers", "Lacquer", "Enamels"};
    String[] periods = {"Clear", "Xia", "Shang", "Zhou", "Chuanqiu", "Zhanggou", "Qin", "Han",
            "Shangou", "Ji", "South and North", "Shui", "Tang", "Liao", "Song", "Jin",
            "Yuan", "Ming", "Qing", "Modern"};

    AutoCompleteTextView scategory;
    ArrayAdapter<String> categoryItems;

    AutoCompleteTextView speriod;
    ArrayAdapter<String> periodItems;

    private DatabaseReference mDatabase;

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

        // Database Manager Instance
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        isAdmin = getIntent().getBooleanExtra("admin_status", false);
        Log.d("[TAAM]", "isAdmin: " + isAdmin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // LOGIN ==================================================================================
        Button adminBTN = findViewById(R.id.adminLoginButton);

        logindialog = new Dialog(this);
        logindialog.setContentView(R.layout.login_screen);
        logindialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        logindialog.setCancelable(false);

        Button adminCancelBTN = logindialog.findViewById(R.id.BackButton);
        Button adminLoginBTN = logindialog.findViewById(R.id.LogButton);
        loginPresenter = new LoginPresenter(this, new LoginModel());
        pdfPresenter = new PdfPresenter(this);

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
        // =========================================================================================

        // SEARCH ==================================================================================

        Button searchBTN = findViewById(R.id.searchButton);

        searchdialog = new Dialog(this);
        searchdialog.setContentView(R.layout.search_input);
        searchdialog.getWindow().setBackgroundDrawable((new ColorDrawable(Color.TRANSPARENT)));
        searchdialog.setCancelable(false);

        Button searchCancelBTN = searchdialog.findViewById(R.id.BackButton);
        Button searchResultBTN = searchdialog.findViewById(R.id.ResultButton);

        searchBTN.setOnClickListener(v -> {
            if(isSearch){
                searchRestart();
                isSearch=false;
            }
            searchdialog.show();
        });

        scategory = searchdialog.findViewById(R.id.LotCategory);
        categoryItems = new ArrayAdapter<>(this, R.layout.search_list, categories);

        scategory.setAdapter(categoryItems);

        scategory.setOnItemClickListener((adapterView, view, i, l) -> {
            String item = adapterView.getItemAtPosition(i).toString();
            if((scategory.getText().toString()).equals("Clear")){
                scategory.setText("");
                scategory.setHint("Select Category");
            }
            Toast.makeText(MainActivity.this, "Item: " + item, Toast.LENGTH_SHORT).show();
        });

        speriod = searchdialog.findViewById(R.id.LotPeriod);
        periodItems = new ArrayAdapter<>(this, R.layout.search_list, periods);

        speriod.setAdapter(periodItems);

        speriod.setOnItemClickListener((adapterView, view, i, l) -> {
            String item2 = adapterView.getItemAtPosition(i).toString();
            if((speriod.getText().toString()).equals("Clear")){
                speriod.setText("");
                speriod.setHint("Select Period");
            }
            Toast.makeText(MainActivity.this, "Item: " + item2, Toast.LENGTH_SHORT).show();
        });

        slotnum = searchdialog.findViewById(R.id.LotNum);
        sname = searchdialog.findViewById(R.id.LotName);

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
                    Intent intent = new Intent(MainActivity.this, ViewItemActivity.class);
                    intent.putExtra("checkedItems", items);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", databaseError.getMessage());
                }
            });
            searchdialog.dismiss();
        });

        // =========================================================================================

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
                        if (checkPermission()) {
                            // Permission is not granted
                            Log.d("PermissionDebug", "Requesting permission");
                            requestPermission();
                            Log.d("PermissionDebug", "Requested permission");
                        } else {
                            pdfPresenter.generateReport(reportSpinner.getSelectedItem().toString(),
                                    reportSearch.getText().toString(), reportCheckBox.isChecked(), itemDataSet);
                        }
                    }
                });
                reportDialog.show();
            }
        });

    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
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

    public void removeLot(@NonNull ArrayList<Item> items, int x, boolean state){
        if(!state) {
            items.removeIf(item -> x != item.getLotNumber());
        }
    }
    public void removeName(@NonNull ArrayList<Item> items, String x){
        if(!x.isEmpty()) {
            items.removeIf(item -> !x.equals(item.getName()));
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

        if (slotnum != null) {
            slotnum.clearFocus();
        }

        if (sname != null) {
            sname.clearFocus();
        }

        if (scategory != null) {
            scategory.clearFocus();
        }

        if (speriod != null) {
            speriod.clearFocus();
        }

        scategory.setAdapter(categoryItems);
        speriod.setAdapter(periodItems);
        searchdialog.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PermissionDebug", "Request code: " + requestCode);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionDebug", "Permission granted");
                pdfPresenter.generateReport(reportSpinner.getSelectedItem().toString(),
                        reportSearch.getText().toString(), reportCheckBox.isChecked(), itemDataSet);
            } else {
                Log.d("PermissionDebug", "Permission denied");
                Toast.makeText(this, "Permission denied. Cannot create PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


