package com.vocaby.app.models;

import android.text.TextUtils;
import android.util.Patterns;

public class AuthModel {
    private String email;
    private String password;
    private String confirmationPassword;

    public AuthModel() {
        this.email = "";
        this.password = "";
        this.confirmationPassword = "";
    }

    public AuthModel(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthModel(String email, String password, String confirmationPassword) {
        this.email = email;
        this.password = password;
        this.confirmationPassword = confirmationPassword;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getConfirmationPassword() {
        return this.confirmationPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmailAndPassword(String email, String password) {
        setEmail(email);
        setPassword(password);
    }

    public void setConfirmationPassword(String confirmationPassword) {
        this.confirmationPassword = confirmationPassword;
    }

    public boolean isEmail() {
        return (!TextUtils.isEmpty(this.email) && Patterns.EMAIL_ADDRESS.matcher(this.email).matches());
    }

    public boolean emailIsEmpty() {
        return this.email.isEmpty();
    }

    public boolean emailIsValid() {
        return isEmail() && !emailIsEmpty();
    }

    public boolean passwordIsEmpty() {
        return this.password.isEmpty();
    }

    public boolean confirmationPasswordIsEmpty() { return this.confirmationPassword.isEmpty(); }

    public boolean passwordsMatch() {return !passwordIsEmpty() && this.password.equals(this.confirmationPassword); }

    public boolean loginIsValid() {
        return emailIsValid() && !passwordIsEmpty();
    }

    public boolean registrationIsValid() {
        return emailIsValid() && !confirmationPasswordIsEmpty() && passwordsMatch();
    }
}
