package com.example.emission.dto;

public class AuditRequest {
    private String inspectionNo;
    private String action;
    private String opinion;

    public String getInspectionNo() { return inspectionNo; }
    public void setInspectionNo(String inspectionNo) { this.inspectionNo = inspectionNo; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
}
