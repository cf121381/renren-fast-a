package io.renren.modules.sys.vo;

import java.io.Serializable;
import java.math.BigDecimal;

import io.renren.modules.sys.entity.SysUserEntity;
import lombok.Data;

/**
 *
 * @author changbindong
 * @date 5/6/23
 * @description
 */
@Data
public class CountTempVo implements Serializable {

	private static final long serialVersionUID = -2613220107724343050L;

	private SysUserEntity sysUser;

	private Integer orderCount;

	private BigDecimal orderTotalAmount;

	private BigDecimal orderAvgAmount;

}
