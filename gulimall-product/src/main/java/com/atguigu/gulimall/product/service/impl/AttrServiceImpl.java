package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    AttrAttrgroupRelationDao relationDao;

    @Resource
    AttrGroupDao attrGroupDao;

    @Resource
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if(attrIds == null || attrIds.size() == 0){
            return null;
        }
        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);
        return (List<AttrEntity>) attrEntities;
    }

    /**
     * ?????????????????????????????????????????????
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1?????????????????????????????????????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2????????????????????????????????????????????????????????????
        //2.1)?????????????????????????????????
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //2.2)??????????????????????????????
        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //2.3)?????????????????????????????????????????????????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(attrIds!=null && attrIds.size()>0){
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type",
                "base".equals(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(e -> {
                e.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),queryWrapper
        );
        // ?????? attr ??????????????????????????????
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> attrEntitys = page.getRecords();
        List<AttrRespVo> attrRespVos = attrEntitys.stream().map(attrEntity -> {
            AttrRespVo vo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, vo);

            // 1. ???????????? id ??????????????????
            CategoryEntity categoryEntity = categoryDao.selectById(vo.getCatelogId());
            if (categoryEntity != null) {
                vo.setCatelogName(categoryEntity.getName());
            }

            // 2. ?????? attr_id ????????????
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", vo.getAttrId()));
            if (relationEntity != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    vo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    /**
     * ?????? attr ?????? attrGroup ????????????
     * @param attr
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // ?????????????????????
            long attrGroupId = attr.getAttrGroupId();
            if (attrGroupId != 0) {
                AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
                entity.setAttrGroupId(attrGroupId);
                entity.setAttrId(attrEntity.getAttrId());
                relationDao.insert(entity);
            }
        }
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        if (attrEntity == null) {
            return respVo;
        }
        BeanUtils.copyProperties(attrEntity, respVo);

        // ?????? attrGroupId ??? catelogPath
        // 1. ?????? attr_group ??????????????? attr_group_id
        AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if (relationEntity != null) {
            respVo.setAttrGroupId(relationEntity.getAttrGroupId());
        }
        // 2. ????????????id ??????????????????
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        respVo.setCatelogPath(catelogPath);
        return respVo;
    }

    /**
     * ?????? attr ????????????????????????????????? ?????????
     * @param attr
     */
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // ??????????????????????????????
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrId(attr.getAttrId());
            entity.setAttrGroupId(attr.getAttrGroupId());
            if (relationEntity != null) {
                relationDao.update(entity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                // ??????
                relationDao.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAttr(List<Long> attrIds) {
        // ?????? attr ???
        this.removeByIds(attrIds);

        // ???????????????
        List<AttrAttrgroupRelationEntity> relationEntityList = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_id", attrIds));
        if (relationEntityList != null) {
            for (AttrAttrgroupRelationEntity relationEntity : relationEntityList) {
                relationDao.deleteByAttrId(relationEntity.getAttrId());
            }
        }
    }

    /**
     * ???????????????????????????
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return baseMapper.selectSearchAttrIds(attrIds);
    }


}