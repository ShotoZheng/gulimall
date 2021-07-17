package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserInfoTo {

    private Long userId;
    /** 非登录用户(游客)的 key */
    private String userKey;
    private boolean tempUser = false;
}
