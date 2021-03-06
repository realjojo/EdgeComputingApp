/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edgecomputing.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.edgecomputing.R;
import com.edgecomputing.adapter.BlueListAdapter;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.bean.BlueDevice;
import com.edgecomputing.utils.LoadingDialog;
import com.edgecomputing.utils.OkHttpUtil;

public class DeviceListActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;

   // private BluetoothAdapter mBtAdapter;
    private TextView mEmptyList;
    private LoadingDialog loadingDialog;
    public static final String TAG = "DeviceListActivity";
    
    List<BluetoothDevice> deviceList;
    private ArrayList<BlueDevice> blueDevices = new ArrayList<>();
    private BlueListAdapter deviceAdapter;
    private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private Handler mHandler;
    private boolean mScanning;
    private RecyclerView newDevicesListView;
    private BluetoothDevice mBlueDevice;

    private String selectServerAddress;
    private MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.activity_device_list);
//        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
//        layoutParams.gravity=Gravity.TOP;
//        layoutParams.y = 200;
        // 去除顶部标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        mainApplication = (MainApplication) getApplication();
        selectServerAddress = mainApplication.getServerAddress();
        if(selectServerAddress == null) {
            selectServerAddress = "http://10.109.246.55:8089";
        }
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        populateList();
        mEmptyList = (TextView) findViewById(R.id.empty);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (mScanning==false) {
            	    scanLeDevice(true);
                } else {
            	    finish();
                }
            }
        });
    }

    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new BlueListAdapter(this, blueDevices);
        devRssiValues = new HashMap<String, Integer>();

        newDevicesListView = (RecyclerView) findViewById(R.id.new_devices);
        newDevicesListView.setLayoutManager(new LinearLayoutManager(this));
        newDevicesListView.setAdapter(deviceAdapter);
        deviceAdapter.setOnItemClickListener(new BlueListAdapter.onRecycleItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
//                BluetoothDevice device = deviceList.get(position);
                mBlueDevice = deviceList.get(position);
//                showMessage("开始与" + mBlueDevice.getName() + "进行配对");
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//
//                Bundle b = new Bundle();
//                b.putString(BluetoothDevice.EXTRA_DEVICE, mBlueDevice.getAddress());
//
//                Intent mIntent = new Intent();
//                mIntent.putExtras(b);
//                setResult(Activity.RESULT_OK, mIntent);
//                finish();
                HashMap<String, String> params = new HashMap<>(1);
                params.put("braceletNo", mBlueDevice.getAddress());
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerAddress, "devices/braceletBind", OkHttpUtil.TYPE_GET, params, new OkHttpUtil.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        if(result.equals("true")) {
                            MainApplication mainApplication = (MainApplication) getApplication();
                            if(mainApplication.getBraceletNo() == null) {
                                mainApplication.setBraceletNo(mBlueDevice.getAddress());
                            }
                            showMessage("开始与" + mBlueDevice.getName() + "进行配对");
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);

                            Bundle b = new Bundle();
                            b.putString(BluetoothDevice.EXTRA_DEVICE, mBlueDevice.getAddress());

                            Intent mIntent = new Intent();
                            mIntent.putExtras(b);
                            setResult(Activity.RESULT_OK, mIntent);
                            finish();
                        }else {
                            Intent intent = new Intent();
                            intent.setClass(DeviceListActivity.this, BraceletActivity.class);
                            intent.putExtra("macAddress", mBlueDevice.getAddress());
                            startActivityForResult(intent, 1);
                        }
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                        Toast.makeText(getBaseContext(), "查询信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        scanLeDevice(true);
    }
    
    private void scanLeDevice(final boolean enable) {
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
					mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    cancelButton.setText(R.string.scan);
                    destroyDialog();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            cancelButton.setText(R.string.cancel);
            showDialog();
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            cancelButton.setText(R.string.scan);
            destroyDialog();
        }
    }

    public void showDialog() {
        if(loadingDialog == null) {
            loadingDialog = LoadingDialog.showDialog(this);
        }
        loadingDialog.show();
    }

    public void destroyDialog() {
        if(loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	  runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              if(device.getName() != null) {
                                  addDevice(device, rssi);
                              }
                          }
                      });
                }
            });
        }
    };
    
    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;
        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
        	deviceList.add(device);
        	blueDevices.add(new BlueDevice(device.getName(), device.getAddress(), device.getBondState()));
            mEmptyList.setVisibility(View.GONE);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1) {
            showMessage("开始与" + mBlueDevice.getName() + "进行配对");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, mBlueDevice.getAddress());

            Intent mIntent = new Intent();
            mIntent.putExtras(b);
            setResult(Activity.RESULT_OK, mIntent);
            finish();
        }else if(resultCode == 2) {
            showMessage("手环未绑定，请先绑定再连接蓝牙");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
       
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
    	
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
  
            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        	
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }
    
    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
                
            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
