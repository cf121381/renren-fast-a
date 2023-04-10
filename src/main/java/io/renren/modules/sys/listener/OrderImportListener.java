package io.renren.modules.sys.listener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import io.renren.common.constants.OrderStatus;
import io.renren.modules.sys.vo.OrderImportVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;


/**
 *
 * @author changbindong
 * @date 3/27/23
 * @description
 */
@Slf4j
@Data
public class OrderImportListener extends AnalysisEventListener<OrderImportVo> {

	private String MSG_SEPARATOR = ",";

	private List<OrderImportVo> list;

	public OrderImportListener(List<OrderImportVo> containerImportList) {
		this.list = containerImportList;
	}

	@Override
	public void invoke(OrderImportVo containerImportVo, AnalysisContext analysisContext) {
		containerImportVo.setRowNum(analysisContext.readRowHolder().getRowIndex() + 1);
		containerImportVo.setFlag(true);
		list.add(containerImportVo);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext analysisContext) {
		if (!baseCheck(list)) {
			return;
		}
		bizCheck(list);
	}

	/**
	 * 业务逻辑校验
	 * 订单编码
	 * @param orderImportList
	 */
	private void bizCheck(List<OrderImportVo> orderImportList) {

	}

	private boolean baseCheck(List<OrderImportVo> orderList) {
		if (CollectionUtils.isEmpty(orderList)) {
			log.info("订单导入处理完成，数据为空");
			return false;
		}

		List<String> repetitionOrders = orderList.stream()
				.collect(Collectors.groupingBy(OrderImportVo::getOrderNo, Collectors.counting()))
				.entrySet().stream().filter(entry -> entry.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(repetitionOrders)) {
			orderList.stream()
					.filter(order -> repetitionOrders.contains(order.getOrderNo()))
					.forEach(orderRow -> {
						orderRow.setFlag(false);
						orderRow.setErrorMsg("执行单号存在重复");
					});
			return false;
		}
		orderList.forEach(order -> {
			boolean result = true;
			StringBuilder errorMsg = new StringBuilder();

			order.setOrderStatus(OrderStatus.parseByName(order.getStatusStr()));

			if (StringUtils.isEmpty(order.getOrderNo())) {
				result = false;
				errorMsg.append("执行单号为空").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getCustomerName())) {
				result = false;
				errorMsg.append("客户名称为空").append(MSG_SEPARATOR);
			}
			if (Objects.isNull(order.getOrderStatus())) {
				result = false;
				errorMsg.append("未知的订单状态").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getProjectName())) {
				result = false;
				errorMsg.append("项目名称为空").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getBookUser())) {
				result = false;
				errorMsg.append("下单人名称为空").append(MSG_SEPARATOR);
			}
			if (Objects.isNull(order.getBookUserPhone())) {
				result = false;
				errorMsg.append("下单人手机号状态").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getCustomerManager())) {
				result = false;
				errorMsg.append("客户管理员名称为空").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getCustomerManagerPhone())) {
				result = false;
				errorMsg.append("客户管理员手机号为空").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getAgent())) {
				result = false;
				errorMsg.append("代理商为空").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getAgentPhone())) {
				result = false;
				errorMsg.append("代理商手机号为空").append(MSG_SEPARATOR);
			}
			if (StringUtils.isEmpty(order.getBookTimeStr())) {
				result = false;
				errorMsg.append("下单时间为空").append(MSG_SEPARATOR);
			}
			else {
				Date bookTime = parseDate(order.getBookTimeStr());
				if (Objects.isNull(bookTime)) {
					result = false;
					errorMsg.append("下单时间格式错误").append(MSG_SEPARATOR);
				}
				else {
					order.setBookTime(bookTime);
				}
			}
			order.setFlag(result);
			order.setErrorMsg(formatMsg(errorMsg));
		});

		return orderList.stream().allMatch(OrderImportVo::getFlag);
	}

	private static Date parseDate(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		dateFormat.setLenient(false);
		try {
			return dateFormat.parse(dateStr.trim());
		}
		catch (ParseException e) {
			return null;
		}
	}

	private String formatMsg(StringBuilder msg) {
		if (Objects.isNull(msg)) {
			return null;
		}
		if (msg.lastIndexOf(MSG_SEPARATOR) != -1) {
			return msg.substring(0, msg.lastIndexOf(MSG_SEPARATOR));
		}
		return msg.toString();
	}
}
