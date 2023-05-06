package io.renren.common.constants;

import io.renren.common.exception.RRException;

/**
 *
 * @author changbindong
 * @date 5/5/23
 * @description
 */
public enum StatisticsType {
	ORDER_COUNT("order_count"),
	ORDER_TOTAL_AMOUNT("order_total_amount");

	private String value;

	StatisticsType(String value) {
		this.value = value;
	}

	public static StatisticsType of(String _value) {
		for (StatisticsType statisticsType : StatisticsType.values()) {
			if (statisticsType.value.equals(_value)) {
				return statisticsType;
			}
		}
		throw new RRException("未知的统计类型");
	}
}
