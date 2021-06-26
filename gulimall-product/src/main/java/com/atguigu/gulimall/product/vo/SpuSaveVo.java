/**
 * Copyright 2019 bejson.com
 */
package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {
    /** spu 基本信息部分 pms_spu_info*/
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    /** spu 描述信息 pms_spu_info_desc*/
    private List<String> decript;
    /** spu 图片集信息 pms_spu_images*/
    private List<String> images;
    /** spu 积分信息 sms_spu_bounds */
    private Bounds bounds;
    /** spu 规格参数 pms_product_attr_value*/
    private List<BaseAttrs> baseAttrs;
    /** sku 相关信息*/
    private List<Skus> skus;

}