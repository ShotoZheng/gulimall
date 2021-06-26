package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * 响应实体对象
 * @author admin
 */
@Data
public class AttrRespVo extends AttrVo {
    /**
     * 分类名称
     */
    private String catelogName;

    /**
     * 分组名称
     */
    private String groupName;

    private Long[] catelogPath;
}
