package com.xxx.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.rabbitmq.tools.json.JSONUtil;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.SeckillMessage;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.rabbitmq.MQSender;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.service.ISeckillOrderService;
import com.xxx.seckill.vo.GoodsVo;
import com.xxx.seckill.vo.RespBean;
import com.xxx.seckill.vo.RespBeanEnum;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Resource
    private IGoodsService goodsService;
    @Resource
    private ISeckillOrderService seckillOrderService;
    @Resource
    private IOrderService orderService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private MQSender mqSender;
    @Resource
    private Gson gson;

    private Map<Long, Boolean> emptyStockMap = new HashMap<>();

//    @RequestMapping(value = "/doSeckill2")
//    public String doSeckill2(Model model, User user, Long goodsId) {
//        if (user == null) {
//            return "login";
//        }
//
//        model.addAttribute("user", user);
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//
//            return "secKillFail";
//        }
//
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id",
//                user.getId())
//                .eq("goods_id", goodsId));
//
//        if (seckillOrder != null) {
//            //已经重复抢购了
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
//            return "secKillFail";
//        }
//
//        //开始抢购，创建订单
//        Order order = orderService.seckill(user, goods);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goods);
//        return "orderDetail";
//
//
//    }

    /**
     * 秒杀功能
     *
     * @param model   视图
     * @param user    用户信息
     * @param goodsId 商品的Id
     * @return 返回值
     */
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(Model model, User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();

        //1.从redis中获取，判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue()
                .get("order:" + user.getId() + ":" + goodsId);

        if (seckillOrder != null) {
            //已经重复抢购了
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //通过内存标记减少redis的访问
        if (emptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //2.redis预减库存，必须是原子减少
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if (stock < 0) {
            emptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //3.下订单
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(gson.toJson(seckillMessage));
        return RespBean.success(0);

//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//
//        //从redis中获取，判断是否重复抢购
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue()
//                .get("order:" + user.getId() + ":" + goods.getId());
//
//
//        if (seckillOrder != null) {
//            //已经重复抢购了
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//
//        //开始抢购，创建订单
//        Order order = orderService.seckill(user, goods);
//        return RespBean.success(order);

    }

    /**
     * 初始化，将商品库存数量加载到redis
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(goodsVo)) {
            return;
        }
        goodsVo.forEach(goods -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goods.getId(), goods.getStockCount());
            emptyStockMap.put(goods.getId(), false);
        });
    }

    /**
     * 获取秒杀结果
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }
}
