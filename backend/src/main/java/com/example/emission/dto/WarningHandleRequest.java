package com.example.emission.dto;

public class WarningHandleRequest {
    private Long warningId;
    private String handleOpinion;
    private boolean reinspectRequired;
    private String reinspectDeadline;
    private String status;

    public Long getWarningId() { return warningId; }
    public void setWarningId(Long warningId) { this.warningId = warningId; }

    public String getHandleOpinion() { return handleOpinion; }
    public void setHandleOpinion(String handleOpinion) { this.handleOpinion = handleOpinion; }

    public boolean isReinspectRequired() { return reinspectRequired; }
    public void setReinspectRequired(boolean reinspectRequired) { this.reinspectRequired = reinspectRequired; }

    public String getReinspectDeadline() { return reinspectDeadline; }
    public void setReinspectDeadline(String reinspectDeadline) { this.reinspectDeadline = reinspectDeadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
