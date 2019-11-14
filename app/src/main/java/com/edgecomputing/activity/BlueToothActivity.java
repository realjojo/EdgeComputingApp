package com.edgecomputing.activity;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.BaseOperations;
import com.edgecomputing.utils.OkHttpUtil;
import com.edgecomputing.utils.UartService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class BlueToothActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "BlueToothActivity";
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
    private Button btnConnectDisconnect,btnSend,mbutton_hight,mbutton_heart;
    private EditText edtMessage;
    private MainApplication mainApplication;
    private String currentAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            setResult(6);
            finish();
            return;
        }
        mainApplication = (MainApplication) getApplication();
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        mbutton_hight = (Button) findViewById(R.id.button_hight);
        mbutton_heart = (Button) findViewById(R.id.button_heart);
        btnSend = (Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();

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
                        Intent newIntent = new Intent(BlueToothActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                        //Log.d(TAG, "寻找请求码:"+REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();
                        }
                    }
                }
            }
        });
        // Handler Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        // Handler Send button
        mbutton_hight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag3) {
                    //EditText editText = (EditText) findViewById(R.id.sendText);
                    String message = "high";
                    currentAction = "high";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else {
                    showMessage("没有连接设备，无法获取高度值哦!");
                }
            }
        });
        // Handler Send button
        mbutton_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag3) {
                    //EditText editText = (EditText) findViewById(R.id.sendText);
                    String message = "heart";
                    currentAction = "heart";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    showMessage("没有连接设备，无法获取心率值哦!");
                }
            }
        });
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
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
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
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });
            }
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
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
                                postHeartRate(BaseOperations.ByteArrToHeartRate(txValue));
                                listAdapter.add("["+currentDateTimeString+"] RX: " + BaseOperations.ByteArrToHeartRate(txValue));
                            }else if(currentAction.equals("high")) {
                                listAdapter.add("["+currentDateTimeString+"] RX: " + BaseOperations.ByteArrToHeight(txValue) + "  hpa");
                            }
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
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

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(BlueToothActivity)");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy(BlueToothActivity)");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop(BlueToothActivity)");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause(BlueToothActivity)");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart(BlueToothActivity)");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume(BlueToothActivity)");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "特殊设备程序运行到这里了："+requestCode);
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "... onActivity.address=="+deviceAddress);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "   mserviceValue:" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    Log.i(TAG, deviceAddress);
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
                    setResult(6);
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    public void postHeartRate(String heart) {
        if(mainApplication.getPrisonerId() == null) {
            showMessage("无法获取服刑人员编号，心率数据无法上传");
        }else {
            HashMap<String, String> params = new HashMap<>(1);
            params.put("prisonerId", mainApplication.getPrisonerId());
            params.put("heartbeat", heart);
            OkHttpUtil.getInstance(getBaseContext()).requestAsyn("prisonerData/upload", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }else {
            setResult(6);
            finish();
        }
//        else {
//            new AlertDialog.Builder(this)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle(R.string.popup_title)
//                    .setMessage(R.string.popup_message)
//                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    })
//                    .setNegativeButton(R.string.popup_no, null)
//                    .show();
//        }
    }
}
