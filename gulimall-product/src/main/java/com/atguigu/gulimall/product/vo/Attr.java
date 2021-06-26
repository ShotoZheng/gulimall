/**
  * Copyright 2019 bejson.com 
  */
package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * sku 销售属性值 pms_sku_sale_attr_value
 */
@Data
public class Attr {

    private Long attrId;
    private String attrName;
    private String attrValue;

}