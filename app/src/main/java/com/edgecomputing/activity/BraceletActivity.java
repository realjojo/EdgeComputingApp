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

import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.EditTextClearTool;
import com.edgecomputing.utils.OkHttpUtil;

import java.util.HashMap;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/15 11:11
 */
public class BraceletActivity extends AppCompatActivity {
    private static final String TAG = "BraceletActivity";

    private Button connectBtn;
    private EditText macAddressEt, prisonerIdEt;
    private ImageView prisonerIdClear;
    private String macAddress;
    private String selectServerAddress;
    private boolean isBind = false;

    private MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet);
        // 去除顶部标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        Intent intent = getIntent();
        macAddress = intent.getStringExtra("macAddress");
        mainApplication = (MainApplication) getApplication();
        selectServerAddress = mainApplication.getServerAddress();
        if(selectServerAddress == null) {
            selectServerAddress = "http://10.109.246.55:8089";
        }
        init();
    }

    public void init() {
        macAddressEt = (EditText) findViewById(R.id.et_mac_address);
        prisonerIdEt = (EditText) findViewById(R.id.et_prisonerId);
        prisonerIdClear = (ImageView) findViewById(R.id.iv_prisonerIdClear);
        connectBtn = (Button) findViewById(R.id.btn_bindBracelet);

        EditTextClearTool.addClearListener(prisonerIdEt, prisonerIdClear);

        macAddressEt.setText(macAddress);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(macAddress != null && !macAddressEt.equals("")) {
                    HashMap<String, String> pp = new HashMap<>(2);
                    pp.put("braceletNo", macAddressEt.getText().toString());
                    pp.put("prisonerId", prisonerIdEt.getText().toString());
                    OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerAddress, "devices/braceletBindPrisoner", OkHttpUtil.TYPE_POST_FORM, pp, new OkHttpUtil.ReqCallBack<String>() {
                        @Override
                        public void onReqSuccess(String result) {
                            Log.i(TAG, result);
                            if(result.equals("绑定成功")) {
                                isBind = true;
                                Toast.makeText(getBaseContext(), "手环绑定成功", Toast.LENGTH_SHORT).show();
                                MainApplication mainApplication = (MainApplication) getApplication();
                                mainApplication.setPrisonerId(prisonerIdEt.getText().toString());
                                mainApplication.setBraceletNo(macAddressEt.getText().toString());
                                setResult(1);
                                finish();
                            }else {
                                Toast.makeText(getBaseContext(), "手环绑定失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onReqFailed(String errorMsg) {
                            Log.e(TAG, errorMsg);
                            Toast.makeText(getBaseContext(), "手环绑定失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed(BraceletActivity)");
        if(!isBind) {
            setResult(2);
            finish();
        }else {
            setResult(1);
            finish();
        }
    }
}
