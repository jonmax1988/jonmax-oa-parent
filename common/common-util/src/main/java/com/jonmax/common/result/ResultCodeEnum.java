package com.jonmax.common.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    FAIL_USER_PASSWORD(202, "用户或密码错误"),
    USER_STATUS_WRONG(203, "用户被禁用，请联系管理员"),
    LOGIN_MOBLE_ERROR(205,"认证失败"),

    SERVICE_ERROR(2012, "服务异常"),
    DATA_ERROR(204, "数据异常"),

    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限");

    private Integer code;

    private String message;
    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;


    }
}