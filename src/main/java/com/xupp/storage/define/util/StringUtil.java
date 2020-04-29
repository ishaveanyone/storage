package com.xupp.storage.define.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StringUtil {
    /**
     *
     * @param str
     * @param encoding 自定转换的 字符集
     * @return
     * @throws UnsupportedEncodingException
     * @decreption 将给定的字符串 中的中文转换成指定的字符集
     */
    public static String parseChinese(String str,String encoding) throws UnsupportedEncodingException {
        String[] strArray=str.split("");
//		String[] strArray=str.split("");
        StringBuffer sb=new StringBuffer();
        for(String tmp:strArray) {
            if(tmp==null||"".equals(tmp))
                continue;
            //如果是中文就要进行转换
            if(tmp.matches("[\u4e00-\u9fcc]+")) {
                //指定装换格式
                sb.append(URLEncoder.encode(tmp,encoding));
            }else {
                sb.append(tmp);
            }
//			System.out.println(tmp);
        }
        return sb.toString();
    }

    /**
     *
     * @param str   原字符串
     * @param cha1  字符1 唯一
     * @param cha2  字符2 最后一个字符（可以重复出现在原来字符串中）
     * @return 返回字符一字符2之间的字符串
     */
    public static String catStrBetweenChar(String str,char cha1,char cha2) {
        return str.substring(str.indexOf(cha1)+1,str.lastIndexOf(String.valueOf(cha2)));
    }




    /**
     * @param str
     * @param cha1 截取字符(可以是唯一的或者是最后一个字符)
     * @return 返回指定的字符串之后的子串
     */
    public static String catLastStrByChar(String str,char cha1) {
        return str.substring(str.lastIndexOf(String.valueOf(cha1))+1);
    }


    /**
     *
     * @param str
     * @param cha1   截取字符(可以是唯一的或者是最后一个字符)
     * @return 返回指定的字符串之前的子串
     */
    public static String catBeforeStrByChar(String str,char cha1) {
        return str.substring(0,str.lastIndexOf(String.valueOf(cha1)));
    }

    /**
     * @param str
     * @param cha1
     * @param index
     * @return 获取倒数第index 个后面的字符子串
     */
    public static String catLastStrByCharAndIndex(String str,char cha1,int index) {
        StringBuffer sb=new StringBuffer();
        while(index>0) {
            String targetStr=catLastStrByChar(str,cha1);
            str=catBeforeStrByChar(str,cha1);
            sb.insert(0, targetStr+"/");
            index--;
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


}
