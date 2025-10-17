package com.datamining.ssedemo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVO<T>{


    private int code;
    private String msg;
    private T data;

    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "success", null);
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data);
    }

    public static <T> ResultVO<T> success(String msg, T data) {
        return new ResultVO<>(200, msg, data);
    }

    public static <T> ResultVO<T> fail() {
        return new ResultVO<>(500, "fail", null);
    }

    public static <T> ResultVO<T> fail(String msg) {
        return new ResultVO<>(500, msg, null);
    }

    public static <T> ResultVO<T> fail(int code, String msg) {
        return new ResultVO<>(code, msg, null);
    }
}
