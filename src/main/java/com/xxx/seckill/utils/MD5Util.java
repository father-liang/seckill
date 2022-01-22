package com.xxx.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class MD5Util {
    private static final String salt = "1a2b3c4d";

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    /**
     * 第一次加密
     *
     * @param inputPass
     * @return
     */
    public static String inputPassToFromPass(String inputPass) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 第二次加密
     *
     * @param formPass 第二次加密的密码
     * @param salt     第二次加密用的盐
     * @return
     */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 调用的加密算法
     *
     * @param inputPass
     * @param salt      随机生成的盐
     * @return
     */
    public static String inputPassToDBPass(String inputPass, String salt) {
        String formPass = inputPassToFromPass(inputPass);
        String dbPass = formPassToDBPass(formPass, salt);

        return dbPass;
    }

    public static void main(String[] args) {
        //61bafc53901789aa6596e08f683b374e
        System.out.println(inputPassToFromPass("123456789qw"));
        System.out.println(formPassToDBPass("61bafc53901789aa6596e08f683b374e", "1a2b3c4d"));
        System.out.println(inputPassToDBPass("123456789qw", "1a2b3c4d"));
    }


}
