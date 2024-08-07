package com.example.taam;

import com.example.taam.structures.User;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPresenter {
    private final MainActivity mainActivity;
    private final LoginModel loginModel;

    public LoginPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.loginModel = new LoginModel();
    }

    public void login(User user) {
        if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            mainActivity.onLoginFailure();
            return;
        }

        loginModel.loginQuery(user.getEmail(), user.getPassword()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mainActivity.onLoginSuccess();
                mainActivity.switchAdminStatus(true);
            } else {
                mainActivity.onLoginFailure();
            }
        });
    }
}