package com.edgecomputing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
 * @Date: Created on 2019/4/21 20:57
 */
public class RegisterActivity extends AppCompatActivity {

    private Button registerBtn;
    private EditText userNameEt, pwdEt, confirmPwdEt, userIdEt, userIdCardEt;
    private ImageView userNameClear, pwdClear, confirmPwdClear, userIdClear, userIdCardClear;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // 去除顶部标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        init();
    }

    public void init() {
        registerBtn = (Button) findViewById(R.id.btn_register);
        userNameEt = (EditText) findViewById(R.id.et_userName1);
        pwdEt = (EditText) findViewById(R.id.et_password1);
        confirmPwdEt = (EditText) findViewById(R.id.et_confirmPassword1);
        userNameClear = (ImageView) findViewById(R.id.iv_unameClear1);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear1);
        confirmPwdClear = (ImageView) findViewById(R.id.iv_confirmPwdClear1);
        userIdEt = (EditText) findViewById(R.id.et_userId);
        userIdClear = (ImageView) findViewById(R.id.iv_idClear1);
        userIdCardEt = (EditText) findViewById(R.id.et_userIdCard);
        userIdCardClear = (ImageView) findViewById(R.id.iv_idCardClear1);

        EditTextClearTool.addClearListener(userNameEt,userNameClear);
        EditTextClearTool.addClearListener(pwdEt,pwdClear);
        EditTextClearTool.addClearListener(confirmPwdEt, confirmPwdClear);
        EditTextClearTool.addClearListener(userIdEt, userIdClear);
        EditTextClearTool.addClearListener(userIdCardEt, userIdCardClear);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<>(5);
                params.put("userName", userNameEt.getText().toString());
                params.put("userId", userIdEt.getText().toString());
                params.put("userIdCard", userIdCardEt.getText().toString());
                params.put("password", pwdEt.getText().toString());
                params.put("confirmPwd", confirmPwdEt.getText().toString());
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn("users/register", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        if(JSON.parseObject(result).getInteger("code") == 200) {
                            JSONObject jsonObj = JSON.parseObject(result).getJSONObject("data");
                            String userName = jsonObj.getString("userName");
                            String password = jsonObj.getString("password");
                            String userId = jsonObj.getString("userId");
                            String idCard = jsonObj.getString("idCard");
                            MainApplication mainApplication = (MainApplication) getApplication();
                            mainApplication.setLoginUserInfo(userName, "", "", userId, idCard);
                            Intent intent = new Intent();
                            intent.setClass(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("userName", userName);
                            intent.putExtra("password", password);
                            setResult(1, intent);
                            finish();
                        } else {
                            Toast.makeText(getBaseContext(), JSON.parseObject(result).getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                    }
                });
            }
        });
    }

}
