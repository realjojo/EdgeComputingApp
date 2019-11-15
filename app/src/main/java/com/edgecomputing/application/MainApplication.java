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
    private String userId;
    private String idCard;
    private String password;
    private String token;
    private String prisonerId;
    private boolean isLogin;
    private boolean isRememberPwd;
    private boolean isReg2Server;
    private String deviceNo;
    private String braceletNo;

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

    public void setLoginUserInfo(String userName, String token, String userId, String idCard) {
        this.userName = userName;
//        this.prisonerId = prisonerId;
        this.token = token;
        this.userId = userId;
        this.idCard = idCard;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName);
        editor.putString("userId", userId);
        editor.putString("idCard", idCard);
//        editor.putString("prisonerId", prisonerId);
        editor.putString("token", token);
        editor.commit();
    }

    public String getPrisonerId() {
        return sharedPreferences.getString("prisonerId", null);
    }

    public void setPrisonerId(String prisonerId) {
        this.prisonerId = prisonerId;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("prisonerId", prisonerId);
        editor.commit();
    }

    public String getDeviceNo() {
        return sharedPreferences.getString("deviceNo", null);
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deviceNo", deviceNo);
        editor.commit();
    }

    public String getUserId() {
        return sharedPreferences.getString("userId", null);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.commit();
    }

    public String getIdCard() {
        return sharedPreferences.getString("idCard", null);
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("idCard", idCard);
        editor.commit();
    }

    public String getBraceletNo() {
        return sharedPreferences.getString("bracelet", null);
    }

    public void setBraceletNo(String braceletNo) {
        this.braceletNo = braceletNo;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("bracelet", braceletNo);
        editor.commit();
    }
}
