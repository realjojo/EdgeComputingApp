package com.edgecomputing.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.edgecomputing.activity.LoginActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: jojo
 * @Date: Created on 2019/4/22 18:38
 */
public class CommonUtil {

    private static final String TAG = "CommonUtil";

    /**从指定路径文件中读取数据
     * filepath： 读取文件的路径 /mnt/sdcard/uartrw/uartrw.txt
     * over:	写入文件方式，true：追加新内容； false：覆盖原文件
     */
    public static String ReadFromFile(String folderpath) throws IOException {
        Log.i(TAG, "开始读取文件!");
        //声明流对象
//        FileInputStream fis = null;
//        try{
//            fis = new FileInputStream(folderpath);
//            InputStreamReader reader = new InputStreamReader(fis, "GBK");
//            //读取数据
//            BufferedReader br = new BufferedReader(reader);
//            br.readLine();
//            String s = br.readLine();
        File file = new File(folderpath);
        String s = readLastLine(file,null);
        if(s != null) {
            String[] str = s.split("]:");
            if(str[1].trim().equals("AAEE0000FF")) {
                Calendar c = Calendar.getInstance();
                int curMinute = c.get(Calendar.SECOND);
                String[] recordTime = str[0].split(":");
//                记录插入时间在当前时间的一秒钟之内，报警
                if(curMinute-1 <= Integer.parseInt(recordTime[2])) {
                    return "danger";
                }else {
//                否则，记录是之前已报警的记录，不报警
                    return "safe";
                }
            } else {
                return "safe";
            }
        } else {
            return "safe";
        }
//        }catch(Exception e){
//            e.printStackTrace();
//        }finally{
//            try{
//                //关闭流，释放资源
//                fis.close();
//            }catch(Exception e){}
//        }
//        return "safe";
    }

    public static String readLastLine(File file, String charset) throws IOException {
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            return null;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            long len = raf.length();
            if (len == 0L) {
                return "";
            } else {
                long pos = len - 1;
                while (pos > 0) {
                    pos--;
                    raf.seek(pos);
                    if (raf.readByte() == '\n') {
                        break;
                    }
                }
                if (pos == 0) {
                    raf.seek(0);
                }
                byte[] bytes = new byte[(int) (len - pos)];
                raf.read(bytes);
                if (charset == null) {
                    return new String(bytes);
                } else {
                    return new String(bytes, charset);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前连接的wifi Ip
     * @param context
     * @return
     */
    public static int getWifiIp(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wm.getConnectionInfo();
        int ipAddr = 0;
        if(info != null) {
            //获取ip地址
            ipAddr = info.getIpAddress();
        }
        return ipAddr;
    }

    /**
     * 当前Wifi连接状态
     * @param context
     * @return
     */
    public static boolean isWifiEnabled(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo.isConnected();
        } else {
            return false;
        }
    }

    public static String getPredicationResult(double[] infos){
        String prediction_r = "";
        double[] result = {0,0,0,0};
        result[3] = infos[14];
        if (infos[6]>=2 ){
            result[0]=1;
        }
        if(infos[7]>0){
            result[1]=1;
        }
        if(infos[15]>0){
            result[2] = 1;
        }
        double re = result[0]*0.1+result[1]*0.1+result[2]*0.15+result[3]*0.65;
        if(re<0.25) {
            prediction_r = "0";
        } else if(re>=0.25&&re<0.5) {
            prediction_r = "1";
        } else if(re>=0.5&&re<0.75) {
            prediction_r = "2";
        } else if(re>=0.75) {
            prediction_r = "3";
        }
        return prediction_r;
    }

    /**
     * 此方法用于从csv中读取数据
     * 方法本身应该是实时获得犯人心率数据并输入模型的，但由于没有连上设备，故直接从csv中读取模拟数据
     */
    public static List<int[]> generateData(String path, int row) {
        List<int[]> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return list;
        }
        FileInputStream fiStream;
        Scanner scanner;
        try {
            fiStream = new FileInputStream(file);
            scanner = new Scanner(fiStream,"UTF-8");
            scanner.nextLine();//读下一行,把表头越过
            int count = row;
            while (scanner.hasNextLine() && count > 0) {
                String sourceString = scanner.nextLine();
                Log.e("source-->", sourceString);
                Pattern pattern = Pattern.compile("[^,]*,");
                Matcher matcher = pattern.matcher(sourceString);
                int [] oneData = new int [17];
                int i = 0;
                while(matcher.find()) {
                    String find = matcher.group().replace(",", "");
                    oneData[i] = Integer.parseInt(find.trim());
                    i++;
                }
                list.add(oneData);
                count--;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "文件不存在");
            e.printStackTrace();
        }
        return list;
    }

    public static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(statusColor);
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        //让view不根据系统窗口来调整自己的布局
        ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
            ViewCompat.requestApplyInsets(mChildView);
        }
    }

}
