package com.xxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.seckill.pojo.Goods;
import com.xxx.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Liang
 * @since 2022-01-22
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 获取商品列表
     * @return 商品列表
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 根据商品Id查询商品的详情
     * @param goodsId 商品Id
     * @return 商品详情
     */
    GoodsVo findGoodsVoByGoodsId(long goodsId);
}
