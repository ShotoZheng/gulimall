package com.atguigu.common.validate.validators;

import com.atguigu.common.validate.annotations.ListValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义注解 @ListValue校验器
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> dataSet = new HashSet<>();

    /**
     * 初始化方法
     * values 数组的值为注解上指明的值，即0和1
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        if (values != null) {
            for (int val : values) {
                dataSet.add(val);
            }
        }
    }

    /**
     * 校验方法
     * @param value 需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return dataSet.contains(value);
    }
}
