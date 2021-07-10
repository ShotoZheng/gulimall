package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

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

    /**
     * 更新分类和品牌分类关联表的分类名称
     * @param category
     */
//    @CacheEvict(value = "category", key = "'categoryKey'")
    @CacheEvict(value = "category", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'categoryKey'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategoryName(category.getCatId(), category.getName());
    }

    /**
     * 使用 SpringCache 来实现缓存
     * @return
     */
    @Cacheable(value = "category", key = "'categoryKey'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys.....");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 自动添加缓存
     * @return
     */
    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        return getCatalogJsonFromDb();
    }

    /**
     * 手动添加缓存
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonSelf() {
        // 1. 查询缓存
        String catalogJsonString = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJsonString)) {
            // 2. 缓存对象不存在则查询数据库，并序列化后存入缓存中
            return getCatalogJsonWithRedissonLock();
        }
        // 3. 缓存对象存在那么反序列后返回
        Map<String, List<Catelog2Vo>> catalogJson = JSON.parseObject(catalogJsonString, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return catalogJson;
    }

    /**
     * redisson lock 锁实现
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonWithRedissonLock() {
        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            String catalogJson = redisTemplate.opsForValue().get("catalogJson");
            if (StringUtils.isNotEmpty(catalogJson)) {
                dataFromDb = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
            } else {
                dataFromDb = getCatalogJsonFromDb();
                String s = JSON.toJSONString(dataFromDb);
                redisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
            }
        } finally {
            // 解锁
            lock.unlock();
        }
        return dataFromDb;
    }

    /**
     * 采用 redis lock 实现分布式锁(双重检查锁)
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        // 加锁的同时并设置过期时间
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> dataFromDb;
        if (lock) {
            log.info("获取分布式锁成功...");
            try {
                String catalogJSON = redisTemplate.opsForValue().get("catalogJson");
                if (StringUtils.isNotEmpty(catalogJSON)) {
                    dataFromDb = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
                } else {
                    dataFromDb = getCatalogJsonFromDb();
                    String s = JSON.toJSONString(dataFromDb);
                    redisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
                }
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            log.info("获取分布式锁失败...等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getCatalogJsonWithRedisLock();
        }

    }

    /**
     * 使用本地锁
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonWithSynchronized() {
        String catalogJsonString = null;
        synchronized (this) {
            // 1. 查询缓存
            catalogJsonString = redisTemplate.opsForValue().get("catalogJson");
            if (StringUtils.isEmpty(catalogJsonString)) {
                // 2. 缓存对象不存在则查询数据库，并序列化后存入缓存中
                Map<String, List<Catelog2Vo>> catalogJson = getCatalogJsonFromDb();
                String jsonString = JSON.toJSONString(catalogJson);
                redisTemplate.opsForValue().set("catalogJson", jsonString);
                return catalogJson;
            }
        }
        // 3. 缓存对象存在那么反序列后返回
        TypeReference<Map<String, List<Catelog2Vo>>>  typeReference = new TypeReference<Map<String, List<Catelog2Vo>>>(){};
        Map<String, List<Catelog2Vo>> catalogJson = JSON.parseObject(catalogJsonString, typeReference);
        return catalogJson;
    }

    /**
     * 数据库查询二三级分类数据
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        log.info("从数据库查询二三级分类数据.....");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParentCategory(selectList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCategory(selectList, v.getCatId());
            //2、封装上面面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParentCategory(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    private List<CategoryEntity> getParentCategory(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return collect;
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