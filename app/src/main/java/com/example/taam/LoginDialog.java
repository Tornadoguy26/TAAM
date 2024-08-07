package com.example.taam;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taam.structures.User;

import java.util.Objects;

public class LoginDialog {

    private MainActivity mainActivity;
    private Dialog logindialog;
    private TextView loginStatus;

    public LoginDialog(MainActivity activity) {
        this.mainActivity = activity;

        logindialog = new Dialog(activity);
        logindialog.setContentView(R.layout.login_screen);
        Objects.requireNonNull(logindialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        logindialog.setCancelable(false);

        Button adminCancelBtn = logindialog.findViewById(R.id.BackButton);
        Button adminLoginBtn = logindialog.findViewById(R.id.LogButton);

        EditText userEditText = logindialog.findViewById(R.id.LogUsername);
        EditText passwordEditText = logindialog.findViewById(R.id.LogPassword);
        CheckBox passwordVisibility = logindialog.findViewById(R.id.PasswordVis);

        loginStatus = logindialog.findViewById(R.id.LogIncorrect);

        LoginPresenter loginPresenter = new LoginPresenter(this, DatabaseManager.getInstance());

        adminCancelBtn.setOnClickListener(v -> {
            userEditText.setText("");
            passwordEditText.setText("");
            passwordVisibility.setChecked(false);
            loginStatus.setText("");
            logindialog.dismiss();
        });

        adminLoginBtn.setOnClickListener(v -> {
            String email = userEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            User user = new User(email, password);
            loginPresenter.login(user);
        });

        passwordVisibility.setOnCheckedChangeListener((v, flag) -> {
            if(flag){
                passwordEditText.setTransformationMethod(null);
            } else {
                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
            }
        });
    }

    public void onLoginSuccess(){
        loginStatus.setText("Login Successful");
        loginStatus.setTextColor(Color.GREEN);
    }

    public void onLoginFailure(){
        loginStatus.setText("Login Failed: invalid credentials");
        loginStatus.setTextColor(Color.RED);
    }

    public void switchAdminStatus(boolean status) {
        logindialog.dismiss();
        mainActivity.switchAdminStatus(status);
    }

    public void show() {
        logindialog.show();
    }

}
