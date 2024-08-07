package com.example.taam;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taam.structures.User;

import java.util.Objects;

public class LoginDialog {

    private final MainActivity mainActivity;
    private final Dialog loginDialog;
    private final TextView loginStatus;

    public LoginDialog(MainActivity activity) {
        this.mainActivity = activity;

        loginDialog = new Dialog(activity);
        loginDialog.setContentView(R.layout.login_screen);
        Objects.requireNonNull(loginDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loginDialog.setCancelable(false);

        Button adminCancelBtn = loginDialog.findViewById(R.id.BackButton);
        Button adminLoginBtn = loginDialog.findViewById(R.id.LogButton);

        EditText userEditText = loginDialog.findViewById(R.id.LogUsername);
        EditText passwordEditText = loginDialog.findViewById(R.id.LogPassword);
        CheckBox passwordVisibility = loginDialog.findViewById(R.id.PasswordVis);

        loginStatus = loginDialog.findViewById(R.id.LogIncorrect);

        LoginPresenter loginPresenter = new LoginPresenter(this, DatabaseManager.getInstance());

        adminCancelBtn.setOnClickListener(v -> {
            userEditText.setText("");
            passwordEditText.setText("");
            passwordVisibility.setChecked(false);
            loginStatus.setText("");
            loginDialog.dismiss();
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
        loginDialog.dismiss();
        mainActivity.switchAdminStatus(status);
    }

    public void show() {
        loginDialog.show();
    }

}
