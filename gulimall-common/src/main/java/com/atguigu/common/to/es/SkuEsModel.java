package com.atguigu.common.to.es;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {

    private Long skuId;

    /** 上架 spuId */
    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    /** 是否有库存 */
    private Boolean hasStock;

    /** 热点值 */
    private Long hotScore;

    private Long brandId;

    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    /** 可被检索 product_attr */
    private List<Attrs> attrs;

    /**
     * SPU 销售属性 product_attr_value
     */
    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

}
