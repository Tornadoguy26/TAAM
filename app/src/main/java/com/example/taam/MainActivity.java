package com.example.taam;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button adminBTN;
    Dialog logindialog;
    Button adminCancelBTN, adminLoginBTN;

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

        adminBTN = findViewById(R.id.AdminLogin);

        logindialog = new Dialog(this);
        logindialog.setContentView(R.layout.login_screen);
        logindialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        logindialog.setCancelable(false);

        adminCancelBTN = logindialog.findViewById(R.id.BackButton);
        adminLoginBTN = logindialog.findViewById(R.id.LogButton);

        adminBTN.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                logindialog.show();
            }
        });

        adminCancelBTN.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v){
               logindialog.dismiss();
           }
        });

        adminLoginBTN.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });
    }
}