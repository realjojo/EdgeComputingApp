package com.edgecomputing.utils;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.Arrays;

public class BaseOperations {

	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num)
	{
		return num & 0x1;
	}

    //Hex字符串转byte
    static public byte HexToByte(String inHex) {
    	return (byte)Integer.parseInt(inHex,16);
    }

	/*将字符串作为十六进制数转换为十六进制形式*/
	public static byte[] stringAsHex(String str) {
        int n=0;
        String ss=str;
        while(ss.contains(" ")){
            ss=ss.substring(ss.indexOf(" ")+1);
            n++;
        }
		byte[] txData = new byte[(str.length()-n)/2];
		int i=0,j=0;
		for(;i<str.length()-1;i++){
			if(str.charAt(i)!=32){
		        Integer a = new java.math.BigInteger(str.substring(i, i+2), 16).intValue();
				txData[j] = (byte) (a&0xff);
				j++;
				i++;
			}
		}
		return txData;
	}

    //hex字符串转字节数组
	static public byte[] HexToByteArr(String inHex) {
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen)==1)
		{//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}else {//偶数
			result = new byte[(hexlen/2)];
		}
	    int j=0;
		for (int i = 0; i < hexlen; i+=2)
		{
			result[j]=HexToByte(inHex.substring(i,i+2));
			j++;
		}
	    return result; 
	}
	public static String byteToHex(byte[] sbyte,int len,String insterStr){
		String str = "";
		for(int i=0;i<len;i++){
			str += (sbyte[i]&0xff)<16?"0"+Integer.toHexString(sbyte[i]&0xff).toUpperCase() + insterStr:Integer.toHexString(sbyte[i]&0xff).toUpperCase() + insterStr;
		}
		return str;
	}

    /**
     * 十六进制数组转byte数组
     * @param data 要进行转换的十六进制数
     * @return 转换得到的byte数组
     */
    public static byte[] hexToByte(int[] data) {
        byte[] buf = new byte[data.length];
        for(int i=0;i<data.length;i++) {
            buf[i] = (byte) (data[i] & 0x000000ff);
        }
        return buf;
    }

    /**
     * 将用户输入的字符串转成16进制数
     * @param data 要进行转化的字符串
     * @return 转换完成后的16进制数
     */
    public static int[] stringToHex(String data) {
        data = data.toLowerCase();
        data = data.replace(" ","");
        Log.i("TAG","data="+data);
        Log.i("TAG","data.length="+data.length());
        int[] buf;
        if(data.length()%2==0){
            buf = new int[data.length()/2];
        }else{
            buf = new int[data.length()/2+1];
        }
        for(int i=0,j=0;i<data.length();i+=2,j++){
            char c1 = data.charAt(i);
            char c2;
            if(i+1==data.length()){
                c2 = 0;
            }else{
                c2 = data.charAt(i+1);
            }
            int n1 = charToHex(c1);
            int n2 = charToHex(c2);

            buf[j] = 16*n1+n2;
            Log.i("TAG","c1="+c1+" c2="+c2);
        }
        for(int k:buf){
            Log.i("TAG","k="+k);
        }
        return buf;
    }

    /**
     * 将char类型的数转换成16进制的数
     * @param c char数
     * @return 对应的16进制数
     */
    public static int charToHex(char c) {
        int n = 0;
        switch (c){
            case '0':n = 0X0;break;
            case '1':n = 0X1;break;
            case '2':n = 0X2;break;
            case '3':n = 0X3;break;
            case '4':n = 0X4;break;
            case '5':n = 0X5;break;
            case '6':n = 0X6;break;
            case '7':n = 0X7;break;
            case '8':n = 0X8;break;
            case '9':n = 0X9;break;
            case 'a':n = 0XA;break;
            case 'b':n = 0XB;break;
            case 'c':n = 0XC;break;
            case 'd':n = 0XD;break;
            case 'e':n = 0XE;break;
            case 'f':n = 0XF;break;
        }
        return n;
    }

    /**
     * 将10进制数转对应的16进制的字符串显示
     * @param i 要进行转换显示的十进制数
     * @return 对应的十六进制数
     */
    public static String dexToHex(int i) {
        StringBuilder builder = new StringBuilder();
        if(i==0){
            builder.append("00");
        }else{
            while(i>0){
                switch (i%16) {
                    case 10:
                        builder.insert(0, 'A');
                        break;
                    case 11:
                        builder.insert(0, 'B');
                        break;
                    case 12:
                        builder.insert(0, 'C');
                        break;
                    case 13:
                        builder.insert(0, 'D');
                        break;
                    case 14:
                        builder.insert(0, 'E');
                        break;
                    case 15:
                        builder.insert(0, 'F');
                        break;
                    default:
                        builder.insert(0,i%16);
                        break;
                }
                i /= 16;
            }
        }
        if(builder.length()==1) {
            builder.insert(0,"0");
        }
        return builder.toString();
    }

    //-------------------------------------------------------
    // 字节数组转转hex字符串
    public static String ByteArrToHex(byte[] inBytArr) {
		StringBuilder strBuilder = new StringBuilder();
		int j=inBytArr.length;
		for (int i = 0; i < j; i++) {
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}

	//获取心率数据
	public static String ByteArrToHeartRate(byte[] bytes) {
        return String.valueOf(HexStringToInt(Byte2Hex(bytes[3])));
    }

    //获取高度数据
    public static String ByteArrToHeight(byte[] bytes) {
        int res = (int) ((0x00 & 0xFF)
                        | ((long)(bytes[11] & 0xFF)<<24)
                        | ((long)(bytes[12] & 0xFF)<<16)
                        | ((long)(bytes[13] & 0xFF)<<8));
        return String.valueOf(Float.intBitsToFloat(res));
    }

    // 从byte数组的index处的连续4个字节获得一个float
    public static float getFloat(byte[] arr, int index) {
        byte[] bytes = new byte[4];
        bytes[0] = 0x00;
        bytes[1] = arr[index];
        bytes[2] = arr[index+1];
        bytes[3] = arr[index+2];
        return Float.intBitsToFloat(getInt(bytes));
    }

    // 从byte数组的index处的连续4个字节获得一个int
    public static int getInt(byte[] arr) {
        return 	(0xff000000 	& (arr[0] << 24))  |
                (0x00ff0000 	& (arr[1] << 16))  |
                (0x0000ff00 	& (arr[2] << 8))   |
                (0x000000ff 	&  arr[3]);
    }

    /**
     * 16进制字符串转十进制int
     * @param HexString
     * @return
     */
    public static int HexStringToInt(String HexString) {
        int inJTFingerLockAddress = Integer.valueOf(HexString, 16);
        return inJTFingerLockAddress;
    }

    //-------------------------------------------------------
    // 1字节转2个Hex字符
    public static String Byte2Hex(Byte inByte) {
    	return String.format("%02x", inByte).toUpperCase();
    }

    /**
     * byte[]数组转String字符串
     * @param b
     * @return
     */
    public static String ByteArrayToString(byte[] b) {
        int res = b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
        return String.valueOf(res);
    }
}
