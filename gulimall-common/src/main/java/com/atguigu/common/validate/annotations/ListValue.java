package com.atguigu.common.validate.annotations;

import com.atguigu.common.validate.validators.ListValueConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shotozheng
 */
@Documented
@Constraint(validatedBy = { ListValueConstraintValidator.class }) // 指定自定义的校验器
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE }) // 注解支持使用的位置
@Retention(RUNTIME) // 注解运行时获取
public @interface ListValue {

    /** 默认提示信息 */
    String message() default "{com.atguigu.common.validate.annotations.ListValue.message}";

    /** 分组 */
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] values() default  { };
}
