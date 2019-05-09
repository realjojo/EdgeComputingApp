package com.edgecomputing.application;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

/**
 * @Author: jojo
 * @Date: Created on 2019/4/20 15:53
 */
public class MainApplication extends Application {

    private String userName;
    private String password;
    private String token;
    private boolean isLogin;
    private boolean isRememberPwd;
    private boolean isReg2Server;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = this.getSharedPreferences("EdgeComputing", Activity.MODE_PRIVATE);
        if(!sharedPreferences.contains("defaultLogin")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("defaultLogin", false);
            editor.commit();
        }
    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean("defaultLogin", false);
    }

    public void setLogin(boolean login) {
        isLogin = login;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("defaultLogin", login);
        editor.commit();
    }

    public boolean isReg2Server() {
        return sharedPreferences.getBoolean("reg2Server", false);
    }

    public void setReg2Server(boolean reg2Server) {
        isReg2Server = reg2Server;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("reg2Server", reg2Server);
        editor.commit();
    }

    public void setUserName(String userName) {
        this.userName = userName;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName);
        editor.commit();
    }

    public void setPassword(String password) {
        this.password = password;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password", password);
        editor.commit();
    }

    public void setToken(String token) {
        this.token = token;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.commit();
    }

    public void setRememberPwd(boolean rememberPwd) {
        isRememberPwd = rememberPwd;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isRememberPwd", rememberPwd);
        editor.commit();
    }

    public String getUserName() {
        return sharedPreferences.getString("userName", null);
    }

    public String getPassword() {
        return sharedPreferences.getString("password", null);
    }

    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    public boolean isRememberPwd() {
        return sharedPreferences.getBoolean("isRememberPwd", false);
    }

    public void setLoginUserInfo(String userName, String token) {
        this.userName = userName;
        this.token = token;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName);
        editor.putString("token", token);
        editor.commit();
    }
}
