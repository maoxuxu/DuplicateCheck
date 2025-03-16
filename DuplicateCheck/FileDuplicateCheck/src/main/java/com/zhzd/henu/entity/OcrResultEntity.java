package com.zhzd.henu.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OcrResultEntity {

    // 顶层包含多个关键字对象的Map
    private Map<String, Keyword> keywords;

    public Map<String, Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(Map<String, Keyword> keywords) {
        this.keywords = keywords;
    }

    // Keyword类
    public static class Keyword {
        private int bingoNum;  // 整数类型的bingoNum
        private List<FileInfo> bingoFiles = new ArrayList<>();
        public int getBingoNum() {
            return bingoNum;
        }

        public void setBingoNum(int bingoNum) {
            this.bingoNum = bingoNum;
        }

        public List<FileInfo> getBingoFiles() {
            return bingoFiles;
        }

        public void setBingoFiles(List<FileInfo> bingoFiles) {
            this.bingoFiles = bingoFiles;
        }
    }

//    // BingoFile类
//    public static class BingoFile {
//        private Map<String, FileInfo> files;  // 存储多个文件信息，文件名作为key
//
//        public Map<String, FileInfo> getFiles() {
//            return files;
//        }
//
//        public void setFiles(Map<String, FileInfo> files) {
//            this.files = files;
//        }
//    }

    // FileInfo类
    public static class FileInfo {
        private String fileName;
        private String confidence;  // 字符串类型的confidence
        private String path;        // 字符串类型的path
        private String page;        // 字符串类型的page
        private String assetDescription;  // 字符串类型的assetDescription
        private File registrationCertificateFile;  // 字符串类型的assetDescription
        private String personFillingName;
        private String registrationExpirationDate;
        private String similarScore;
        private String outPath;
        private boolean containsKeywords; //判断风险词是否含有特殊词汇
        public String getConfidence() {
            return confidence;
        }

        public void setConfidence(String confidence) {
            this.confidence = confidence;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public String getAssetDescription() {
            return assetDescription;
        }

        public void setAssetDescription(String assetDescription) {
            this.assetDescription = assetDescription;
        }

        public File getRegistrationCertificateFile() {
            return registrationCertificateFile;
        }

        public void setRegistrationCertificateFile(File registrationCertificateFile) {
            this.registrationCertificateFile = registrationCertificateFile;
        }

        public String getRegistrationExpirationDate() {
            return registrationExpirationDate;
        }

        public void setRegistrationExpirationDate(String registrationExpirationDate) {
            this.registrationExpirationDate = registrationExpirationDate;
        }

        public String getPersonFillingName() {
            return personFillingName;
        }

        public void setPersonFillingName(String personFillingName) {
            this.personFillingName = personFillingName;
        }

        public String getSimilarScore() {
            return similarScore;
        }

        public void setSimilarScore(String similarScore) {
            this.similarScore = similarScore;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getOutPath() {
            return outPath;
        }

        public void setOutPath(String outPath) {
            this.outPath = outPath;
        }

        public boolean isContainsKeywords() {
            return containsKeywords;
        }

        public void setContainsKeywords(boolean containsKeywords) {
            this.containsKeywords = containsKeywords;
        }
    }

    // 测试方法
    public static void main(String[] args) {
//        // 创建实例并使用
//        OcrResultEntity collection = new OcrResultEntity();
//        collection.setKeywords(new HashMap<>());
//
//        // 创建FileInfo对象
//        FileInfo fileInfo1 = new FileInfo();
//        fileInfo1.setConfidence("0.95");
//        fileInfo1.setPath("/path/to/file1");
//        fileInfo1.setPage("12");
//        fileInfo1.setAssetDescription("This is file 1.");
//
//        FileInfo fileInfo2 = new FileInfo();
//        fileInfo2.setConfidence("0.90");
//        fileInfo2.setPath("/path/to/file2");
//        fileInfo2.setPage("15");
//        fileInfo2.setAssetDescription("This is file 2.");
//
//        // 创建BingoFile对象
//        BingoFile bingoFile = new BingoFile();
//        bingoFile.setFiles(new HashMap<>());
//        bingoFile.getFiles().put("fileName1", fileInfo1);
//        bingoFile.getFiles().put("fileName2", fileInfo2);
//
//        // 创建Keyword对象
//        Keyword keyword = new Keyword();
//        keyword.setBingoNum(2);
//        keyword.setBingoFile(bingoFile);
//
//        // 将keyword对象放入collection中
//        collection.getKeywords().put("keyword1", keyword);
//
//        // 打印输出结果
//        for (String key : collection.getKeywords().keySet()) {
//            Keyword kw = collection.getKeywords().get(key);
//            System.out.println("Keyword: " + key);
//            System.out.println("BingoNum: " + kw.getBingoNum());
//
//            BingoFile bf = kw.getBingoFile();
//            for (String fileName : bf.getFiles().keySet()) {
//                FileInfo fi = bf.getFiles().get(fileName);
//                System.out.println("FileName: " + fileName);
//                System.out.println("Confidence: " + fi.getConfidence());
//                System.out.println("Path: " + fi.getPath());
//                System.out.println("Page: " + fi.getPage());
//                System.out.println("Asset Description: " + fi.getAssetDescription());
//            }
//        }
    }
}
