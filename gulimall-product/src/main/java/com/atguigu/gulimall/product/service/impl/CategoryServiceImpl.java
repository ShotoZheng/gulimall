package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 树形查询分类列表数据
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查询所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. 过滤获取所有父分类
        List<CategoryEntity> collect = entities.stream().filter(
                e -> e.getParentCid() == 0
        ).map(e -> {
            // 3. 设置每个父分类的子分类
            e.setChildren(getChildren(e, entities));
            return e;
            // 4. 对所有父分类进行升序排序
        }).sorted((e1, e2) -> (e1.getSort() == null ? 0 : e1.getSort()) - (e2.getSort() == null ? 0 : e2.getSort())).collect(Collectors.toList());
        return collect;
    }

    /**
     * 通过分类标识删除分类
     * @param catIds 分类标识
     */
    @Override
    public void removeMenuByIds(List<Long> catIds) {
        //TODO 检查被删除菜单是否被其他地方引用

        baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 查找三级分类路径
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        ArrayDeque<Long> pathQ = new ArrayDeque<>();
        Long catId = catelogId;
        while (catId != 0) {
            pathQ.addFirst(catId);
            CategoryEntity categoryEntity = baseMapper.selectById(catId);
            catId = categoryEntity.getParentCid();
        }
        return pathQ.toArray(new Long[pathQ.size()]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategoryName(category.getCatId(), category.getName());
    }

    /**
     * 递归设置父分类的所有子分类
     * @param curMenu 当前菜单分类实体
     * @param allMenus 所有菜单分类列表
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity curMenu, List<CategoryEntity> allMenus) {
        // 1. 过滤获取当前父分类的所有子分类
        List<CategoryEntity> entities = allMenus.stream().filter(e ->
                curMenu.getCatId().equals(e.getParentCid())
        ).map(e -> {
            // 2. 设置当前父分类的所有子分类
            e.setChildren(getChildren(e, allMenus));
            return e;
            // 3. 对所有父分类进行升序排序
        }).sorted((e1, e2) -> (e1.getSort() == null ? 0 : e1.getSort()) - (e2.getSort() == null ? 0 : e2.getSort())).collect(Collectors.toList());
        return entities;
    }
}