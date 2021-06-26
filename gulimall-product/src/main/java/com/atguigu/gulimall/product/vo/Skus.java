/**
  * Copyright 2019 bejson.com 
  */
package com.atguigu.gulimall.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Skus {
    /** sku 销售属性信息 pms_sku_sale_attr_value*/
    private List<Attr> attr;
    /** sku 基本信息 pms_sku_info*/
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    /** sku 图片集 pms_sku_image*/
    private List<Images> images;
    private List<String> descar;
    /**折扣信息 sms_sku_ladder*/
    private int fullCount;
    private BigDecimal discount;
    /** 叠加优惠状态*/
    private int countStatus;
    /**满减信息 ：sms_sku_full_reduction*/
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    /** 叠加优惠状态*/
    private int priceStatus;
    /** 会员价格 sms_member_price*/
    private List<MemberPrice> memberPrice;


}