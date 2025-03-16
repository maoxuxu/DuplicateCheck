package com.zhzd.henu.tools;

public class ConvertStringToIntArray {
    // 将 String 转换为 int 数组
    public static int[] convertStringToIntArray(String str) {
        // 去掉字符串的左右方括号和空格
        str = str.replaceAll("\\[|\\]|\\s", "");

        // 根据逗号分隔字符串
        String[] strArray = str.split(",");

        // 创建一个与字符串数组相同大小的 int 数组
        int[] intArray = new int[strArray.length];

        // 将字符串数组转换为整数数组
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }

        return intArray;
    }
}
