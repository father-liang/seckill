package com.xxx.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.service.ISeckillOrderService;
import com.xxx.seckill.vo.GoodsVo;
import com.xxx.seckill.vo.RespBeanEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/seckill")
public class SecKillController {

    @Resource
    private IGoodsService goodsService;
    @Resource
    private ISeckillOrderService seckillOrderService;
    @Resource
    private IOrderService orderService;

    /**
     * 秒杀功能
     *
     * @param model   视图
     * @param user    用户信息
     * @param goodsId 商品的Id
     * @return 返回值
     */
    @RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId) {
        if (user == null) {
            return "login";
        }

        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());

            return "secKillFail";
        }

        //判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
                .eq("goods_id", goodsId));

        if (seckillOrder != null) {
            //已经重复抢购了
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }

        //开始抢购，创建订单
        Order order = orderService.seckill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";


    }
}
