package com.edgecomputing.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.MapView;
import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.OkHttpUtil;

import java.util.HashMap;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/21 11:37
 */
public class TaskActivity extends AppCompatActivity {

    private static final String TAG = "TaskActivity";

    private TextView tv_task, tv_car, tv_prisonerNo, tv_prisonerName, tv_policeNo, tv_policeName, tv_taskDetail, tv_taskLevel;
    private String selectServerAddress;
    private MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(TaskActivity)");
        setContentView(R.layout.activity_task);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("押解任务详情");
        mainApplication = (MainApplication) getApplication();
        selectServerAddress = mainApplication.getServerAddress();
        if(selectServerAddress == null) {
            selectServerAddress = "http://10.109.246.55:8089";
        }
        init();
    }

    private void init() {
        tv_task = (TextView) findViewById(R.id.task_no);
        tv_car = (TextView) findViewById(R.id.task_car_no);
        tv_policeNo = (TextView) findViewById(R.id.task_police_no);
        tv_policeName = (TextView) findViewById(R.id.task_police_name);
        tv_prisonerNo = (TextView) findViewById(R.id.task_prisoner_no);
        tv_prisonerName = (TextView) findViewById(R.id.task_prisoner_name);
        tv_taskLevel = (TextView) findViewById(R.id.task_level);
        tv_taskDetail = (TextView) findViewById(R.id.task_detail);
    }

    private void getTaskInfo() {
        HashMap<String, String> params = new HashMap<>();

        OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerAddress, "task/get", OkHttpUtil.TYPE_GET, params, new OkHttpUtil.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {

            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.e(TAG, errorMsg);
            }
        });
    }
}
