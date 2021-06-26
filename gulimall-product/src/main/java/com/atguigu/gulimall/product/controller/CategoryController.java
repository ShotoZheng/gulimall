package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;



/**
 * 商品三级分类
 *
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 13:54:59
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询商品分类，以树形方式返回分类列表
     */
    @RequestMapping("/list/tree")
    public R list() {
        List<CategoryEntity> data = categoryService.listWithTree();
        return R.ok().put("data", data);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 添加保存菜单信息
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateDetail(category);

        return R.ok();
    }

    /**
     * 删除分类信息，@RequestBody 仅支持使用 POST 请求
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
        // 删除菜单
		categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
