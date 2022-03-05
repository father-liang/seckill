package com.xxx.seckill.rabbitmq;

import com.google.gson.Gson;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.SeckillMessage;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.vo.GoodsVo;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Resource
    private Gson gson;
    @Resource
    private IGoodsService goodsService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收到的信息：" + message);

        SeckillMessage seckillMessage = gson.fromJson(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue()
                .get("order:" + user.getId() + ":" + goodsId);

        if (seckillOrder != null) {
            //已经重复抢购了
            return;
        }

        //创建订单
        Order order = orderService.seckill(user, goodsVo);

    }
}
