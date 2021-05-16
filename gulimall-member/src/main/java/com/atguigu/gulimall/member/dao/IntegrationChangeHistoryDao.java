package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.IntegrationChangeHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分变化历史记录
 * 
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 21:30:52
 */
@Mapper
public interface IntegrationChangeHistoryDao extends BaseMapper<IntegrationChangeHistoryEntity> {
	
}
