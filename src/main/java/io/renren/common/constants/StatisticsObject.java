package io.renren.common.constants;

import io.renren.common.exception.RRException;

/**
 *
 * @author changbindong
 * @date 5/5/23
 * @description
 */
public enum StatisticsObject {
	BOOK_USER("book_user"),
	CUSTOMER_MANAGER("customer_manager"),
	AGENT("agent");

	private String value;

	StatisticsObject(String value) {
		this.value = value;
	}

	public static StatisticsObject of(String _value) {
		for (StatisticsObject statisticsType : StatisticsObject.values()) {
			if (statisticsType.value.equals(_value)) {
				return statisticsType;
			}
		}
		throw new RRException("未知的统计类型");
	}
}
