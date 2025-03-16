package com.zhzd.henu.tools;

public class CheckKeyword {
    public static boolean containsKeywords(String str) {
        // 获取前后10个字符
        String before = getFirst10Chars(str);
        String after = getLast10Chars(str);

        // 去除标点符号
        before = removePunctuation(before);
        after = removePunctuation(after);

        // 检查是否包含关键字
        String[] keywords = {"应收账款", "转让", "受让", "质押"};
        for (String keyword : keywords) {
            if (before.contains(keyword) || after.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // 获取字符串前10个字符
    public static String getFirst10Chars(String str) {
        return str.length() > 10 ? str.substring(0, 10) : str;
    }

    // 获取字符串后10个字符
    public static String getLast10Chars(String str) {
        return str.length() > 10 ? str.substring(str.length() - 10) : str;
    }

    // 去除标点符号
    public static String removePunctuation(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (!isPunctuation(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // 判断字符是否为标点符号
    public static boolean isPunctuation(char c) {
        // Unicode 中标点符号的范围
        String punctuations = "。！？，、；：“”‘’（）【】《》";
        return punctuations.indexOf(c) >= 0;
    }
}
