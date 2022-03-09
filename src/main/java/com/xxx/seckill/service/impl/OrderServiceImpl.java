package com.xxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.seckill.exception.GlobalException;
import com.xxx.seckill.mapper.OrderMapper;
import com.xxx.seckill.mapper.SeckillOrderMapper;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.SeckillGoods;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.service.ISeckillGoodsService;
import com.xxx.seckill.utils.MD5Util;
import com.xxx.seckill.utils.UUIDUtil;
import com.xxx.seckill.vo.GoodsVo;
import com.xxx.seckill.vo.OrderDetailVo;
import com.xxx.seckill.vo.RespBeanEnum;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Liang
 * @since 2022-01-22
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private ISeckillGoodsService seckillGoodsService;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private IGoodsService goodsService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 秒杀功能的实现，创建订单
     *
     * @param user  用户消息
     * @param goods 商品信息
     * @return 订单消息
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //减少秒杀商品表的库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id", goods.getId()));
//        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        boolean updateResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count-1")
                .eq("goods_id", goods.getId())
                .gt("stock_count", 0));

        if (seckillGoods.getStockCount() < 1) {
            //判断是否还有库存
            valueOperations.set("isStockEmpty" + goods.getId(), "0");
            return null;
        }

        //生成订单,新建订单但未支付
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderMapper.insert(seckillOrder);

        valueOperations.set("order:" + user.getId() + ":" + goods.getId(), seckillOrder);

        return order;
    }

    /**
     * 订单详情
     *
     * @param orderId 订单Id
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        if (orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }

        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());

        return new OrderDetailVo(order, goodsVo);
    }

    /**
     * 获取秒杀地址
     */
    @Override
    public String createPath(User user, Long goodsId) {

        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }

        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);


        return path.equals(redisPath);

    }

    /**
     * 校验验证码
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (StringUtils.isEmpty(captcha) || user == null || goodsId < 0) {
            return false;
        }

        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
