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
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 收货地址id
     */
    private Long deliveryAddrId;

    /**
     * 冗余过来的商品名称
     */
    private String goodsName;

    /**
     * 订单商品数量
     */
    private Integer goodsCount;

    /**
     * 商品单价
     */
    private BigDecimal goodsPrice;

    /**
     * 下单的渠道，1-pc，2-android，3-ios
     */
    private Integer orderChannel;

    /**
     * 订单状态，0-新建未支付，1-已支付，3-已收货，4-已退款，5-已完成
     */
    private Integer status;

    /**
     * 订单的创建时间
     */
    private Date createDate;

    /**
     * 订单的支付时间
     */
    private Date payDate;


}
