package com.xxx.seckill.utils;

import com.xxx.seckill.pojo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成用户工具类
 */
public class UserUtil {
    private static void createUser(int count) {
        List<User> users = new ArrayList<User>();

        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(1300000000L+i);
            user.setNickname("user"+i);
            user.setSalt("1a2b3c");
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));

        }
    }
}
