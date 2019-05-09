package com.edgecomputing.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.edgecomputing.utils.AndroidWebSocketClient;
import com.edgecomputing.utils.CpuMonitor;
import com.edgecomputing.utils.EditTextClearTool;
import com.edgecomputing.utils.MemoryMonitor;
import com.edgecomputing.utils.OkHttpUtil;

import org.java_websocket.enums.ReadyState;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView batteryText, cpuText, memoryRate;
    private BatteryReceiver receiver = null;
    private String battery;
    private ImageView userImage;
    private TextView userName, userId, logout;

    private Button loginBtn;
    private TextView registerTv;
    private EditText userNameEt, pwdEt;
    private ImageView userNameClear, pwdClear;
    private CheckBox rememberPwd;
    private boolean isRememberPwd = false;

    private MainApplication mainApplication;

    private String LAYOUT;
    private static final String TAG = "MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(MainActivity)");
//        CommonUtil.setStatusBarColor(MainActivity.this, Color.parseColor("#A9A9A9"));
        mainApplication = (MainApplication) getApplication();
        if(!mainApplication.isLogin()) {
            setContentView(R.layout.activity_login);
            initLoginLayout();
        } else {
            setContentView(R.layout.activity_main);
            initMainLayout();
            startMonitor();
        }
    }

    private void initMainLayout() {
        Log.i(TAG, "initMainLayout(MainActivity)");
        LAYOUT = "main";
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        cpuText = (TextView) findViewById(R.id.cpu_rate);
        memoryRate = (TextView) findViewById(R.id.memory_rate);
        batteryText = (TextView) findViewById(R.id.buttery_rate);

        View headView = navigationView.getHeaderView(0);
        userImage = headView.findViewById(R.id.user_imageView);
        userName = headView.findViewById(R.id.user_name);
        userId = headView.findViewById(R.id.user_id);
        logout = headView.findViewById(R.id.logout);

//        userImage.setImageResource(R.mipmap.ic_logo);  // TODO: 用户头像
        userName.setText(mainApplication.getUserName());
        userId.setText(""); // TODO: uid

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<>();
                params.put("token", mainApplication.getToken());
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn("users/logout", OkHttpUtil.TYPE_DEL, params, new OkHttpUtil.ReqCallBack<String>() {

                    @Override
                    public void onReqSuccess(String result) {
                        mainApplication.setToken("");
                        mainApplication.setLogin(false);
                        setContentView(R.layout.activity_login);
                        initLoginLayout();
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                    }
                });
            }
        });

    }

    private void initLoginLayout() {
        Log.i(TAG, "initLoginLayout(MainActivity)");
        LAYOUT = "login";
        // 去除顶部标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        userNameEt = (EditText) findViewById(R.id.et_userName);
        pwdEt = (EditText) findViewById(R.id.et_password);
        userNameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        loginBtn = (Button) findViewById(R.id.btn_login);
        registerTv = (TextView) findViewById(R.id.tv_register);
        rememberPwd = (CheckBox) findViewById(R.id.cb_checkbox);

        EditTextClearTool.addClearListener(userNameEt,userNameClear);
        EditTextClearTool.addClearListener(pwdEt,pwdClear);

        if(mainApplication.getUserName() != null) {
            userNameEt.setText(mainApplication.getUserName());
            if(mainApplication.isRememberPwd()) {
                rememberPwd.setChecked(true);
                pwdEt.setText(mainApplication.getPassword());
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
                intent.setClass(MainActivity.this, RegisterActivity.class);
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

    private void loginRequest() {
        HashMap<String, String> params = new HashMap<>();
        params.put("userName", userNameEt.getText().toString());
        params.put("password", pwdEt.getText().toString());
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn("users/login", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                if(JSON.parseObject(result).getInteger("code") == 200) {
                    JSONObject jsonObj = JSON.parseObject(result).getJSONObject("data");
                    String token = jsonObj.getString("loginToken");
                    String userName = jsonObj.getString("userName");
                    mainApplication.setLogin(true);
                    mainApplication.setLoginUserInfo(userName, token);
                    if(isRememberPwd) {
                        mainApplication.setPassword(pwdEt.getText().toString());
                        mainApplication.setRememberPwd(true);
                    } else {
                        mainApplication.setRememberPwd(false);
                    }
                    if(!mainApplication.isReg2Server()) {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, Register2ServerActivity.class);
                        startActivityForResult(intent,2);
                    } else {
                        setContentView(R.layout.activity_main);
                        initMainLayout();
                        if(receiver == null) {
                            startMonitor();
                        }
                    }
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

    private void startMonitor() {
        //初始化webSocket链接
        ReadyState readyState = AndroidWebSocketClient.getInstance(this).getReadyState();
        Log.i("WebSocket", "getReadyState() = " + readyState);

        if(readyState.equals(ReadyState.NOT_YET_CONNECTED)){
            Log.i("WebSocket", "---初始化WebSocket客户端---");
            AndroidWebSocketClient.getInstance(this).connect();
        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 广播接收者
     */
    private class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                battery = level + "%";
                batteryText.setText(battery);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart(MainActivity)");
        if(LAYOUT.equals("main")) {
            ScheduledExecutorService executorService;
            executorService = new ScheduledThreadPoolExecutor(1);
//            new ThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build()
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    String cpu = CpuMonitor.getCpuRate();
                    String mem = MemoryMonitor.getMemoryRate(getApplicationContext());
                    String info = "device: {deviceId: " + "2, " + "battery: " + battery + ", " + "cpuRate: " + cpu + ", " + "memRate: " + mem + "}";
                    if(AndroidWebSocketClient.getInstance(getApplicationContext()).getReadyState().equals(ReadyState.OPEN)) {
                        AndroidWebSocketClient.getInstance(getApplicationContext()).send(info);
                    }
                }
            },1,30, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy(MainActivity)");
        if(LAYOUT.equals("main")) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult(MainActivity)");
        if(resultCode == 1) {
            userNameEt.setText(data.getStringExtra("userName"));
            pwdEt.setText(data.getStringExtra("password"));
        } else if(resultCode == 2) {
            setContentView(R.layout.activity_main);
            initMainLayout();
            if(receiver == null) {
                startMonitor();
            }
        } else if(resultCode == 3) {
            Toast.makeText(getBaseContext(), "设备注册失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause(MainActivity)");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume(MainActivity)");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart(MainActivity)");
    }

}
