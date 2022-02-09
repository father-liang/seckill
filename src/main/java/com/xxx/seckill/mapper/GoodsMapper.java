package com.xxx.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxx.seckill.pojo.Goods;
import com.xxx.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Liang
 * @since 2022-01-22
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * 获取商品列表
     * @return 商品列表
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 根据商品Id获取商品的信息
     * @return 商品信息
     * @param goodsId 商品Id
     */
    GoodsVo findGoodsVoByGoodsId(long goodsId);

}
