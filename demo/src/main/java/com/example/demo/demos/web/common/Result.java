// src/main/java/com/example/demo/demos/web/common/Result.java
package com.example.demo.demos.web.common;

import lombok.Data;

/**
 * 统一API响应结果封装
 */
@Data
public class Result<T> {
    // 状态码：0-成功，1-失败
    private Integer code;
    // 提示信息
    private String message;
    // 响应数据
    private T data;

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        return result;
    }

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功响应（自定义消息+数据）
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 失败响应（自定义消息）
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }

    // 失败响应（自定义状态码+消息）
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
