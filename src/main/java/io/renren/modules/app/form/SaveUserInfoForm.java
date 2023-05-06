package io.renren.modules.app.form;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 *
 * @author changbindong
 * @date 4/11/23
 * @description
 */
@Data
public class SaveUserInfoForm implements Serializable {

	private static final long serialVersionUID = 4425627047549523446L;

	@NotBlank(message="openId不能为空")
	private String openId;

	@NotBlank(message="token不能为空")
	private String token;

	@NotBlank(message="userId不能为空")
	private Long userId;

	@NotBlank(message="手机号不能为空")
	private String phone;

	@NotBlank(message="手机验证码不能为空")
	private String code;
}
