package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * openfeign 远程调用 gulimall-coupon 服务
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    String REQ_MAP = "coupon/coupon";

    @RequestMapping(REQ_MAP + "/list")
    R list(@RequestParam Map<String, Object> params);
}
