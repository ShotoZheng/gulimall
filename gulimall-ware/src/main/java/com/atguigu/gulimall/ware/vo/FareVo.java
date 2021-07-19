package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 地址和运费
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
