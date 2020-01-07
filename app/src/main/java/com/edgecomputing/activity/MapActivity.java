package com.edgecomputing.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
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
import com.edgecomputing.R;
import com.edgecomputing.application.MainApplication;
import com.edgecomputing.utils.LocationUtil;
import com.edgecomputing.utils.OkHttpUtil;
import com.edgecomputing.utils.WarnDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/14 17:30
 */
public class MapActivity extends AppCompatActivity {

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

    private boolean flag;
    private static Context context;
    private Button getGps;

    private WarnDialog warnDialog;
    private MainApplication mainApplication;
    private String selectServerAddress;
    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(MapActivity)");
        setContentView(R.layout.activity_map);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("押解轨迹展示");
        mMapView =  (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mainApplication = (MainApplication) getApplication();
        selectServerAddress = mainApplication.getServerAddress();
        if(selectServerAddress == null) {
            selectServerAddress = "http://10.109.246.55:8089";
        }
        init();
    }

    private void init() {
        Log.i(TAG, "init(MapActivity)");
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        context = MapActivity.this;
        getGps = (Button) findViewById(R.id.map_get_gps);
        getGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    getGPSLocation();
                } else {
                    Toast.makeText(getApplicationContext(), "no permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            flag = grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void postGPS(double longitude, double latitude) {
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", mainApplication.getUserId());
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        OkHttpUtil.getInstance(getBaseContext()).requestAsyn(selectServerAddress, "prisonerData/upload2", OkHttpUtil.TYPE_POST_FORM, params, new OkHttpUtil.ReqCallBack<String>() {
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

    /**
     * 通过GPS获取定位信息
     */
    public void getGPSLocation() {
        Location gps = LocationUtil.getGPSLocation(context);
        if (gps == null) {
            //设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
            LocationUtil.addLocationListener(context, LocationManager.GPS_PROVIDER, new LocationUtil.ILocationListener() {
                @Override
                public void onSuccessLocation(Location location) {
                    if (location != null) {
                        postGPS(location.getLongitude(), location.getLatitude());
                        Toast.makeText(getApplicationContext(), "gps onSuccessLocation location:  lat==" + location.getLatitude() + "     lng==" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "gps location is null", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            postGPS(gps.getLongitude(), gps.getLatitude());
            Toast.makeText(this, "gps location: lat==" + gps.getLatitude() + "  lng==" + gps.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过网络等获取定位信息
     */
    private void getNetworkLocation() {
        Location net = LocationUtil.getNetWorkLocation(this);
        if (net == null) {
            Toast.makeText(this, "net location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "network location: lat==" + net.getLatitude() + "  lng==" + net.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 采用最好的方式获取定位信息
     */
    private void getBestLocation() {
        Criteria c = new Criteria();//Criteria类是设置定位的标准信息（系统会根据你的要求，匹配最适合你的定位供应商），一个定位的辅助信息的类
        c.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        c.setAltitudeRequired(false);//设置不需要海拔
        c.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        c.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度
        //... Criteria 还有其他属性，就不一一介绍了
        Location best = LocationUtil.getBestLocation(this, c);
        if (best == null) {
            Toast.makeText(this, " best location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "best location: lat==" + best.getLatitude() + " lng==" + best.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showDialog() {
        if(warnDialog == null) {
            warnDialog = WarnDialog.showDialog(this, "");
        }
        warnDialog.show();
    }

    private void destroyDialog() {
        if(warnDialog != null) {
            warnDialog.dismiss();
        }
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
                    addPolyLine(mPolylineOptionscolor.color(getColor(status)));
                }
                mPolylineOptionscolor = null;
                mPolylineOptionscolor = new PolylineOptions().width(mWidth);
                status = segmentTrafficStatus.getStatus();
                for (int j = 0; j < mployline.size(); j++) {
                    mPolylineOptionscolor.add(convertToLatLng(mployline.get(j)));
                }
            }
            if (i == tmcSection.size()-1 && mPolylineOptionscolor != null) {
                addPolyLine(mPolylineOptionscolor.color(getColor(status)));
                addPolyLine(new PolylineOptions().add(
                        convertToLatLng(mployline.get(mployline.size()-1)), endPoint)
                        .setDottedLine(true));
            }
        }
    }

    private int getColor(String status) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult(MapActivity)");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart(MapActivity)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy(MapActivity)");
        if(smoothMarkerList != null && smoothMarkerList.size() > 0) {
            for (int i = 0; i < smoothMarkerList.size(); i++){
                if(smoothMarkerList.get(i) != null) {
                    smoothMarkerList.get(i).setMoveListener(null);
                    smoothMarkerList.get(i).destroy();
                }
            }
        }
        mMapView.onDestroy();
        LocationUtil.unRegisterListener(context);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause(MapActivity)");
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume(MapActivity)");
        mMapView.onResume();
        initPermission();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart(MapActivity)");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

}
