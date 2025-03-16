package com.zhzd.henu.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraverseFolder {
    // 遍历文件夹并将文件添加到 fileList 中
    public static void traverseFolder(File folder, List<File> fileList) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles(); // 获取文件夹下的文件和子文件夹
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file); // 如果是文件，添加到列表
                    } else if (file.isDirectory()) {
//                        traverseFolder(file, fileList); // 如果是子文件夹，递归遍历
                    }
                }
            }
        } else {
            System.out.println("目录不存在或不是一个文件夹: " + folder.getAbsolutePath());
        }
    }
    public static Map<String,File> traverseFolder(File folder,File folderFirst) {
        Map<String,File> map=new HashMap<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles(); // 获取文件夹下的文件和子文件夹
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        map.put(file.getName(),file);
                    } else if (file.isDirectory()) {
//                        traverseFolder(file, fileList); // 如果是子文件夹，递归遍历
                    }
                }
            }
        } else {
            System.out.println("目录不存在或不是一个文件夹: " + folder.getAbsolutePath());
        }
        if (folderFirst.exists() && folderFirst.isDirectory()) {
            File[] files = folderFirst.listFiles(); // 获取文件夹下的文件和子文件夹
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        map.put(file.getName(),file);
                    } else if (file.isDirectory()) {
//                        traverseFolder(file, fileList); // 如果是子文件夹，递归遍历
                    }
                }
            }
        } else {
            System.out.println("目录不存在或不是一个文件夹: " + folder.getAbsolutePath());
        }
        return map;
    }
}
