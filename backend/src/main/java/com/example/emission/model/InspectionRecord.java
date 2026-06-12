package com.example.emission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty("inspectionNo")
    public String getInspectionNo() { return inspectionNo; }
    public void setInspectionNo(String inspectionNo) { this.inspectionNo = inspectionNo; }

    @JsonProperty("plateNumber")
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    @JsonProperty("stationName")
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    @JsonProperty("inspectionTime")
    public String getInspectionTime() { return inspectionTime; }
    public void setInspectionTime(String inspectionTime) { this.inspectionTime = inspectionTime; }

    @JsonProperty("coValue")
    public double getCoValue() { return coValue; }
    public void setCoValue(double coValue) { this.coValue = coValue; }

    @JsonProperty("hcValue")
    public double getHcValue() { return hcValue; }
    public void setHcValue(double hcValue) { this.hcValue = hcValue; }

    @JsonProperty("noxValue")
    public double getNoxValue() { return noxValue; }
    public void setNoxValue(double noxValue) { this.noxValue = noxValue; }

    @JsonProperty("opacityValue")
    public double getOpacityValue() { return opacityValue; }
    public void setOpacityValue(double opacityValue) { this.opacityValue = opacityValue; }

    @JsonProperty("result")
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    @JsonProperty("inspector")
    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }

    @JsonProperty("reportStatus")
    public String getReportStatus() { return reportStatus; }
    public void setReportStatus(String reportStatus) { this.reportStatus = reportStatus; }

    @JsonProperty("auditor")
    public String getAuditor() { return auditor; }
    public void setAuditor(String auditor) { this.auditor = auditor; }

    @JsonProperty("auditTime")
    public String getAuditTime() { return auditTime; }
    public void setAuditTime(String auditTime) { this.auditTime = auditTime; }

    @JsonProperty("auditOpinion")
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
}
