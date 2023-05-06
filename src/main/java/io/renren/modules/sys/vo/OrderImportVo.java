package io.renren.modules.sys.vo;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.excel.annotation.ExcelProperty;
import io.renren.common.constants.OrderStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author changbindong
 * @date 4/10/23
 * @description
 */
@Data
public class OrderImportVo implements Serializable {

	private static final long serialVersionUID = -8896849862744883896L;

	@ApiModelProperty(value = "Excel行号")
	private int rowNum;

	@ApiModelProperty(value = "此行导入状态 0-失败 1-成功")
	private int stat = 1;

	@ApiModelProperty(value = "错误信息")
	private String errorMsg;

	/**
	 * 验证结果
	 */
	private Boolean flag;

	private boolean update;

	/**
	 * 订单号 - 执行单号
	 */
	@ExcelProperty("执行单号")
	private String orderNo;

	/**
	 * 客户名称
	 */
	@ExcelProperty("客户名称")
	private String customerName;

	/**
	 * 项目名称
	 */
	@ExcelProperty("项目名称")
	private String projectName;

	/**
	 * 金额 （实际金额*100，展示需要处理）
	 */
	@ExcelProperty("金额")
	private String amount;

	/**
	 * 下单人名称
	 */
	@ExcelProperty("厂商销售")
	private String bookUser;

	/**
	 * 下单人手机号
	 */
	@ExcelProperty("厂商销售电话")
	private String bookUserPhone;

	/**
	 * 客户管理员名称
	 */
	@ExcelProperty("佳杰销售")
	private String customerManager;

	/**
	 * 客户管理员电话
	 */
	@ExcelProperty("佳杰销售电话")
	private String customerManagerPhone;

	/**
	 * 代理商名称
	 */
	@ExcelProperty("代理商销售")
	private String agent;

	/**
	 * 代理商电话
	 */
	@ExcelProperty("代理商销售电话")
	private String agentPhone;

	/**
	 * 订单日期
	 */
	@ExcelProperty("订单日期")
	private String bookTimeStr;

	private Date bookTime;

	/**
	 * 订单状态
	 */
	@ExcelProperty("订单状态")
	private String statusStr;


	private OrderStatus orderStatus;

}
