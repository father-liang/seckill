
# 工程简介
秒杀系统

## 项目技术栈
springboot
thymeleaf
mybatis-plus
redis
rabbitMq
mysql

## 秒杀流程
![avatar](https://img-blog.csdnimg.cn/33c9c0de80204c6cb429b638ca1cf699.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6L-Y5LiN5b-r54K55rua5Y6755yL5Lmm77yf,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

# 相关优化

## 服务优化
### 1.解决超卖的问题：
1.redis预减库存，有一个下单请求过来，若减完之后redis的库存小于0，则直接返回客户端库存不足，同时设置内存标记，减少下次redis的访问。如果扣除redis库存成功，则异步下单，将创建订单的消息发给mq，返回客户排队中。
```java
 		//1.通过内存标记减少redis的访问
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
```
2.数据库层面的操作，redis预减库存只是拿到秒杀的资格，真正秒杀成功，是消息队列的消费者成功消费了消息，在数据库创建了订单。
（1）数据库层面，我们创建了订单表用户id，商品Id的组合唯一索引，防止重复下单
（2）数据库层面，减少库存的时候，我们要判断此时库存是否大于零

```java
boolean updateResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count-1")
                .eq("goods_id", goods.getId())
                .gt("stock_count", 0));
```
### 2.保证MQ不丢失消息
#### （1）生产者向MQ发送消息，网络问题造成消息丢失
开启生产者的confirm模式，如果消息被投递到相应的队列，则mq会返回一个ack给生产者，如果rabbitmq自身错误导致消息丢失，则mq会返回一个nack给生产者，生产者重发消息
#### （2）MQ丢失消息
开启持久化，创建queue的时候设置持久化，生产者发送消息的时候也要设置持久化。持久化与confirm模式结合时，只有持久化完成之后，才会返回一个ack消息，如果持久化失败，会返回nack消息。
#### （3）消费者丢失消息
关闭的MQ的自动确认功能，改为手动确认。自动确认是当队列中的消息被消费后，立即删除该消息，而手动确认是MQ收到消费者发来的ack消息才删除消息。
### 3.使用rabbitMQ进行异步下单
实现秒杀模块和下订单模块进行解耦，实现异步下单，减少高峰期服务器的压力
### 4.接口限流
1.使用令牌桶算法进行接口限流。
令牌桶是按照固定的速率生成令牌，请求能从桶中获取令牌就执行，否则失败。漏桶算法是进入漏桶的速率不限，但是出桶的速率是固定的。在突发的适应性上，令牌桶一次性可以获得多个令牌，允许突然的大流量。漏桶算法是限制流出的速率，不能处理突发的大流量。

我们这里使用的是Google的RateLimiter，是一个单机的限流算法，所有的令牌都是在内存中的。

![avatar](https://upload-images.jianshu.io/upload_images/2996559-eb57febdf713e2bd.png) 

RateLimiter具有两个特殊机制：

1.RateLimiter生成令牌不是采取定时生成的方式，而是采取延迟计算，即在获取令牌时，计算 【当前时间- nextFreeTicketMicros (下一次请求可以被授予令牌的时间)/令牌生成速率】，获得令牌数。

2.RateLimiter还有一个偿还机制，当前请求的债务(请求的令牌数大于当前存储的令牌数)，当前请求可以直接执行，下一次请求来偿还，也就是必须等上一次请求欠的令牌数生产出来才能被授予。 

相关资料：

https://blog.csdn.net/qq_34069882/article/details/107716560

https://blog.csdn.net/bohu83/article/details/51596346

### 5.保证MQ不重复消费
我们这里不保证MQ重复消费，项目中每个消息都带有用户Id和商品Id，下单成功我们会把用户Id和商品Id作为key，将订单存入redis中，在消费端我们查看redis，来判断是否重复消费。

## 安全优化
### 1.动态隐藏秒杀url：
为了防止坏人提前知道我们的秒杀url，在秒杀活动开始前写一些脚本将我们的商品秒杀光，我们需要隐藏我们的秒杀地址。秒杀之前我们需要调用getPath()方法获取经过MD5加密的字符，作为接下来秒杀地址的一部分。这个创建的加密字符我们会存入到redis中，过期时间设为5秒，意味着5秒之后url就过期了，需要重新请求，就极大的减少了被脚本秒光商品的问题。
### 2.加验证码：
加验证码可以防止脚本秒杀我们的商品，同时也可以分散客户点击流量，减少某一时间的流量，分摊到后面的一段时间中。我们的验证码创建之后是存在redis中，过期时间是10秒，10秒之后就需要重新请求。
### 3.接口防刷
同一个用户，五秒内只能访问五次，超过五次就拒绝访问。通过将访问次数存入redis中实现。
```java
		//限制访问次数，5秒内最多只能访问五次
        String uri = request.getRequestURI();
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());

        if (count == null) {
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) {
            valueOperations.increment(uri + ":" + user.getId());
        } else {
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
        }
```