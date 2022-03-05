package com.xxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Liang
 * @since 2022-01-22
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
