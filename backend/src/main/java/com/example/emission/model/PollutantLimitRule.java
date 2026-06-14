package com.example.emission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("pollutant_limit_rules")
public class PollutantLimitRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("fuel_type")
    private String fuelType;

    @TableField("emission_standard")
    private String emissionStandard;

    @TableField("co_limit")
    private Double coLimit;

    @TableField("hc_limit")
    private Double hcLimit;

    @TableField("nox_limit")
    private Double noxLimit;

    @TableField("opacity_limit")
    private Double opacityLimit;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    public PollutantLimitRule() {}

    public PollutantLimitRule(Long id, String fuelType, String emissionStandard,
                               Double coLimit, Double hcLimit, Double noxLimit,
                               Double opacityLimit, String status, String remark) {
        this.id = id;
        this.fuelType = fuelType;
        this.emissionStandard = emissionStandard;
        this.coLimit = coLimit;
        this.hcLimit = hcLimit;
        this.noxLimit = noxLimit;
        this.opacityLimit = opacityLimit;
        this.status = status;
        this.remark = remark;
    }

    @JsonProperty("id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("fuelType")
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    @JsonProperty("emissionStandard")
    public String getEmissionStandard() { return emissionStandard; }
    public void setEmissionStandard(String emissionStandard) { this.emissionStandard = emissionStandard; }

    @JsonProperty("coLimit")
    public Double getCoLimit() { return coLimit; }
    public void setCoLimit(Double coLimit) { this.coLimit = coLimit; }

    @JsonProperty("hcLimit")
    public Double getHcLimit() { return hcLimit; }
    public void setHcLimit(Double hcLimit) { this.hcLimit = hcLimit; }

    @JsonProperty("noxLimit")
    public Double getNoxLimit() { return noxLimit; }
    public void setNoxLimit(Double noxLimit) { this.noxLimit = noxLimit; }

    @JsonProperty("opacityLimit")
    public Double getOpacityLimit() { return opacityLimit; }
    public void setOpacityLimit(Double opacityLimit) { this.opacityLimit = opacityLimit; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @JsonProperty("remark")
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @JsonProperty("createTime")
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @JsonProperty("updateTime")
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
