package io.renren.modules.sys.vo;

import io.renren.modules.sys.entity.SysUserEntity;
import lombok.Data;

/**
 *
 * @author changbindong
 * @date 4/8/23
 * @description
 */
@Data
public class SysUserVo extends SysUserEntity {

	private static final long serialVersionUID = -1825863635357506499L;

	private String roleName;
}
