package com.edgecomputing.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jojo
 * @Date: Created on 2019/4/9 16:09
 */
public class CpuMonitor {

    /**
     * 实时获取CPU当前频率（单位KHZ）
     */
    public static String getCurCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取cpu使用率
     * @return
     */
    public static String getCpuRate(){
        //采样第一次cpu信息快照
        Map<String,String> map1 = getMap();
        //总的CPU时间totalTime = user+nice+system+idle+iowait+irq+softirq
        long totalTime1 =getTime(map1);
        System.out.println(totalTime1+"...........................totalTime1.");
        //获取idleTime1
        long idleTime1 = Long.parseLong(map1.get("idle"));
        System.out.println(idleTime1 + "...................idleTime1");
        //间隔360ms
        try {
            Thread.sleep(360);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //采样第二次cpu信息快照
        Map<String,String> map2 = getMap();
        long totalTime2 = getTime(map2);
        System.out.println(totalTime2+"............................totalTime2");
        //获取idleTime1
        long idleTime2 = Long.parseLong(map2.get("idle"));
        System.out.println(idleTime2+"................idleTime2");

        //得到cpu的使用率
        int cpuRate = (int)(100*((totalTime2-totalTime1)-(idleTime2-idleTime1))/(totalTime2-totalTime1));
        return cpuRate + "%";
    }

    /**
     * 得到cpu信息
     */
    public static long getTime(Map<String,String> map){
        long totalTime = Long.parseLong(map.get("user")) + Long.parseLong(map.get("nice"))
                + Long.parseLong(map.get("system")) + Long.parseLong(map.get("idle"))
                + Long.parseLong(map.get("iowait")) + Long.parseLong(map.get("irq"))
                + Long.parseLong(map.get("softirq"));
        return totalTime;
    }

    /**
     * 采样CPU信息快照的函数，返回Map类型
     */
    public static Map<String,String> getMap(){
        String[] cpuInfos = null;
        //读取cpu信息文件
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")));
            String load = br.readLine();
            br.close();
            cpuInfos = load.split(" ");
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("线程异常");
            e.printStackTrace();
        }
        Map<String,String> map = new HashMap<>();
        map.put("user",cpuInfos[2]);
        map.put("nice",cpuInfos[3]);
        map.put("system",cpuInfos[4]);
        map.put("idle",cpuInfos[5]);
        map.put("iowait",cpuInfos[6]);
        map.put("irq",cpuInfos[7]);
        map.put("softirq",cpuInfos[8]);
        return map;
    }

}
