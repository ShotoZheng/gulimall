/**
  * Copyright 2019 bejson.com 
  */
package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分信息 sms_spu_bounds
 */
@Data
public class Bounds {

    /** 金币 */
    private BigDecimal buyBounds;
    /** 成长值 */
    private BigDecimal growBounds;


}