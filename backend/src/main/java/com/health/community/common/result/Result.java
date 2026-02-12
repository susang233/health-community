package com.health.community.common.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;    // 200=成功, 401=未授权, 403=无权限, 500=服务器错误...
    private String message;  // 提示信息
    private T data;          // 数据

    // 成功
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    // 通用错误
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    // 未授权（特别重要！）
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }
}