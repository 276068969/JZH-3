package com.example.emission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EnvironmentalJudgmentResult {

    private String environmentalStatus;
    private String statusLevel;
    private List<ExceededItem> exceededItems;
    private String suggestion;
    private String appliedStandard;

    public EnvironmentalJudgmentResult() {}

    public EnvironmentalJudgmentResult(String environmentalStatus, String statusLevel,
                                       List<ExceededItem> exceededItems, String suggestion,
                                       String appliedStandard) {
        this.environmentalStatus = environmentalStatus;
        this.statusLevel = statusLevel;
        this.exceededItems = exceededItems;
        this.suggestion = suggestion;
        this.appliedStandard = appliedStandard;
    }

    @JsonProperty("environmentalStatus")
    public String getEnvironmentalStatus() { return environmentalStatus; }
    public void setEnvironmentalStatus(String environmentalStatus) { this.environmentalStatus = environmentalStatus; }

    @JsonProperty("statusLevel")
    public String getStatusLevel() { return statusLevel; }
    public void setStatusLevel(String statusLevel) { this.statusLevel = statusLevel; }

    @JsonProperty("exceededItems")
    public List<ExceededItem> getExceededItems() { return exceededItems; }
    public void setExceededItems(List<ExceededItem> exceededItems) { this.exceededItems = exceededItems; }

    @JsonProperty("suggestion")
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    @JsonProperty("appliedStandard")
    public String getAppliedStandard() { return appliedStandard; }
    public void setAppliedStandard(String appliedStandard) { this.appliedStandard = appliedStandard; }

    public static class ExceededItem {
        private String pollutant;
        private double value;
        private double limit;
        private double exceedRatio;

        public ExceededItem() {}

        public ExceededItem(String pollutant, double value, double limit, double exceedRatio) {
            this.pollutant = pollutant;
            this.value = value;
            this.limit = limit;
            this.exceedRatio = exceedRatio;
        }

        @JsonProperty("pollutant")
        public String getPollutant() { return pollutant; }
        public void setPollutant(String pollutant) { this.pollutant = pollutant; }

        @JsonProperty("value")
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        @JsonProperty("limit")
        public double getLimit() { return limit; }
        public void setLimit(double limit) { this.limit = limit; }

        @JsonProperty("exceedRatio")
        public double getExceedRatio() { return exceedRatio; }
        public void setExceedRatio(double exceedRatio) { this.exceedRatio = exceedRatio; }
    }
}
