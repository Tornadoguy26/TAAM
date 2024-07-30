package com.example.taam;

import com.example.taam.structures.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPresenter {
    private final MainActivity mainActivity;
    private final FirebaseAuth auth;

    public LoginPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.auth = FirebaseAuth.getInstance();
    }

    public void login(User user) {
        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mainActivity.onLoginSuccess();
                        mainActivity.switchAdminStatus(true);
                    } else {
                        mainActivity.onLoginFailure();
                    }
                });
    }
}