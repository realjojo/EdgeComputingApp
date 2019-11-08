package com.edgecomputing.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.EditTextClearTool;
import com.edgecomputing.utils.OkHttpUtil;

import java.util.HashMap;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/7 10:42
 */
public class LoginActivity extends AppCompatActivity {

    private TextView registerTv;
    private Button loginBtn;
    private EditText userNameEt, pwdEt, prisonerIdEt;
    private ImageView userNameClear, pwdClear, idClear;
    private CheckBox rememberPwd;
    private boolean isRememberPwd;
    private MainApplication mainApplication;
    private String deviceNo;
    private TelephonyManager tm;
    private boolean logout;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 去除顶部标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        mainApplication = (MainApplication) getApplication();
        Intent intent = getIntent();
        logout = intent.getBooleanExtra("logout", false);
        init();
    }

    public void init() {
        userNameEt = (EditText) findViewById(R.id.et_userName);
        pwdEt = (EditText) findViewById(R.id.et_password);
        prisonerIdEt = (EditText) findViewById(R.id.et_prisonerId);
        userNameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        idClear = (ImageView) findViewById(R.id.iv_idClear);
        loginBtn = (Button) findViewById(R.id.btn_login);
        registerTv = (TextView) findViewById(R.id.tv_register);
        rememberPwd = (CheckBox) findViewById(R.id.cb_checkbox);

        EditTextClearTool.addClearListener(userNameEt,userNameClear);
        EditTextClearTool.addClearListener(pwdEt,pwdClear);
        EditTextClearTool.addClearListener(prisonerIdEt, idClear);

        if(mainApplication.getUserName() != null) {
            userNameEt.setText(mainApplication.getUserName());
            prisonerIdEt.setText(mainApplication.getPrisonerId());
            if(mainApplication.isRememberPwd()) {
                rememberPwd.setChecked(true);
                isRememberPwd = true;
                pwdEt.setText(mainApplication.getPassword());
            }
        }

        deviceNo = mainApplication.getDeviceNo();
        if(deviceNo == null){
            tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                } else {
                    deviceNo = tm.getDeviceId();
                    mainApplication.setDeviceNo(deviceNo);
                }
            }
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginRequest();
            }
        });

        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        rememberPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    isRememberPwd = true;
                } else {
                    isRememberPwd = false;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //如果没有获取权限，那么可以提示用户去设置界面--->应用权限开启权限
                    Toast.makeText(getApplicationContext(), "获取权限失败", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        deviceNo = tm.getDeviceId();
                        if (deviceNo != null) {
                            mainApplication.setDeviceNo(deviceNo);
                        }
                    }
                }
            }
        }
    }

    private void loginRequest() {
        HashMap<String, String> pp = new HashMap<>(1);
        pp.put("deviceNo", mainApplication.getDeviceNo());
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn("dvcM/devices/judgeDeviceNo", OkHttpUtil.TYPE_GET, pp, new OkHttpUtil.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                if(result.equals("true")){
                    if(!mainApplication.isReg2Server()){
                        mainApplication.setReg2Server(true);
                    }
                    HashMap<String, String> params = new HashMap<>(3);
                    params.put("userName", userNameEt.getText().toString());
                    params.put("prisonerId", prisonerIdEt.getText().toString());
                    params.put("password", pwdEt.getText().toString());
                    OkHttpUtil.getInstance(getBaseContext()).requestAsyn("usrM/users/login", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
                        @Override
                        public void onReqSuccess(String result) {
                            if(JSON.parseObject(result).getInteger("code") == 200) {
                                JSONObject jsonObj = JSON.parseObject(result).getJSONObject("data");
                                String token = jsonObj.getString("loginToken");
                                String userName = jsonObj.getString("userName");
                                String prisonerId = jsonObj.getString("prisonerId");
                                String userId = jsonObj.getString("userId");
                                String idCard = jsonObj.getString("idCard");
                                mainApplication.setLogin(true);
                                mainApplication.setLoginUserInfo(userName, token, prisonerId, userId, idCard);
                                if(isRememberPwd) {
                                    mainApplication.setPassword(pwdEt.getText().toString());
                                    mainApplication.setRememberPwd(true);
                                } else {
                                    mainApplication.setRememberPwd(false);
                                }
                                if(logout){
                                    setResult(2);
                                    finish();
                                }else {
                                    setResult(1);
                                    finish();
                                }
                            } else {
                                Toast.makeText(getBaseContext(), JSON.parseObject(result).getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onReqFailed(String errorMsg) {
                            Log.e(TAG, errorMsg);
                            Toast.makeText(getBaseContext(), "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    mainApplication.setReg2Server(false);
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, Register2ServerActivity.class);
                    startActivityForResult(intent,2);
                }
            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.e(TAG, errorMsg);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult(MainActivity)");
        if(resultCode == 1) {
            userNameEt.setText(data.getStringExtra("userName"));
            prisonerIdEt.setText(data.getStringExtra("prisonerId"));
            pwdEt.setText(data.getStringExtra("password"));
        }else if(resultCode == 2){

        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed(LoginActivity)");
        if(logout) {
            setResult(4);
            finish();
        }else {
            setResult(3);
            finish();
        }
    }
}
