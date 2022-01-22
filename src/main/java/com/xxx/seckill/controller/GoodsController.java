package com.xxx.seckill.controller;

import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.impl.UserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *商品Controller
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private UserServiceImpl userService;

    @RequestMapping("/toList")
    public String toList(Model model, User user){
        //未登录就返回登陆
        if(user==null) return "login";

        model.addAttribute("user", user);
        return "goodsList";
    }
}
