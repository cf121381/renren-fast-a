package io.renren.common.utils;

import io.renren.common.exception.RRException;

/**
 *
 * @author changbindong
 * @date 4/10/23
 * @description
 */
public class AssertUtils {

	public static void isTrue(boolean condition, String errorMsg) {
		if (!condition) {
			throw new RRException(errorMsg);
		}
	}
}
