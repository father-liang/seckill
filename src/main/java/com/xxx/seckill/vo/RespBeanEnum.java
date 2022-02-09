package com.xxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum  RespBeanEnum {
    //通用枚举
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),

    //登录模块枚举
    LOGIN_ERROR(500210, "用户名或密码不正确"),
    MOBILE_ERROR(500211, "手机号码格式不正确"),
    BIND_ERROR(500212, "参数校验异常"),

    //秒杀模块5005xx
    EMPTY_STOCK(500500, "库存不足"),
    REPEATE_ERROR(500501, "不可重复抢购")

    ;

    private final Integer code;
    private final String message;


}
