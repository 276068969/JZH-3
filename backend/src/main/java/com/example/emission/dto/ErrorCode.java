package com.example.emission.dto;

public enum ErrorCode {
    SUCCESS("SUCCESS", "操作成功"),
    EMPTY_KEYWORD("EMPTY_KEYWORD", "请输入车牌号、VIN 或检测报告编号"),
    KEYWORD_TOO_SHORT("KEYWORD_TOO_SHORT", "输入内容过短，请至少输入 2 个字符"),
    KEYWORD_TOO_LONG("KEYWORD_TOO_LONG", "输入内容过长，请控制在 30 个字符以内"),
    VEHICLE_NOT_FOUND("VEHICLE_NOT_FOUND", "未找到匹配的车辆，请检查输入是否正确"),
    PLATE_NOT_FOUND("PLATE_NOT_FOUND", "未找到该车牌号对应的车辆"),
    VIN_NOT_FOUND("VIN_NOT_FOUND", "未找到该 VIN 对应的车辆"),
    INSPECTION_NOT_FOUND("INSPECTION_NOT_FOUND", "未找到该检测报告编号对应的记录"),
    VEHICLE_NOT_FOUND_BY_INSPECTION("VEHICLE_NOT_FOUND_BY_INSPECTION", "检测报告编号存在，但未找到关联的车辆信息"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误，请稍后重试");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
