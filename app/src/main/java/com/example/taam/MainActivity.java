package com.example.taam;


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

import android.annotation.SuppressLint;
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
