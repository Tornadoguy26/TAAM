package com.example.taam;

import com.example.taam.structures.User;

public class LoginPresenter {
    private final LoginDialog loginDialog;
    private final DatabaseManager model;

    public LoginPresenter(LoginDialog loginDialog, DatabaseManager model) {
        this.loginDialog = loginDialog;
        this.model = model;
    }

    public void login(User user) {
        if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            loginDialog.onLoginFailure();
            return;
        }

        model.loginQuery(user.getEmail(), user.getPassword()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loginDialog.onLoginSuccess();
                loginDialog.switchAdminStatus(true);
            } else {
                loginDialog.onLoginFailure();
            }
        });
    }
}