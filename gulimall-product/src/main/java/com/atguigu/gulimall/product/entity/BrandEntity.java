package com.atguigu.gulimall.product.entity;

import com.atguigu.common.validate.groups.AddGroup;
import com.atguigu.common.validate.annotations.ListValue;
import com.atguigu.common.validate.groups.UpdateGroup;
import com.atguigu.common.validate.groups.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 * 
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 13:55:00
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "更新品牌时品牌id不能为空", groups = {UpdateGroup.class, UpdateStatusGroup.class})
	@Null(message = "新增品牌时品牌id必须为空", groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "品牌logo地址不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@URL(message = "品牌logo地址必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	@NotBlank(message = "品牌介绍不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "品牌状态不能为空", groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
	@ListValue(values = {0, 1}, groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "检索首字母不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须在a-z或者A-Z范围内", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序字段不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@PositiveOrZero(message = "排序字段必须是非负整数",  groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
