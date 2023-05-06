/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "订单列表查询表单")
public class OrderQueryForm {
	@ApiModelProperty(value = "订单号")
	private String orderNo;

	@ApiModelProperty(value = "客户名称")
	private String customerName;

	@ApiModelProperty(value = "订单状态")
	private Integer status;

	@ApiModelProperty(value = "客户管理员名称")
	private String customerManager;

	private int page;

	private int limit;

}
