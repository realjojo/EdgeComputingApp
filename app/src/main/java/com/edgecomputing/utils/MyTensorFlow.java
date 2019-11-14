package com.edgecomputing.utils;

import android.content.res.AssetManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/12 9:52
 */
public class MyTensorFlow {
    private static final String MODEL_FILE = "file:///android_asset/test_model.pb"; //模型存放路径
    private Graph graph_;
    private Session session_;

    private static final String TAG = "MyTensorFlow";

    private TensorFlowInferenceInterface inferenceInterface;

    static {
        //加载库文件
        System.loadLibrary("tensorflow_inference");
    }

    public MyTensorFlow(AssetManager assetManager) {
        //接口定义
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public boolean initTensorFlow() {
        // 新建Graph
        graph_ = new Graph();
        // 加载pb到Graph
        graph_ = inferenceInterface.graph();
        // 初始化session
        session_ = new Session(graph_);
        if (session_ == null) {
            return false;
        }
        return true;
    }

    public void runTensorFlow() {
        float[][] input = new float[1][2];

        input[0][0] = 1;
        input[0][1] = 2;

        // 定义输入tensor
        Tensor inputTensor = Tensor.create(input);

        // 指定输入，输出节点，运行并得到结果
        Tensor resultTensor = session_.runner()
                .feed("x_input", inputTensor)
                .fetch("cal_node")
                .run()
                .get(0);

        float[][] dst = new float[1][1];
        resultTensor.copyTo(dst);

        // 处理结果
        ArrayList<Float> resultList = new ArrayList<>();
        for (float val : dst[0]) {
            if (val != 0) {
                resultList.add(val);
                Log.i(TAG, val+"");
            } else {
                break;
            }
        }
    }

//    public float[] getAddResult() {
//        //为输入数据赋值
//        inputs[0]=1;
//        inputs[1]=3;
//
//        //将数据feed给tensorflow
//        Trace.beginSection("feed");
//        inferenceInterface.feed(inputName, inputs, WIDTH, HEIGHT);
//        Trace.endSection();
//
//        //运行乘2的操作
//        Trace.beginSection("run");
//        String[] outputNames = new String[] {outputName};
//        inferenceInterface.run(outputNames);
//        Trace.endSection();
//
//        //将输出存放到outputs中
//        Trace.beginSection("fetch");
//        inferenceInterface.fetch(outputName, outputs);
//        Trace.endSection();
//
//        return outputs;
//    }
}
