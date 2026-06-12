package com.example.emission.model;

public class InspectionRecord {
    private String inspectionNo;
    private String plateNumber;
    private String stationName;
    private String inspectionTime;
    private double coValue;
    private double hcValue;
    private double noxValue;
    private double opacityValue;
    private String result;
    private String inspector;
    private String reportStatus;
    private String auditor;
    private String auditTime;
    private String auditOpinion;

    public InspectionRecord() {}

    public InspectionRecord(String inspectionNo, String plateNumber, String stationName,
                            String inspectionTime, double coValue, double hcValue, double noxValue,
                            double opacityValue, String result, String inspector, String reportStatus) {
        this.inspectionNo = inspectionNo;
        this.plateNumber = plateNumber;
        this.stationName = stationName;
        this.inspectionTime = inspectionTime;
        this.coValue = coValue;
        this.hcValue = hcValue;
        this.noxValue = noxValue;
        this.opacityValue = opacityValue;
        this.result = result;
        this.inspector = inspector;
        this.reportStatus = reportStatus;
    }

    public String inspectionNo() { return inspectionNo; }
    public String plateNumber() { return plateNumber; }
    public String stationName() { return stationName; }
    public String inspectionTime() { return inspectionTime; }
    public double coValue() { return coValue; }
    public double hcValue() { return hcValue; }
    public double noxValue() { return noxValue; }
    public double opacityValue() { return opacityValue; }
    public String result() { return result; }
    public String inspector() { return inspector; }
    public String reportStatus() { return reportStatus; }
    public String auditor() { return auditor; }
    public String auditTime() { return auditTime; }
    public String auditOpinion() { return auditOpinion; }

    public void setReportStatus(String reportStatus) { this.reportStatus = reportStatus; }
    public void setAuditor(String auditor) { this.auditor = auditor; }
    public void setAuditTime(String auditTime) { this.auditTime = auditTime; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
}
