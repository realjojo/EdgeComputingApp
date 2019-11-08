package com.edgecomputing.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.edgecomputing.application.MainApplication;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;

/**
 * @Author: jojo
 * @Date: Created on 2019/4/16 16:12
 */
public class AndroidWebSocketClient extends WebSocketClient {

    private Context context;

    private static final String url = "ws://10.108.120.33:8080/ws/webSocket/2";

    //由于WebSocketClient对象是不能重复使用的，所以将AndroidWebSocketClient写为单例模式
    private static AndroidWebSocketClient mInstance;
//    private MainApplication mainApplication;
    private static final String TAG = "WebSocket";

    /**
     * 私有构造方法
     * @param context
     */
    private AndroidWebSocketClient(Context context) {
        //开启webSocket客户端
        super(URI.create(url));
        this.context = context;
//        mainApplication = application;
    }

    /**
     * 公有方法，返回单例对象
     * 这样我们就可以从外部对MyWebSocketClient进行初始化并开启链接
     * @param context
     * @return
     */
    public static AndroidWebSocketClient getInstance(Context context) {
        //单例：考虑线程安全问题, 两种方式: 1. 给方法加同步锁 synchronized, 效率低; 2. 给创建对象的代码块加同步锁
        if(mInstance == null) {
            synchronized (AndroidWebSocketClient.class) {
                if (mInstance == null) {
                    mInstance = new AndroidWebSocketClient(context);
                }
            }
        }
        return mInstance;
    }

    public AndroidWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    /**
     * 长链接开启
     * @param handshakedata
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("WebSocket", "WebSocket长链接打开 onOpen");
//        HashMap<String, String> pp = new HashMap<>();
//        pp.put("deviceNo", mainApplication.getDeviceNo());
//        OkHttpUtil.getInstance(context).requestAsyn("dvcM/devices/login", OkHttpUtil.TYPE_PUT, pp, new OkHttpUtil.ReqCallBack<String>(){
//
//            @Override
//            public void onReqSuccess(String result) {
//                Log.i(TAG, "设备接入WebSocket连接");
//            }
//
//            @Override
//            public void onReqFailed(String errorMsg) {
//                Log.e(TAG, errorMsg);
//            }
//        });
    }

    /**
     * 消息通道收到消息
     * @param message
     */
    @Override
    public void onMessage(String message) {
        Log.i("收到服务器发来的消息", message);
    }

    /**
     * 长链接关闭
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("WebSocket", "WebSocket长链接关闭 onClose");
        Log.e("WebSocket onClose", reason);
//        HashMap<String, String> pp = new HashMap<>();
//        pp.put("deviceNo", mainApplication.getDeviceNo());
//        OkHttpUtil.getInstance(context).requestAsyn("dvcM/devices/logout", OkHttpUtil.TYPE_PUT, pp, new OkHttpUtil.ReqCallBack<String>(){
//
//            @Override
//            public void onReqSuccess(String result) {
//                Log.i(TAG, "设备断开WebSocket连接");
//            }
//
//            @Override
//            public void onReqFailed(String errorMsg) {
//                Log.e(TAG, errorMsg);
//            }
//        });
    }

    /**
     * 链接发生错误
     * @param ex
     */
    @Override
    public void onError(Exception ex) {
        Log.i("WebSocket", "WebSocket长链接发生错误 onError");
        Log.e("WebSocket", ex.getMessage());
    }
}
