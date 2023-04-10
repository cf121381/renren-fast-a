package io.renren.modules.sys.vo;

import io.renren.modules.app.entity.OrderDetailEntity;
import lombok.Data;

/**
 *
 * @author changbindong
 * @date 4/10/23
 * @description
 */
@Data
public class OrderVo extends OrderDetailEntity {

	private String bookDateStr;

	private String amountStr;
}
