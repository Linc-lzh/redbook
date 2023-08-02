package com.common.result;

public enum ResultCodeEnum {

    SUCCESS(200, "请求成功"),
    FAIL(500, "请求失败"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "请求资源不存在"),
    INVALID_REQUEST(400, "请求参数不合法"),
    TOO_MANY_REQUESTS(429, "请求过于频繁");

    private final int code;
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

