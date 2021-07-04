package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 13:54:59
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 树形查询分类列表数据
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 通过分类标识删除分类
     * @param catIds 分类标识
     */
    void removeMenuByIds(List<Long> catIds);

    /**
     * 查找三级分类路径
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateDetail(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catelog2Vo>> getCatalogJson();
}

