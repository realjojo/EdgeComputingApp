package com.edgecomputing.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.OkHttpUtil;

import java.util.HashMap;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/21 12:18
 */
public class PrisonerActivity extends AppCompatActivity {

    private static final String TAG = "PrisonerActivity";

    private TextView tv_prisonerId, tv_prisonerName, tv_prisonerGender, tv_prisonerAge,
            tv_prisonerEdu, tv_prisonerHeight, tv_prisonerWeight, tv_prisonerComment, tv_prisonerCrime;
    private ImageView iv_prisonerIcon;

    private MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(PrisonerActivity)");
        setContentView(R.layout.activity_prisoner);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("押解任务详情");
        init();
    }

    private void init() {
        tv_prisonerId = (TextView) findViewById(R.id.prisoner_id);
        tv_prisonerName = (TextView) findViewById(R.id.prisoner_name);
        tv_prisonerAge = (TextView) findViewById(R.id.prisoner_age);
        tv_prisonerGender = (TextView) findViewById(R.id.prisoner_gender);
        tv_prisonerHeight = (TextView) findViewById(R.id.prisoner_height);
        tv_prisonerWeight = (TextView) findViewById(R.id.prisoner_weight);
        tv_prisonerEdu = (TextView) findViewById(R.id.prisoner_education);
        tv_prisonerCrime = (TextView) findViewById(R.id.prisoner_crime);
        tv_prisonerComment = (TextView) findViewById(R.id.prisoner_comment);
        iv_prisonerIcon = (ImageView) findViewById(R.id.prisoner_icon);

        mainApplication = (MainApplication) getApplication();
        if(mainApplication.getPrisonerId() != null) {
            getPrisonerInfo(mainApplication.getPrisonerId());
        } else {
            Toast.makeText(this, "无法获取服刑人员信息", Toast.LENGTH_SHORT).show();
        }
    }

    private void getPrisonerInfo(String prid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("prisonerId", prid);
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn("prisoners/get", OkHttpUtil.TYPE_GET, params, new OkHttpUtil.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                JSONObject jsonObj = JSON.parseObject(result);
                switch (jsonObj.getString("id")) {
                    case "1":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_1);
                        break;
                    case "2":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_2);
                        break;
                    case "3":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_3);
                        break;
                    case "4":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_4);
                        break;
                    case "5":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_5);
                        break;
                    case "6":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_6);
                        break;
                    case "7":
                        iv_prisonerIcon.setImageResource(R.mipmap.prisoner_7);
                        break;
                    default:
                        break;
                }
                tv_prisonerId.setText(jsonObj.getString("prisonerId"));
                tv_prisonerName.setText(jsonObj.getString("prisonerName"));
                tv_prisonerGender.setText(jsonObj.getString("gender"));
                tv_prisonerAge.setText(jsonObj.getString("age"));
                tv_prisonerEdu.setText(jsonObj.getString("educationBackground"));
                tv_prisonerCrime.setText(jsonObj.getString("crime"));
                tv_prisonerHeight.setText(jsonObj.getString("height"));
                tv_prisonerWeight.setText(jsonObj.getString("weight"));
                tv_prisonerComment.setText(jsonObj.getString("comment"));
            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.e(TAG, errorMsg);
            }
        });
    }
}
