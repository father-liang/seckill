package com.xxx.seckill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author Liang
 * @since 2022-01-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_seckill_goods")
public class SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀商品ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 库存数量
     */
    private Integer stockCount;

    /**
     * 秒杀开始时间
     */
    private Date startDate;

    /**
     * 秒杀结束时间
     */
    private Date endDate;


}
