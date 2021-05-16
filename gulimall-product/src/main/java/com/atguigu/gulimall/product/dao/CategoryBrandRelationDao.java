package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌分类关联
 * 
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 13:55:00
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {
	
}
