package com.edgecomputing.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.BaseOperations;
import com.edgecomputing.utils.CommonUtil;
import com.edgecomputing.utils.CpuMonitor;
import com.edgecomputing.utils.MemoryMonitor;
import com.edgecomputing.utils.OkHttpUtil;
import com.edgecomputing.bean.ServerAddress;
import com.edgecomputing.utils.UartService;
import com.edgecomputing.utils.WarnDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ImageView userImage;
    private TextView userName, userId, logout;

    private BatteryReceiver receiver = null;
    private String battery;

    private boolean STATUS = false;
    private boolean stopRunnable = false;
    private boolean stopRunnable1 = false;
    private boolean stopTestRunnable = false;
    private boolean stopVervelRunnable = false;
    private boolean reConnect = true;
//    private boolean isConnectToServer = true;
    private WarnDialog warnDialog;
    private MainApplication mainApplication;
//    private boolean back = true;
//    private MyTensorFlow myTensorFlow = new MyTensorFlow(getAssets());

    private static final int REQUEST_LOGIN = 1;
    private static final int REQUEST_LOGOUT = 2;
    private static final int RESULT_LOGOUT_FAIL = 3;
    private static final int REQUEST_MAP = 4;
    private static final int REQUEST_TASK = 5;
    private static final int REQUEST_PRISONER = 6;
    private static final int REQUEST_SELECT_DEVICE = 11;
    private static final int REQUEST_ENABLE_BT = 12;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    public static boolean flag3 = false;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend, mbutton_height, mbutton_heart, button_send, btnGetCpu;
    private CheckBox server1, server2;
    private Button btnSelectServer;
    private String selectServerUrl;
    private EditText edtMessage;
    private String currentAction;
    private String myDeviceAddress;
    private boolean isVibrating = false;
//    private boolean isComplete = false;
//    private boolean isPostHeartComplete = false;
//    private String currentHeart;

    private Vibrator vibrator;

    private static final String TAG = "MainActivity";

    Handler handler = new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            String cpu = CpuMonitor.getCpuRate();
            String mem = MemoryMonitor.getMemoryRate(getApplicationContext());
            postDeviceInfo(cpu, mem);
            if(!stopRunnable) {
                handler.postDelayed(runnable, 5000);
            }
        }
    };
    Handler handler1 = new Handler();
    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if (!stopRunnable1 && flag3) {
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                String message = "heart";
                currentAction = "heart";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                showMessage("没有连接设备，无法获取心率哦!");
            }
        }
    };
    Handler vervelHandler = new Handler();
    Runnable vervelRunnable=new Runnable() {
        @Override
        public void run() {
            try {
                readFileWarn();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!stopVervelRunnable) {
                vervelHandler.postDelayed(vervelRunnable, 1000);
            }
        }
    };
    Handler testHandler = new Handler();
    Runnable testRunnable = new Runnable() {
        @Override
        public void run() {
            if (!stopTestRunnable) {
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "devices/isConnectivity", OkHttpUtil.TYPE_GET, null, new OkHttpUtil.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        Log.i(TAG, result);
                        testHandler.postDelayed(testRunnable, 5000);
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                        stopRunnable = true;
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] Error: "+ "can't connect server!");
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        getRiskValue();
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(MainActivity)");
        mainApplication = (MainApplication) getApplication();
        if(!mainApplication.isLogin()) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        } else {
            STATUS = true;
            setContentView(R.layout.activity_main);
            initBase();
            initBTLayout();
        }
    }

    private void initBase() {
        Log.i(TAG, "initBase(MainActivity)");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);
        userImage = headView.findViewById(R.id.user_imageView);
        userName = headView.findViewById(R.id.user_name);
        userId = headView.findViewById(R.id.user_id);
        logout = headView.findViewById(R.id.logout);
        if(mainApplication.getUserName().equals("张平")){
            userImage.setImageResource(R.mipmap.user_1);
        }else if(mainApplication.getUserName().equals("宋小玲")){
            userImage.setImageResource(R.mipmap.user_4);
        }else if(mainApplication.getUserName().equals("马飞")) {
            userImage.setImageResource(R.mipmap.user_2);
        }
