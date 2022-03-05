package com.xxx.seckill.controller;

import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.impl.UserServiceImpl;
import com.xxx.seckill.vo.DetailVo;
import com.xxx.seckill.vo.GoodsVo;
import com.xxx.seckill.vo.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 商品Controller
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private IGoodsService goodsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * window 5000次压力测试，QPS：541
     *
     * @param model
     * @param user
     * @return
     */
    //做页面缓存，将页面缓存到redis中
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        //未登录就返回登陆
        if (user == null) return "login";

        //从redis中获取HTML页面，用String字符串表示,如果不为空则直接返回
        String html = (String) redisTemplate.opsForValue().get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
//        return "goodsList";

        //如果为空则进行手动渲染，并存入到redis中并返回
        WebContext context = new WebContext(request, response,
                request.getServletContext(),
                request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)) {
            redisTemplate.opsForValue().set("goodsList", html, 30, TimeUnit.SECONDS);
        }

        return html;
    }

    /**
     * 跳转商品详情页面
     *
     * @param model
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user,
                             @PathVariable long goodsId) {

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date now = new Date();
        //秒杀的状态
        int secKillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;

        if (now.before(startDate)) {
            //当前时间在秒杀时间之前
            remainSeconds = ((int) ((startDate.getTime() - now.getTime()) / 1000));

        } else if (now.after(endDate)) {
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀正在进行
            secKillStatus = 1;
            remainSeconds = 0;
        }

        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSeconds(remainSeconds);

        return RespBean.success(detailVo);
    }
}
