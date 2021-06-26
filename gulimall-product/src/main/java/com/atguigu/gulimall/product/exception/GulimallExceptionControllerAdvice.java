package com.atguigu.gulimall.product.exception;


import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一异常处理器
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    /**
     * 方法参数异常校验
     * @return
     */
    @ExceptionHandler(value= MethodArgumentNotValidException.class)
    public R handleValidateException(MethodArgumentNotValidException e) {
        log.info("handleValidateException 异常信息:");
        e.printStackTrace();
        // 获取绑定结果
        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            bindingResult.getFieldErrors().forEach(item -> {
                errorMap.put(item.getField(), item.getDefaultMessage());
            });
            return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
        }
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }

    /**
     * 顶层异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Exception e) {
        log.info("handleException 异常信息:");
        e.printStackTrace();
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