//        else if(mainApplication.getUserName().equals("李建国")) {
//            userImage.setImageResource(R.mipmap.user_3);
//        }
        userName.setText("姓名：" + mainApplication.getUserName());
        userId.setText("民警编号：" + mainApplication.getUserId());
        vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<>(1);
                params.put("token", mainApplication.getToken());
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "users/logout", OkHttpUtil.TYPE_DEL, params, new OkHttpUtil.ReqCallBack<String>() {

                    @Override
                    public void onReqSuccess(String result) {
                        mainApplication.setToken("");
                        mainApplication.setLogin(false);
                        stopRunnable = true;
                        stopTestRunnable = true;
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        intent.putExtra("logout", true);
                        startActivityForResult(intent, REQUEST_LOGOUT);
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                    }
                });
            }
        });

        /**
         * 广播初始化
         */
        receiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);
        //开启线程获取手机性能数据
//        handler.postDelayed(runnable, 5000);
//        testHandler.postDelayed(testRunnable, 5000);
    }

    private void initBTLayout() {
        Log.i(TAG, "initBTLayout(MainActivity)");
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            setResult(6);
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_connect);
//        mbutton_height = (Button) findViewById(R.id.button_hight);
//        mbutton_heart = (Button) findViewById(R.id.button_heart);
//        btnSend = (Button) findViewById(R.id.sendButton);
//        edtMessage = (EditText) findViewById(R.id.sendText);

        server1 = (CheckBox) findViewById(R.id.cb_server1);
        server2 = (CheckBox) findViewById(R.id.cb_server2);
        btnSelectServer = (Button) findViewById(R.id.btn_select_server);
        button_send = (Button) findViewById(R.id.btn_warn_open);
        btnGetCpu = (Button) findViewById(R.id.btn_get_cpu);
        initService();

        server1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    server2.setChecked(false);
                }
            }
        });

        server2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    server1.setChecked(false);
                }
            }
        });

        btnSelectServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(server2.isChecked()) {
                    selectServerUrl = ServerAddress.SERVER_URL2.getServer_url();
                }else {
                    selectServerUrl = ServerAddress.SERVER_URL1.getServer_url();
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("当前服务器地址为：\n" + selectServerUrl + "\n" + "确认开启相关服务吗？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainApplication.setServerAddress(selectServerUrl);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        btnGetCpu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnGetCpu.getText().equals("开启手机监控")) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("确认开启手持机性能监测吗？")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    btnGetCpu.setText("关闭手机监控");
                                    //开启线程获取手机性能数据
                                    handler.postDelayed(runnable, 5000);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    btnGetCpu.setText("开启手机监控");
                    stopRunnable = true;
//                    handler.removeCallbacks(runnable);
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button_send.getText().equals("开启脚环告警")) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("提示")
                                .setMessage("确认开启脚环告警功能吗？")
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        button_send.setText("关闭脚环告警");
                                        stopVervelRunnable = false;
                                        vervelHandler.post(vervelRunnable);
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                } else {
                    button_send.setText("开启脚环告警");
                    stopVervelRunnable = true;
//                    vervelHandler.removeCallbacks(vervelRunnable);
                }
            }
        });

        // Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        stopRunnable1 = true;
                        reConnect = true;
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                        //Log.d(TAG, "寻找请求码:"+REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        reConnect = false;
                        if (mDevice!=null) {
                            mService.disconnect();
                        }
                    }
                }
            }
        });
        // Handler Send button
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText editText = (EditText) findViewById(R.id.sendText);
//                String message = editText.getText().toString();
//                byte[] value;
//                try {
//                    //send data to service
//                    value = message.getBytes("UTF-8");
//                    mService.writeRXCharacteristic(value);
//                    //Update the log with time stamp
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    edtMessage.setText("");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        // Handler Send button
//        mbutton_height.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (flag3) {
//                    //EditText editText = (EditText) findViewById(R.id.sendText);
//                    String message = "high";
//                    currentAction = "high";
//                    byte[] value;
//                    try {
//                        //send data to service
//                        value = message.getBytes("UTF-8");
//                        mService.writeRXCharacteristic(value);
//                        //Update the log with time stamp
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
//                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                        edtMessage.setText("");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                }else {
//                    showMessage("没有连接设备，无法获取高度值哦!");
//                }
//            }
//        });
//        // Handler Send button
//        mbutton_heart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (flag3) {
//                    //EditText editText = (EditText) findViewById(R.id.sendText);
//                    String message = "heart";
//                    currentAction = "heart";
//                    byte[] value;
//                    try {
//                        //send data to service
//                        value = message.getBytes("UTF-8");
//                        mService.writeRXCharacteristic(value);
//                        //Update the log with time stamp
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
//                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                        edtMessage.setText("");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    showMessage("没有连接设备，无法获取心率值哦!");
//                }
//            }
//        });

