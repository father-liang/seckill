package com.xxx.seckill.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码校验
 */
public class ValidatorUtil {

    /**
     * 正则表达式的模板
     */
    private static final Pattern mobile_pattern = Pattern.compile("[1]([3-9])[0-9]{9}$");

    public static boolean isMobile(String mobile){
        //判断号码是否为空
        if(StringUtils.isEmpty(mobile)){
            return false;
        }

        //正则匹配进行校验
        Matcher matcher = mobile_pattern.matcher(mobile);
        return matcher.matches();
    }


}
