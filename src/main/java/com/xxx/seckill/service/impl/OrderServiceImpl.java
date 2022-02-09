package com.xxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.seckill.mapper.GoodsMapper;
import com.xxx.seckill.mapper.OrderMapper;
import com.xxx.seckill.mapper.SeckillOrderMapper;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.SeckillGoods;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.service.ISeckillGoodsService;
import com.xxx.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 *  服务实现类
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
    /**
     * 秒杀功能的实现，创建订单
     * @param user 用户消息
     * @param goods 商品信息
     * @return 订单消息
     */
    @Override
    public Order seckill(User user, GoodsVo goods) {
        //减少秒杀商品表的库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id", goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        seckillGoodsService.updateById(seckillGoods);

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

        return order;
    }
}
