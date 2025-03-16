package com.zhzd.henu.entity;
import java.util.List;
public class InformationContainerEntity {
    private List<InformationListEntity> selectedInformationList;
    private List<InformationListEntity> unselectedInformationList;

    // Getters and Setters
    public List<InformationListEntity> getSelectedInformationList() {
        return selectedInformationList;
    }

    public void setSelectedInformationList(List<InformationListEntity> selectedInformationList) {
        this.selectedInformationList = selectedInformationList;
    }

    public List<InformationListEntity> getUnselectedInformationList() {
        return unselectedInformationList;
    }

    public void setUnselectedInformationList(List<InformationListEntity> unselectedInformationList) {
        this.unselectedInformationList = unselectedInformationList;
    }
}
