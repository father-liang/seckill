package com.xxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.vo.GoodsVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Liang
 * @since 2022-01-22
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goods);
}
