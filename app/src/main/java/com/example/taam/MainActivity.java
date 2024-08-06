package com.example.taam;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import android.annotation.SuppressLint;
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



public class MainActivity extends AppCompatActivity {

    // LOGIN =================
    private EditText auser, apassword;

    private Dialog logindialog;
    private LoginPresenter loginPresenter;
    // =======================

    // SEARCH =================
    private EditText slotnum, sname;

    private Dialog searchdialog;

    String[] categories = {"Jade", "Paintings", "Calligraphy", "Rubbings", "Bronze",
            "Brass and Copper", "Gold and Silvers", "Lacquer", "Enamels"};
    String[] periods = {"Xia", "Shang", "Zhou", "Chuanqiu", "Zhanggou", "Qin", "Han",
            "Shangou", "Ji", "South and North", "Shui", "Tang", "Liao", "Song", "Jin",
            "Yuan", "Ming", "Qing", "Modern"};

    AutoCompleteTextView scategory;
    ArrayAdapter<String> adapterItems;

    AutoCompleteTextView speriod;
    ArrayAdapter<String> adapterItems2;

    // =======================

    private boolean isAdmin;

    private ArrayList<Item> itemDataSet;
    private CardsAdapter cardsAdapter;


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

        // LOGIN ==================================================================================
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

        if (isAdmin) adminBTN.setText("BACK");
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
            searchdialog.show();
        });


        scategory = searchdialog.findViewById(R.id.auto_complete_txt);
        adapterItems = new ArrayAdapter<String>(this, R.layout.search_list, categories);

        scategory.setAdapter(adapterItems);

        scategory.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(MainActivity.this, "Item: " + item, Toast.LENGTH_SHORT).show();
            }
        });

        speriod = searchdialog.findViewById(R.id.auto_complete_txt2);
        adapterItems2 = new ArrayAdapter<String>(this, R.layout.search_list, periods);

        speriod.setAdapter(adapterItems2);

        speriod.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                String item2 = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(MainActivity.this, "Item: " + item2, Toast.LENGTH_SHORT).show();
            }
        });

        slotnum = searchdialog.findViewById(R.id.auto_complete_txt00);
        sname = searchdialog.findViewById(R.id.auto_complete_txt0);

        searchCancelBTN.setOnClickListener(v -> {
            slotnum.setText("");
            sname.setText("");
            scategory.setText("");
            speriod.setText("");
            searchdialog.dismiss();
        });

        searchResultBTN.setOnClickListener(v -> {

        });

        // =========================================================================================

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
        cardsAdapter = new CardsAdapter(itemDataSet);
        recyclerView.setAdapter(cardsAdapter);

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
