package com.edgecomputing.utils;

import android.app.Activity;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
