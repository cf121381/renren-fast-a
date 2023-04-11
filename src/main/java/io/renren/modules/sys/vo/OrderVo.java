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

	private static final long serialVersionUID = 6577847430538340923L;

	private String bookDateStr;

	private String amountStr;

	private String statusStr;
}
