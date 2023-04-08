/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("order_detail")
public class OrderDetailEntity implements Serializable {

	private static final long serialVersionUID = -5287957996228262445L;

	/**
	 * ID
	 */
	@TableId
	private Long id;

	/**
	 * 订单号 - 执行单号
	 */
	private String orderNo;

	/**
	 * 客户名称
	 */
	private String customerName;

	/**
	 * 项目名称
	 */
	private String projectName;

	/**
	 * 金额 （实际金额*100，展示需要处理）
	 */
	private Integer amount;

	/**
	 * 下单人名称
	 */
	private String bookUser;

	/**
	 * 下单人手机号
	 */
	private String bookUserPhone;

	/**
	 * 客户管理员名称
	 */
	private String customerManager;

	/**
	 * 客户管理员电话
	 */
	private String customerManagerPhone;

	/**
	 * 代理商名称
	 */
	private String agent;

	/**
	 * 代理商电话
	 */
	private String agentPhone;

	/**
	 * 下单时间
	 */
	private Date bookTime;

	/**
	 * 订单状态
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private Date createTime;

}
