package io.renren.modules.app.wx;


public enum WechatIntegrationExceptionConstant implements StdExceptionInfo {

	OTHER_ERROR(9000, "We encountered an error: {}", "微信：未知错误"),
	CANNOT_GET_ACCESS_TOKEN(9001, "Cannot get access token", "微信：无法获取access_token"),
	LOGIN_FAILED(9002, "User login failed", "微信：用户登录错误"),
	INVOKE_API_ERROR(9003, "Invoke wechat API failed", "调用微信API错误"),
	INVALID_PARAMETERS(9004, "Invalid parameters", "非法参数"),

	;

	private int code;

	private String message;

	private String description;

	WechatIntegrationExceptionConstant(int code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
