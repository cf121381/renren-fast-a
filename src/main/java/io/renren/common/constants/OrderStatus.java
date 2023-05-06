package io.renren.common.constants;

import io.renren.common.exception.RRException;

/**
 *
 * @author changbindong
 * @date 4/10/23
 * @description
 */
public enum OrderStatus {
	ACCEPT(1, "已接单"),
	PAID(2, "已付款");

	OrderStatus(Integer value, String name) {
		this.value = value;
		this.name = name;
	}

	private Integer value;

	private String name;

	public Integer getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public static OrderStatus of(Integer status){
		for (OrderStatus orderStatus : OrderStatus.values()){
			if(orderStatus.getValue().equals(status)){
				return orderStatus;
			}
		}
		throw new RRException("未知的订单状态");
	}

	public static OrderStatus parseByName(String name){
		for (OrderStatus orderStatus : OrderStatus.values()){
			if(orderStatus.getName().equals(name)){
				return orderStatus;
			}
		}
		return null;
	}
}
