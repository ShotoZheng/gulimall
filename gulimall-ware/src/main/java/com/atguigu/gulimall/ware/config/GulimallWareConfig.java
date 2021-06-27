package com.atguigu.gulimall.ware.config;

import com.atguigu.common.config.MybatisPlusConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author zhengst
 */
@Configuration
@Import(MybatisPlusConfig.class)
public class GulimallWareConfig {
}
