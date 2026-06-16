package com.example.emission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("system_logs")
public class SystemLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String operator;

    private String role;

    private String action;

    private String businessObject;

    private String detail;

    private String result;

    private String operateTime;

    private String ip;

    public SystemLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getBusinessObject() { return businessObject; }
    public void setBusinessObject(String businessObject) { this.businessObject = businessObject; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getOperateTime() { return operateTime; }
    public void setOperateTime(String operateTime) { this.operateTime = operateTime; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
