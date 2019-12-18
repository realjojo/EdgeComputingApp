package com.edgecomputing.utils;

import android.os.Environment;
import android.util.Log;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.TargetField;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/20 22:23
 */
public class PmmlUtil {

    public static String runRisk() throws Exception {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String pathxml = path + "/risk_model/model_r.pmml";
        Map<String, Double> map = new HashMap<>();
        Map< String, ArrayList<Object>> maps = getArry();
        int total = maps.get("results").size();
        ArrayList< Map<String,Object> > differentItems = new ArrayList<>();
        for(int i = 0;i<2;i++){
            double[] array = (double[])maps.get("arrays").get(i);
            int res = Integer.valueOf(predictLrHeart(pathxml, array));
            int true_value = (int)maps.get("results").get(i);
            if (res!=true_value){
                Map<String,Object> diffMap = new HashMap<>();
                diffMap.put("true_value",true_value);
                diffMap.put("items",maps.get("arrays").get(i));
                diffMap.put("test_value",res);
                differentItems.add(diffMap);
            }
        }
        double auc = 1-(differentItems.size()/total);
        return String.valueOf(auc);
    }

    private static Map< String, ArrayList<Object>> getArry(){
        ArrayList<Object> array = new ArrayList<>();
        ArrayList<Object> results = new ArrayList<>();
        Map< String, ArrayList<Object>> maps = new HashMap<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = path + "/risk_model/test.csv";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));// 文件名
            reader.readLine();//第一行信息，为标题信息，不用,如果需要，注释掉
            String line = null;
            while ((line = reader.readLine()) != null) {

                String item[] = line.split(",");// CSV格式文件为逗号分隔符文件，这里根据逗号切分
                int len = item.length;
                double[] newArray =new double [len-1];
                for(int i = 0; item.length-1 > i; i++){
                    newArray[i]=Double.valueOf(item[i]);
                }
                results.add(Integer.valueOf(item[item.length-1]));
                array.add(newArray);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        maps.put("results",results);
        maps.put("arrays",array);
        return maps;
    }

    public static String predictLrHeart(String pathxml, double[] array)throws Exception {
        PMML pmml = new PMML();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(pathxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(inputStream == null){
        }
        InputStream is = inputStream;
        try {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (JAXBException e1) {
            e1.printStackTrace();
        }finally {
            //关闭输入流
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        pmml = null;
//
        List<InputField> inputFields = evaluator.getInputFields();
        // 过模型的原始特征，从画像中获取数据，作为模型输入
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

        int index = 0;
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();

            FieldValue inputFieldValue = inputField.prepare((Object)array[index]);
            arguments.put(inputFieldName, inputFieldValue);
            index++;
        }

        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        List<TargetField> targetFields = evaluator.getTargetFields();
        //对于分类问题等有多个输出。
        ArrayList<String> strs = new ArrayList<>();
        for (TargetField targetField : targetFields) {
            FieldName targetFieldName = targetField.getName();
            Object targetFieldValue = results.get(targetFieldName);

            strs.add( targetFieldValue.toString());
        }

        return getResult(strs.toString());
    }

    private static String getResult(String str){
        String result = "";

        String regex = "result=[0-9]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        while(matcher.find()){
            String st = matcher.group().toString();
            result = st.substring(st.length()-1);
        }
        return result;
    }
}
