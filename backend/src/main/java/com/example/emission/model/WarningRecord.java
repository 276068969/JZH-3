package com.example.emission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WarningRecord {
    private Long id;
    private String plateNumber;
    private String pollutant;
    private String level;
    private String description;
    private String status;
    private String handler;
    private String handleTime;
    private String handleOpinion;
    private boolean reinspectRequired;
    private String reinspectDeadline;
    private String createdAt;

    public WarningRecord() {}

    public WarningRecord(Long id, String plateNumber, String pollutant, String level,
                         String description, String status, String createdAt) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.pollutant = pollutant;
        this.level = level;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    @JsonProperty("id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("plateNumber")
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    @JsonProperty("pollutant")
    public String getPollutant() { return pollutant; }
    public void setPollutant(String pollutant) { this.pollutant = pollutant; }

    @JsonProperty("level")
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    @JsonProperty("description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @JsonProperty("handler")
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }

    @JsonProperty("handleTime")
    public String getHandleTime() { return handleTime; }
    public void setHandleTime(String handleTime) { this.handleTime = handleTime; }

    @JsonProperty("handleOpinion")
    public String getHandleOpinion() { return handleOpinion; }
    public void setHandleOpinion(String handleOpinion) { this.handleOpinion = handleOpinion; }

    @JsonProperty("reinspectRequired")
    public boolean isReinspectRequired() { return reinspectRequired; }
    public void setReinspectRequired(boolean reinspectRequired) { this.reinspectRequired = reinspectRequired; }

    @JsonProperty("reinspectDeadline")
    public String getReinspectDeadline() { return reinspectDeadline; }
    public void setReinspectDeadline(String reinspectDeadline) { this.reinspectDeadline = reinspectDeadline; }

    @JsonProperty("createdAt")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
