package com.edgecomputing.utils;


import android.util.Log;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: jojo
 * @Date: Created on 2019/4/16 21:20
 */
public class ThreadPoolHelper {

//    private static final Logger logger = (Logger) Logger.getLogger(ThreadPoolHelper.class);

    private static final int POOL_SIZE = 4;

    //订单任务线程池

    private static ThreadPoolExecutor comitTaskPool =(ThreadPoolExecutor) new ScheduledThreadPoolExecutor(POOL_SIZE);


    /**
     * 执行任务
     * @param comitTask
     */
    public static void executeTask(Runnable comitTask) {
        comitTaskPool.execute(comitTask);
        Log.i("【线程池任务】线程池中线程数：", comitTaskPool.getPoolSize() + "");
        Log.i("【线程池任务】队列中等待执行的任务数：", comitTaskPool.getQueue().size() + "");
        Log.i("【线程池任务】已执行完任务数：", comitTaskPool.getCompletedTaskCount() + "");
    }


    /**
     * 关闭线程池
     */
    public static void shutdown() {
        Log.i("CloseThreadPool", "shutdown comitTaskPool...");
        comitTaskPool.shutdown();
        try {
            if (!comitTaskPool.isTerminated()) {
                Log.i("CloseThreadPool", "直接关闭失败[" + comitTaskPool.toString() + "]");
                comitTaskPool.awaitTermination(3, TimeUnit.SECONDS);
                if (comitTaskPool.isTerminated()) {
                    Log.i("CloseThreadPool", "成功关闭[" + comitTaskPool.toString() + "]");
                } else {
                    Log.i("CloseThreadPool", "[" + comitTaskPool.toString() + "]关闭失败，执行shutdownNow...");
                    if (comitTaskPool.shutdownNow().size() > 0) {
                        Log.i("CloseThreadPool", "[" + comitTaskPool.toString() + "]没有关闭成功");
                    } else {
                        Log.i("CloseThreadPool", "shutdownNow执行完毕，成功关闭[" + comitTaskPool.toString() + "]");
                    }
                }
            } else {
                Log.i("CloseThreadPool", "成功关闭[" + comitTaskPool.toString() + "]");
            }
        } catch (InterruptedException e) {
            Log.e("CloseThreadPool", "接收到中断请" + comitTaskPool.toString() + "停止操作");
        }
    }

}
