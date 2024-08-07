package com.example.taam;

import com.example.taam.structures.User;

public class LoginPresenter {
    private final MainActivity mainActivity;
    private final LoginModel loginModel;

    public LoginPresenter(MainActivity mainActivity, LoginModel loginModel) {
        this.mainActivity = mainActivity;
        this.loginModel = loginModel;
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