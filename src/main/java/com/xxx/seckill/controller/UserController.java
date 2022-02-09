package com.xxx.seckill.controller;


import com.xxx.seckill.pojo.User;
import com.xxx.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Liang
 * @since 2022-01-10
 */
@Controller
@RequestMapping("/user")
public class UserController {

    /**
     * 用户信息（测试）
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }

}
