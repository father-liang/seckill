package com.xxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.seckill.mapper.OrderMapper;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.service.IOrderService;
import org.springframework.stereotype.Service;

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

}
