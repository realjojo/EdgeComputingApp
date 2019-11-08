package com.edgecomputing.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
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
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.MovingPointOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.TMC;
import com.amap.api.services.route.WalkRouteResult;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.AndroidWebSocketClient;
import com.edgecomputing.utils.CommonUtil;
import com.edgecomputing.utils.CpuMonitor;
import com.edgecomputing.utils.EditTextClearTool;
import com.edgecomputing.utils.MemoryMonitor;
import com.edgecomputing.utils.OkHttpUtil;

import org.java_websocket.enums.ReadyState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BatteryReceiver receiver = null;
    private String battery;
    private ImageView userImage;
    private TextView userName, userId, logout;

    /**
     * map相关
     */
    private MapView mMapView;
    private AMap aMap;
    private RouteSearch routeSearch;
    private DriveRouteResult mDriveRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(39.959698, 116.300278);
    private LatLonPoint mEndPoint = new LatLonPoint(39.130527, 117.176994);
    private LatLng startPoint;
    private LatLng endPoint;
    private ProgressDialog progDialog = null;
    private boolean nodeIconVisible = true;
    private List<Marker> stationMarkers = new ArrayList<Marker>();
    private boolean isColorfulline = true;
    private PolylineOptions mPolylineOptions;
    private float mWidth = 25; //路线宽度
    private DrivePath drivePath;
    private List<LatLng> mLatLngsOfPath;
    private List<TMC> tmcs;
    private Marker startMarker;
    private Marker endMarker;
    private Marker currentMarker;
    private List<LatLonPoint> throughPointList;
    private List<Marker> throughPointMarkerList = new ArrayList<Marker>();
    private boolean throughPointMarkerVisible = true;
    private List<Polyline> allPolyLines = new ArrayList<Polyline>();
    private PolylineOptions mPolylineOptionscolor = null;
    private List<MovingPointOverlay> smoothMarkerList;
    private List<Marker> markerList;
    private int totalCarNum = 3;
    private int curNum = 0;
    private Bundle savedInstanceState;
    private boolean STATUS = false;
    private boolean stopRunnable = false;
    private WarnDialog warnDialog;
    private MainApplication mainApplication;
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(MainActivity)");
        this.savedInstanceState = savedInstanceState;
        mainApplication = (MainApplication) getApplication();
        if(!mainApplication.isLogin()) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 1);
        } else {
            STATUS = true;
            setContentView(R.layout.activity_main);
            initMainLayout(savedInstanceState);
        }
    }

    private void initMainLayout(Bundle savedInstanceState) {
        Log.i(TAG, "initMainLayout(MainActivity)");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        //        userImage.setImageResource(R.mipmap.ic_logo);  // TODO: 用户头像
        userName.setText("姓名：" + mainApplication.getUserName());
        userId.setText("民警编号：" + mainApplication.getUserId());
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<>(1);
                params.put("token", mainApplication.getToken());
                OkHttpUtil.getInstance(getBaseContext()).requestAsyn("usrM/users/logout", OkHttpUtil.TYPE_DEL, params, new OkHttpUtil.ReqCallBack<String>() {

                    @Override
                    public void onReqSuccess(String result) {
                        mainApplication.setToken("");
                        mainApplication.setLogin(false);
                        stopRunnable = true;
                        if(receiver != null) {
                            unregisterReceiver(receiver);
                        }
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        intent.putExtra("logout", true);
                        startActivityForResult(intent, 1);
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.e(TAG, errorMsg);
                    }
                });
            }
        });

        mMapView =  (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
                dissmissProgressDialog();
                aMap.clear();// 清理地图上的所有覆盖物
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null) {
                        if (result.getPaths().size() > 0) {
                            mDriveRouteResult = result;
                            drivePath = mDriveRouteResult.getPaths().get(0);
                            //设置节点marker是否显示
                            setNodeIconVisibility(false);
                            // 是否用颜色展示交通拥堵情况，默认true
                            setIsColorfulline(true);
                            removeFromMap();
                            addToMap();
                            zoomToSpan();
                            markerList = new ArrayList<>();
                            smoothMarkerList = new ArrayList<>();
                            for (int i = 0; i < totalCarNum; i++) {
                                markerList.add(aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_car)).anchor(0.5f, 0.5f)));
                                smoothMarkerList.add(new MovingPointOverlay(aMap, markerList.get(markerList.size() - 1)));
                            }
                            LatLng drivePoint = mLatLngsOfPath.get(0);
                            Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(mLatLngsOfPath, drivePoint);
                            mLatLngsOfPath.set(pair.first, drivePoint);
                            for (int i = 0; i  < smoothMarkerList.size(); i++){
                                smoothMarkerList.get(i).setPoints(mLatLngsOfPath.subList(pair.first + i*4, mLatLngsOfPath.size()));
                                smoothMarkerList.get(i).setTotalDuration(1000 - i*10);
                            }
                            for (MovingPointOverlay sm : smoothMarkerList){
                                sm.startSmoothMove();
                            }
                            // 设置  自定义的InfoWindow 适配器
                            aMap.setInfoWindowAdapter(infoWindowAdapter);
                            // 显示 infowindow
                            markerList.get(0).showInfoWindow();
                            // 设置移动的监听事件  返回 距终点的距离  单位 米
                            smoothMarkerList.get(0).setMoveListener(new MovingPointOverlay.MoveListener() {
                                @Override
                                public void move(double v) {
                                    runOnUiThread(() -> {
                                        if(title != null){
                                            title.setText( "距离终点还有： " + (int) v + "米");
                                            if(curNum == 0){
                                                prisoner_icon.setImageResource(R.mipmap.prisoner_1);
                                                prisoner_name.setText("周雷");
                                                police_name.setText("张平");
                                                car_no.setText("京PFT838");
                                            }else if(curNum == 1){
                                                prisoner_icon.setImageResource(R.mipmap.prisoner_4);
                                                prisoner_name.setText("张中");
                                                police_name.setText("马飞");
                                                car_no.setText("京BV4151");
                                            }else {
                                                prisoner_icon.setImageResource(R.mipmap.prisoner_5);
                                                prisoner_name.setText("尹狄勇");
                                                police_name.setText("杜和平");
                                                car_no.setText("京AWG392");
                                            }
                                        }
                                    });
                                }
                            });
                        } else if (result != null && result.getPaths() == null) {
                            Toast.makeText(getApplicationContext(), R.string.no_result, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_result,Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "errorCode：" + errorCode, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

            }
        });
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, 2, null, null, "");
        routeSearch.calculateDriveRouteAsyn(query);

        receiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);

