package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

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
}

