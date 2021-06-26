/**
 * Copyright 2019 bejson.com
 */
package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * pms_product_attr_value
 */
@Data
public class BaseAttrs {

    private Long attrId;
    /** 属性值 attrValues*/
    private String attrValues;
    /** 快速展示 quickShow*/
    private int showDesc;

}