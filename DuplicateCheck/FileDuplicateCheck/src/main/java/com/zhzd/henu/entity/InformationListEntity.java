package com.zhzd.henu.entity;

public class InformationListEntity {
    private String subRegistrationNo;
    private String registrationTypeName;
    private String businessTypeName;
    private String registrationInureDate;
    private String pawneeNameListStr;
    private String id;
    private String childId;
    private String registerDetailedInformationListFile;
    private String downQueryProveFile;

    // Getters and Setters
    public String getSubRegistrationNo() {
        return subRegistrationNo;
    }

    public void setSubRegistrationNo(String subRegistrationNo) {
        this.subRegistrationNo = subRegistrationNo;
    }

    public String getRegistrationTypeName() {
        return registrationTypeName;
    }

    public void setRegistrationTypeName(String registrationTypeName) {
        this.registrationTypeName = registrationTypeName;
    }

    public String getBusinessTypeName() {
        return businessTypeName;
    }

    public void setBusinessTypeName(String businessTypeName) {
        this.businessTypeName = businessTypeName;
    }

    public String getRegistrationInureDate() {
        return registrationInureDate;
    }

    public void setRegistrationInureDate(String registrationInureDate) {
        this.registrationInureDate = registrationInureDate;
    }

    public String getPawneeNameListStr() {
        return pawneeNameListStr;
    }

    public void setPawneeNameListStr(String pawneeNameListStr) {
        this.pawneeNameListStr = pawneeNameListStr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getRegisterDetailedInformationListFile() {
        return registerDetailedInformationListFile;
    }

    public void setRegisterDetailedInformationListFile(String registerDetailedInformationListFile) {
        this.registerDetailedInformationListFile = registerDetailedInformationListFile;
    }

    public String getDownQueryProveFile() {
        return downQueryProveFile;
    }

    public void setDownQueryProveFile(String downQueryProveFile) {
        this.downQueryProveFile = downQueryProveFile;
    }
}
