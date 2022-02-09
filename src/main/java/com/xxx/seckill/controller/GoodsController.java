package com.xxx.seckill.controller;

import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.impl.UserServiceImpl;
import com.xxx.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 *商品Controller
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private IGoodsService goodsService;

    /**
     * window 5000次压力测试，QPS：541
     * @param model
     * @param user
     * @return
     */
    @RequestMapping("/toList")
    public String toList(Model model, User user){
        //未登录就返回登陆
        if(user==null) return "login";

        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    /**
     * 跳转商品详情页面
     * @param model
     * @param goodsId
     * @return
     */
    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model,User user, @PathVariable long goodsId){
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date now = new Date();
        //秒杀的状态
        int  secKillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;

        if(now.before(startDate)){
            //当前时间在秒杀时间之前
            remainSeconds = ((int) ((startDate.getTime() - now.getTime()) / 1000));

        }else if(now.after(endDate)){
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            //秒杀正在进行
            secKillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("goods", goodsVo);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goodsDetail";
    }
}
