package com.example.taam;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginModel {
    private final FirebaseAuth auth;

    public LoginModel() {
        this.auth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> loginQuery(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }
}