//        initPython();
//        callPythonCode();

        Log.i(TAG, "INITMAINACTIVITY");
        handler.postDelayed(runnable, 5000);
    }

    public void postDeviceInfo(String cpu, String mem) {
        HashMap<String, String> params = new HashMap<>(4);
        params.put("deviceNo", mainApplication.getDeviceNo());
        params.put("cpuUsageRate", cpu);
        params.put("memoryUsageRate", mem);
        params.put("dumpEnergyRate", battery);
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn("dvcM/deviceRunInfo/upload", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
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

    public void showDialog() {
        if(warnDialog == null) {
            warnDialog = WarnDialog.showDialog(this, "");
        }
        warnDialog.show();
    }

    public void destroyDialog() {
        if(warnDialog != null) {
            warnDialog.dismiss();
        }
    }

    /**
     * 初始化Python环境
     */
    public void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    /**
     * 调用python代码
     */
    public void callPythonCode(){
        Python py = Python.getInstance();
        List<double[]> list = new ArrayList<>();
        list.add(new double[]{22.0, 1.0, 0.0, 172.7, 76.7, 2.0, 2.0, 2.0, 2.0, 0.0, 0.0, 2.0, 4.0, 0.0, 0.989, 0.0});
        list.add(new double[]{28.0,2.0,0.0,175.3,90.7,0.0,1.0,1.0,2.0,1.0,0.0,10.0,13.0,2.0,0.897,1.0});
        list.add(new double[]{29.0,2.0,0.0,177.8,88.0,2.0,3.0,2.0,2.0,1.0,0.0,5.0,8.0,0.0,0.776,1.0});
        String filename = Environment.getDataDirectory().toString();
        Log.i("path", filename);
        // 调用hello.py模块中的greet函数，并传一个参数，等价用法：py.getModule("hello").get("greet").call("Android");
//        PyObject obj = PyObject.fromJava();
        py.getModule("model").callAttr("test", filename);
    }

    /**
     *  个性化定制的信息窗口视图的类
     *  如果要定制化渲染这个信息窗口，需要重载getInfoWindow(Marker)方法。
     *  如果只是需要替换信息窗口的内容，则需要重载getInfoContents(Marker)方法。
     */
    TextView title, prisoner_name, police_name, car_no;
    ImageView prisoner_icon;
    AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter(){

        // 个性化Marker的InfoWindow 视图
        // 如果这个方法返回null，则将会使用默认的信息窗口风格，内容将会调用getInfoContents(Marker)方法获取
        @Override
        public View getInfoWindow(Marker marker) {
            currentMarker = marker;
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_window,null);
            render(marker, infoWindow);
            return infoWindow;
        }

        // 这个方法只有在getInfoWindow(Marker)返回null 时才会被调用
        // 定制化的view 做这个信息窗口的内容，如果返回null 将以默认内容渲染
        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };

    public void render(Marker marker, View view) {
        //如果想修改自定义Infow中内容，请通过view找到它并修改
        title = (TextView) view.findViewById(R.id.title);
        prisoner_icon = (ImageView) view.findViewById(R.id.escort_image);
        prisoner_name = (TextView) view.findViewById(R.id.prisoner_name);
        police_name = (TextView) view.findViewById(R.id.police_name);
        car_no = (TextView) view.findViewById(R.id.car_no);
        title.setText("距离终点还有： " + " " + "米");
        prisoner_icon.setImageResource(R.mipmap.escort_dog);
        prisoner_name.setText("");
        police_name.setText("");
        car_no.setText("");
    }

    public void showPreCarInfo(View view){
        if(aMap!=null && markerList.size()!= 0){
            curNum = (curNum + totalCarNum+1)%totalCarNum;
            markerList.get(curNum).showInfoWindow();
        }
    }

    public void showNextCarInfo(View view){
        if(aMap!=null && markerList.size()!= 0){
            curNum = (curNum + totalCarNum-1)%totalCarNum;
            markerList.get(curNum).showInfoWindow();
        }
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    public void removeFromMap() {
        try {
            if (startMarker != null) {
                startMarker.remove();
            }
            if (endMarker != null) {
                endMarker.remove();
            }
            for (Marker marker : stationMarkers) {
                marker.remove();
            }
            for (Polyline line : allPolyLines) {
                line.remove();
            }
            if (this.throughPointMarkerList != null
                    && this.throughPointMarkerList.size() > 0) {
                for (int i = 0; i < this.throughPointMarkerList.size(); i++) {
                    this.throughPointMarkerList.get(i).remove();
                }
                this.throughPointMarkerList.clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void zoomToSpan() {
        if (mStartPoint != null) {
            if (aMap == null){
                return;
            }
            try {
                LatLngBounds bounds = getLatLngBounds();
                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        b.include(new com.amap.api.maps.model.LatLng(mStartPoint.getLatitude(), mStartPoint.getLongitude()));
        b.include(new LatLng(mEndPoint.getLatitude(), mEndPoint.getLongitude()));
        return b.build();
    }

    private void addStationMarker(MarkerOptions options) {
        if(options == null) {
            return;
        }
        Marker marker = aMap.addMarker(options);
        if(marker != null) {
            stationMarkers.add(marker);
        }
    }

    /**
     * 路段节点图标控制显示接口。
     * @param visible true为显示节点图标，false为不显示。
     * @since V2.3.1
     */
    public void setNodeIconVisibility(boolean visible) {
        try {
            nodeIconVisible = visible;
            if (stationMarkers != null && stationMarkers.size() > 0) {
                for (int i = 0; i < stationMarkers.size(); i++) {
                    stationMarkers.get(i).setVisible(visible);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setIsColorfulline(boolean iscolorfulline) {
        this.isColorfulline = iscolorfulline;
    }

    /**
     * 添加驾车路线添加到地图上显示。
     */
    public void addToMap() {
        initPolylineOptions();
        try {
            if (aMap == null) {
                return;
            }
            if (mWidth == 0 || drivePath == null) {
                return;
            }
            mLatLngsOfPath = new ArrayList<LatLng>();
            tmcs = new ArrayList<TMC>();
            List<DriveStep> drivePaths = drivePath.getSteps();
            startPoint = convertToLatLng(mStartPoint);
            endPoint = convertToLatLng(mEndPoint);
            mPolylineOptions.add(startPoint);
            for (DriveStep step : drivePaths) {
                List<LatLonPoint> latlonPoints = step.getPolyline();
                List<TMC> tmclist = step.getTMCs();
                tmcs.addAll(tmclist);
                addDrivingStationMarkers(step, convertToLatLng(latlonPoints.get(0)));
                for (LatLonPoint latlonpoint : latlonPoints) {
                    mPolylineOptions.add(convertToLatLng(latlonpoint));
                    mLatLngsOfPath.add(convertToLatLng(latlonpoint));
                }
            }
            mPolylineOptions.add(endPoint);
            if (startMarker != null) {
                startMarker.remove();
                startMarker = null;
            }
            if (endMarker != null) {
                endMarker.remove();
                endMarker = null;
            }
            addStartAndEndMarker();
            addThroughPointMarker();
            if (isColorfulline && tmcs.size()>0 ) {
                colorWayUpdate(tmcs);
            }else {
                addPolyLine(mPolylineOptions);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据不同的路段拥堵情况展示不同的颜色
     * @param tmcSection
     */
    private void colorWayUpdate(List<TMC> tmcSection) {
        if (aMap == null) {
            return;
        }
        if (tmcSection == null || tmcSection.size() <= 0) {
            return;
        }
        TMC segmentTrafficStatus;
        addPolyLine(new PolylineOptions().add(startPoint,
                convertToLatLng(tmcSection.get(0).getPolyline().get(0)))
                .setDottedLine(true));
        String status = "";
        for (int i = 0; i < tmcSection.size(); i++) {
            segmentTrafficStatus = tmcSection.get(i);
            List<LatLonPoint> mployline = segmentTrafficStatus.getPolyline();
            if (status.equals(segmentTrafficStatus.getStatus())) {
                for (int j = 1; j < mployline.size(); j++) {
                    //第一个点和上一段最后一个点重复，这个不重复添加
                    mPolylineOptionscolor.add(convertToLatLng(mployline.get(j)));
                }
            }else {
                if (mPolylineOptionscolor != null) {
                    addPolyLine(mPolylineOptionscolor.color(getcolor(status)));
                }
                mPolylineOptionscolor = null;
                mPolylineOptionscolor = new PolylineOptions().width(mWidth);
                status = segmentTrafficStatus.getStatus();
                for (int j = 0; j < mployline.size(); j++) {
                    mPolylineOptionscolor.add(convertToLatLng(mployline.get(j)));
                }
            }
            if (i == tmcSection.size()-1 && mPolylineOptionscolor != null) {
                addPolyLine(mPolylineOptionscolor.color(getcolor(status)));
                addPolyLine(new PolylineOptions().add(
                        convertToLatLng(mployline.get(mployline.size()-1)), endPoint)
                        .setDottedLine(true));
            }
        }
    }

    private int getcolor(String status) {
        if (status.equals("畅通")) {
            return Color.GREEN;
        } else if (status.equals("缓行")) {
            return Color.YELLOW;
        } else if (status.equals("拥堵")) {
            return Color.RED;
        } else if (status.equals("严重拥堵")) {
            return Color.parseColor("#990033");
        } else {
            return Color.parseColor("#537edc");
        }
    }

    private void addPolyLine(PolylineOptions options) {
        if(options == null) {
            return;
        }
        Polyline polyline = aMap.addPolyline(options);
        if(polyline != null) {
            allPolyLines.add(polyline);
        }
    }

    private void addThroughPointMarker() {
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            LatLonPoint latLonPoint = null;
            for (int i = 0; i < this.throughPointList.size(); i++) {
                latLonPoint = this.throughPointList.get(i);
                if (latLonPoint != null) {
                    throughPointMarkerList.add(aMap
                            .addMarker((new MarkerOptions())
                                    .position(
                                            new LatLng(latLonPoint
                                                    .getLatitude(), latLonPoint
                                                    .getLongitude()))
                                    .visible(throughPointMarkerVisible)
                                    .icon(getThroughPointBitDes())
                                    .title("\u9014\u7ECF\u70B9")));
                }
            }
        }
    }

    private BitmapDescriptor getThroughPointBitDes() {
        return BitmapDescriptorFactory.fromResource(R.mipmap.amap_through);
    }

    private void addStartAndEndMarker() {
        startMarker = aMap.addMarker((new MarkerOptions())
                .position(startPoint).icon(getStartBitmapDescriptor())
                .title("\u8D77\u70B9"));
        endMarker = aMap.addMarker((new MarkerOptions()).position(endPoint)
                .icon(getEndBitmapDescriptor()).title("\u7EC8\u70B9"));
    }

    /**
     * 给起点Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     * @return 更换的Marker图片。
     * @since V2.1.0
     */
    private BitmapDescriptor getStartBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.mipmap.amap_start);
    }
    private BitmapDescriptor getEndBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.mipmap.amap_end);
    }
    private BitmapDescriptor getDriveBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.mipmap.amap_car);
    }

    private void addDrivingStationMarkers(DriveStep driveStep, LatLng latLng) {
        addStationMarker(new MarkerOptions()
                .position(latLng)
                .title("\u65B9\u5411:" + driveStep.getAction()
                        + "\n\u9053\u8DEF:" + driveStep.getRoad())
                .snippet(driveStep.getInstruction()).visible(nodeIconVisible)
                .anchor(0.5f, 0.5f).icon(getDriveBitmapDescriptor()));
    }

    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    private void initPolylineOptions() {
        mPolylineOptions = null;
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.parseColor("#537edc")).width(18f);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed(MainActivity)");
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

        if (id == R.id.nav_task) {
            // Handle the camera action
        } else if (id == R.id.nav_info) {

        } else if (id == R.id.nav_slideshow) {

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
                battery = level + "";
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult(MainActivity)");
        if(resultCode == 1) {
            setContentView(R.layout.activity_main);
            STATUS = true;
            initMainLayout(savedInstanceState);
        }else if(resultCode == 2) {
            stopRunnable = false;
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(receiver, intentFilter);
            handler.postDelayed(runnable, 5000);
        }else if(resultCode == 3) {
            finish();
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
        Log.i(TAG, STATUS+"=========");
        if(STATUS){
            if(receiver != null) {
                unregisterReceiver(receiver);
            }
            for (int i = 0; i < smoothMarkerList.size(); i++){
                if(smoothMarkerList.get(i) != null) {
                    smoothMarkerList.get(i).setMoveListener(null);
                    smoothMarkerList.get(i).destroy();
                }
            }        // 销毁平滑移动marker
            mMapView.onDestroy();
            stopRunnable = true;
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause(MainActivity)");
        if(STATUS){
            mMapView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume(MainActivity)");
        if(STATUS){
            mMapView.onResume();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart(MainActivity)");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(STATUS){
            mMapView.onSaveInstanceState(outState);
        }
    }
}
