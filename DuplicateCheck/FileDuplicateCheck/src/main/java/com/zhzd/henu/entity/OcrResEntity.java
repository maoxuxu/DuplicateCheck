package com.zhzd.henu.entity;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class OcrResEntity {
    private String ocrResult;
    private String confidence;
    private String assetDescription;
    private boolean bingoAll;
    private boolean bingo;
    private File registrationCertificateFile;
    private boolean bingoNum;
    private HashMap<String,String> pageHashMap;
    private int[] bingoKeyNumArray;
    private String personFillingName;
    private String registrationExpirationDate;
    private String similarScore;
    private String outPath;
    private boolean containsKeywords; //判断风险词是否含有特殊词汇
    private Map<String,String> riskWordsMap;
    public OcrResEntity(String confidence, String assetDescription, boolean bingoAll, boolean bingo, File registrationCertificateFile, HashMap<String, String> pageHashMap, int[] bingoKeyNumArray, String personFillingName, String registrationExpirationDate, String similarScore, String outPath, boolean containsKeywords, Map<String, String> riskWordsMap) {
        this.confidence = confidence;
        this.bingoAll = bingoAll;
        this.bingo = bingo;
        this.assetDescription = assetDescription;
        this.registrationCertificateFile = registrationCertificateFile;
        this.pageHashMap = pageHashMap;
        this.bingoKeyNumArray = bingoKeyNumArray;
        this.personFillingName = personFillingName;
        this.registrationExpirationDate = registrationExpirationDate;
        this.similarScore = similarScore;
        this.outPath = outPath;
        this.containsKeywords = containsKeywords;
        this.riskWordsMap = riskWordsMap;
    }

    public String getStr() {
        return ocrResult;
    }
    public String getAssetDescription() {
        return assetDescription;
    }
    public String getConfidence() {
        return confidence;
    }
    public String isConfidence() {
        return confidence;
    }

    public boolean isBingoAll() {
        return bingoAll;
    }
    public boolean isBingo() {
        return bingo;
    }

    public HashMap<String, String> getPageHashMap() {
        return pageHashMap;
    }

    public void setPageHashMap(HashMap<String, String> pageHashMap) {
        this.pageHashMap = pageHashMap;
    }

    public int[] getBingoKeyNumArray() {
        return bingoKeyNumArray;
    }

    public void setBingoKeyNumArray(int[] bingoKeyNumArray) {
        this.bingoKeyNumArray = bingoKeyNumArray;
    }

    public File getRegistrationCertificateFile() {
        return registrationCertificateFile;
    }

    public void setRegistrationCertificateFile(File registrationCertificateFile) {
        this.registrationCertificateFile = registrationCertificateFile;
    }

    public String getPersonFillingName() {
        return personFillingName;
    }

    public void setPersonFillingName(String personFillingName) {
        this.personFillingName = personFillingName;
    }

    public String getRegistrationExpirationDate() {
        return registrationExpirationDate;
    }

    public void setRegistrationExpirationDate(String registrationExpirationDate) {
        this.registrationExpirationDate = registrationExpirationDate;
    }

    public String getSimilarScore() {
        return similarScore;
    }

    public void setSimilarScore(String similarScore) {
        this.similarScore = similarScore;
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

    public Map<String, String> getRiskWordsMap() {
        return riskWordsMap;
    }

    public void setRiskWordsMap(Map<String, String> riskWordsMap) {
        this.riskWordsMap = riskWordsMap;
    }
}
