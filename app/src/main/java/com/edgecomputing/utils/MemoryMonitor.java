package com.edgecomputing.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Author: jojo
 * @Date: Created on 2019/4/10 21:26
 */
public class MemoryMonitor {

    private static ActivityManager activityManager;

    public synchronized static ActivityManager getActivityManager(Context context) {
        if (activityManager == null) {
            activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return activityManager;
    }

    /**
     * 获取系统总内存,返回字节单位为KB
     * @return 系统总内存
     */
    public static long getTotalMemory() {
        long totalMemorySize = 0;
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            // 将非数字的字符替换为空
            totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalMemorySize;
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位
     * @param context 可传入应用程序上下文
     * @return 当前可用内存
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 计算已使用内存的百分比，并返回
     * @param context 可传入应用程序上下文
     * @return 已使用内存的百分比，以字符串形式返回
     */
    public static String getMemoryRate(Context context) {
        long totalMemorySize = getTotalMemory();
        long availableSize = getAvailableMemory(context) / 1024;
        int memoryRate = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
        return memoryRate + "";
    }

}
