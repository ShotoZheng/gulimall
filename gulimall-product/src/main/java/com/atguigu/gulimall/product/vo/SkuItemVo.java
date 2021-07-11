package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVo {
    /**sku基本信息获取  pms_sku_info*/
    SkuInfoEntity info;

    /** 是否有库存 */
    boolean hasStock = true;

    /**sku的图片信息  pms_sku_images*/
    List<SkuImagesEntity> images;

    /** 获取spu的介绍 */
    SpuInfoDescEntity desp;

    /**获取spu的销售属性组合。*/
    List<SkuItemSaleAttrVo> saleAttr;

    /** 获取spu的规格参数信息。*/
    List<SpuItemAttrGroupVo> groupAttrs;

}