//        if(myTensorFlow.initTensorFlow()) {
//            myTensorFlow.runTensorFlow();
//        }
    }

    private void initService() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
//                        edtMessage.setEnabled(true);
//                        btnSend.setEnabled(true);
                        flag3 = true;
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopRunnable1 = true;
//                        handler1.removeCallbacks(runnable1);
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
//                        edtMessage.setEnabled(false);
//                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        //手环蓝牙断开重连
                        if(reConnect) {
                            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(myDeviceAddress);
                            Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "   mserviceValue:" + mService);
                            ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                            Log.i(TAG, myDeviceAddress);
                            mService.connect(myDeviceAddress);
                        }else {
                            mService.close();
                        }
                        //setUiState();
                    }
                });
            }
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
                stopRunnable1 = false;
                handler1.postDelayed(runnable1, 5000);
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            if(currentAction.equals("heart")) {
                                checkPrisonerId(BaseOperations.ByteArrToHeartRate(txValue), "0");
                                listAdapter.add("["+currentDateTimeString+"] RX: " + BaseOperations.ByteArrToHeartRate(txValue));
                                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                                handler1.postDelayed(runnable1, 5000);
                            }else if(currentAction.equals("high")) {
//                                checkPrisonerId(currentHeart, BaseOperations.ByteArrToHex(txValue));
                                listAdapter.add("["+currentDateTimeString+"] RX: " + BaseOperations.ByteArrToHex(txValue) + "  hpa");
                                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                                handler1.postDelayed(runnable1, 5000);
                            }
                            Log.i("sendhand", "蓝牙发送手环数据:" + BaseOperations.ByteArrToHex(txValue));
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private void readFileWarn() throws IOException {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String msg = CommonUtil.ReadFromFile(path + "/uartrw/uartrw.txt");
        if(msg.equals("danger")) {
            showDialog("超距警告");
            // 脚环超距告警上传后台
            HashMap<String, String> params = new HashMap<>();
            params.put("prisonerId", mainApplication.getPrisonerId());
            OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "prisonerData/outrange", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
                @Override
                public void onReqSuccess(String result) {
                    Log.i(TAG, result);
                }

                @Override
                public void onReqFailed(String errorMsg) {
                    Log.e(TAG, errorMsg);
                }
            });
        }
    }

    private void checkPrisonerId(String heart, String height) {
        if(mainApplication.getPrisonerId() == null) {
            if(mainApplication.getBraceletNo() == null) {
                showMessage("无法获取手环mac地址，心率数据无法上传");
            } else {
                HashMap<String, String> pp = new HashMap<>(1);
                pp.put("braceletNo", mainApplication.getBraceletNo());
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "devices/prisonerId", OkHttpUtil.TYPE_GET, pp, new OkHttpUtil.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        Log.i(TAG, result);
                        if(result != null && !result.equals("")) {
                            String[] str = result.split(";");
                            if(!str[0].equals("")) {
                                mainApplication.setPrisonerId(str[0]);
                                postHeartRate(heart, height);
                            }
                        }
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] Error: "+ "无法获取服刑人员编号");
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    }
                });
            }
        }else {
            postHeartRate(heart, height);
        }
    }

    private void postHeartRate(String heart, String height) {
        HashMap<String, String> params = new HashMap<>(3);
        params.put("prisonerId", mainApplication.getPrisonerId());
        params.put("heartbeat", heart);
        params.put("height", height);
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "prisonerData/upload", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                Log.i(TAG, "心率和高度数据上传成功");
//                todo:读取风险值
//                HashMap<String, String> pp = new HashMap<>(1);
//                pp.put("PrisonerId", mainApplication.getPrisonerId());
//                OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "prisonerData/get", OkHttpUtil.TYPE_GET, pp, new OkHttpUtil.ReqCallBack<String>() {
//                    @Override
//                    public void onReqSuccess(String result) {
//                        Log.i(TAG, result);
//                        int risk = Integer.parseInt(JSON.parseObject(result).getString("riskValue"));
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        listAdapter.add("["+currentDateTimeString+"] Risk: "+ risk);
//                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                        if(risk >= 75) {
//                            showDialog("3级风险");
//                        }else if(risk >= 50) {
//                            showDialog("2级风险");
//                        }
//                    }
//
//                    @Override
//                    public void onReqFailed(String errorMsg) {
//                        Log.e(TAG, errorMsg);
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        listAdapter.add("["+currentDateTimeString+"] Error: "+ "无法获取风险值");
//                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    }
//                });
            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.e(TAG, errorMsg);
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                listAdapter.add("["+currentDateTimeString+"] Error: "+ "无法上传心率值");
                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
            }
        });
    }

    private void runRisk(double[] array) {
//        double[] array = {21,1,0,174.42525773195877,81.2,1,0,1,0,0,0,8,2,0,0.49,1};
        String res = CommonUtil.getPredicationResult(array);
        Log.i(TAG, res);
        if(res == "2") {
            showDialog("2级风险");
        }else {
            showDialog("3级风险");
        }
    }

    private void getRiskValue() {
        if(mainApplication.getPrisonerInfo() == null) {
            HashMap<String, String> pp = new HashMap<>(1);
            pp.put("braceletNo", mainApplication.getBraceletNo());
            OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "devices/prisonerId", OkHttpUtil.TYPE_GET, pp, new OkHttpUtil.ReqCallBack<String>() {
                @Override
                public void onReqSuccess(String result) {
                    Log.i(TAG, result);
                    if(result != null && !result.equals("")) {
                        String[] str = result.split(";");
                        if(!str[1].equals("")) {
                            mainApplication.setPrisonerInfo(str[1]);
                            runRisk(parseString(str[1]));
                        }
                    }
                }

                @Override
                public void onReqFailed(String errorMsg) {
                    Log.e(TAG, errorMsg);
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] Error: "+ "无法获取服刑人员个人属性特征");
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                }
            });
        } else {
            runRisk(parseString(mainApplication.getPrisonerInfo()));
        }
    }

    private double[] parseString(String s) {
        String[] strings = s.split(",");
        double[] prisonerInfo = new double[strings.length];
        for(int i=0; i<prisonerInfo.length; i++) {
            prisonerInfo[i] = Double.parseDouble(strings[i]);
        }
        return prisonerInfo;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void postDeviceInfo(String cpu, String mem) {
        HashMap<String, String> params = new HashMap<>(4);
        params.put("deviceNo", mainApplication.getDeviceNo());
        params.put("cpuUsageRate", cpu);
        params.put("memoryUsageRate", mem);
        params.put("dumpEnergyRate", battery);
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerUrl, "deviceRunInfo/upload", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                Log.i(TAG, result);
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                listAdapter.add("["+currentDateTimeString+"] CPU: "+ cpu);
                listAdapter.add("["+currentDateTimeString+"] Memory: "+ mem);
                listAdapter.add("["+currentDateTimeString+"] Battery: "+ battery);
                messageListView.smoothScrollToPosition(listAdapter.getCount() - 3);
            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.e(TAG, errorMsg);
                stopRunnable = true;
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                listAdapter.add("["+currentDateTimeString+"] Error: "+ "无法上传设备性能数据");
                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
            }
        });
    }

    public void showDialog(String msg) {
        if(warnDialog == null) {
            warnDialog = WarnDialog.showDialog(this, msg);
        }
        ImageView ivClose = warnDialog.findViewById(R.id.ivClose);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destroyDialog();
            }
        });
        warnDialog.show();
        // 开启震动
        isVibrating = true;
        long[] patter = {1000, 1000};
        vibrator.vibrate(patter, 0);
    }

    public void destroyDialog() {
        if(warnDialog != null) {
            warnDialog.dismiss();
            if(isVibrating) {
                isVibrating = false;
                vibrator.cancel();
            }
        }
    }

    /**
     * 广播接收者
     */
    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                battery = level + "";
            }
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //如果没有获取权限，那么可以提示用户去设置界面--->应用权限开启权限
                    Toast.makeText(getApplicationContext(), "获取权限失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed(MainActivity)");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        Log.i(TAG, "蓝牙连接状态码：" + mState);
        if (mState == UART_PROFILE_CONNECTED) {
            moveTaskToBack(false);
//            Intent startMain = new Intent();
//            startMain.setAction(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
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

        if (id == R.id.nav_task) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, TaskActivity.class);
            startActivityForResult(intent, REQUEST_TASK);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, PrisonerActivity.class);
            startActivityForResult(intent, REQUEST_PRISONER);
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MapActivity.class);
            startActivityForResult(intent, REQUEST_MAP);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult(MainActivity)");
        Log.d(TAG, "特殊设备程序运行到这里了：" + requestCode);
        switch (requestCode) {
            case REQUEST_LOGIN:
                setContentView(R.layout.activity_main);
                STATUS = true;
                initBase();
                initBTLayout();
                break;
            case REQUEST_LOGOUT:
                if(resultCode == RESULT_LOGOUT_FAIL) {
                    finish();
                }else {
                    stopRunnable = false;
//                    stopTestRunnable = false;
                    handler.postDelayed(runnable, 5000);
//                    testHandler.postDelayed(testRunnable, 5000);
                }
                break;
            case REQUEST_MAP:
//                stopRunnable = true;
//                handler.postDelayed(runnable, 15000);
                break;
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    myDeviceAddress = deviceAddress;
                    Log.d(TAG, "... onActivity.address=="+deviceAddress);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "   mserviceValue:" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    Log.i(TAG, deviceAddress);
//                    stopRunnable = true;
//                    handler.postDelayed(runnable, 15000);
                    mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
//                    setResult(6);
//                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart(MainActivity)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy(MainActivity)");
        if(STATUS){
            if(receiver != null) {
                unregisterReceiver(receiver);
            }
//            if(btnGetCpu.getText().equals("关闭手机监控")) {
//                stopRunnable = true;
//                handler.removeCallbacks(runnable);
//            }
//            if(btnConnectDisconnect.getText().equals("Disconnect")) {
//                stopRunnable1 = true;
//                handler1.removeCallbacks(runnable1);
//            }
//            if(button_send.getText().equals("关闭脚环告警")) {
//                stopVervelRunnable = true;
//                vervelHandler.removeCallbacks(vervelRunnable);
//            }
            stopRunnable = true;
            handler.removeCallbacks(runnable);
            stopRunnable1 = true;
            handler1.removeCallbacks(runnable1);
            stopVervelRunnable = true;
            vervelHandler.removeCallbacks(vervelRunnable);
//            stopTestRunnable = true;
//            testHandler.removeCallbacks(testRunnable);
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
            } catch (Exception ignore) {
                Log.e(TAG, ignore.toString());
            }
            unbindService(mServiceConnection);
            mService.stopSelf();
            mService= null;
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
        if(STATUS) {
            if (!mBtAdapter.isEnabled()) {
                Log.i(TAG, "onResume - BT not enabled yet");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart(MainActivity)");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
