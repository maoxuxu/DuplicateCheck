package com.zhzd.henu.entity;

import java.util.Set;

public class DownloadEntity {
    private String registerNo;
    private String filepath;

    private Set<String> fileNames;

    public DownloadEntity() {
        this.registerNo = registerNo;
        this.filepath = filepath;
    }

    // Getters and Setters
    public String getRegisterNo() {
        return registerNo;
    }

    public void setRegisterNo(String registerNo) {
        this.registerNo = registerNo;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Set<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(Set<String> fileNames) {
        this.fileNames = fileNames;
    }
}
