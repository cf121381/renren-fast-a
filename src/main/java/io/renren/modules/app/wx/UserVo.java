package io.renren.modules.app.wx;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;
import lombok.Data;

/**
 *
 * @author changbindong
 * @date 4/10/23
 * @description
 */
@Data
public class UserVo implements Serializable {

	private static final long serialVersionUID = -7199950799404102655L;

	private String token;

	private String openId;

	private String roleName;

	private Long userId;

	private String userName;

	private String phone;

	private List<Long> roleIdList = Lists.newArrayList();
}
