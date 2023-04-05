package io.renren.modules.app.wx;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.service.UserService;
import io.renren.modules.sys.service.SysUserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class MiniprogramHelper {

	private static final Logger logger = LoggerFactory.getLogger(MiniprogramHelper.class);

	private static final String loginUrl = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

	private static final String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=%s";

	private static final String msgWeappTmpKey = "weappTemplateMsg";

	private static final String msgMpTmpKey = "mpTemplateMsg";

	@Resource
	private WechatConfig wechatConfig;

	@Resource
	private UserService userService;

	@Resource
	private SysUserRoleService sysUserRoleService;

	public UserCredentialDto login(String jsCode) throws WechatIntegrationException {
		String appId = wechatConfig.getAppId();
		String secret = wechatConfig.getSecret();
		String url = String.format(loginUrl, appId, secret, jsCode);
		HttpRequest request = HttpUtil.createGet(url);
		HttpResponse response = request.execute();
		JSONObject result = JSONObject.parseObject(response.body());
		Integer errorCode = result.getInteger("errcode");
		if (Objects.nonNull(errorCode) && errorCode != 0) {
			logger.error("Miniprogram login failed: {}", result.getString("errmsg"));
			throw new WechatIntegrationException(WechatIntegrationExceptionConstant.LOGIN_FAILED.getCode());
		}
		UserCredentialDto dto = new UserCredentialDto();
		String unionId = result.getString("unionid");
		String openId = result.getString("openid");
		String sessionKey = result.getString("session_key");
		dto.setOpenId(openId);
		dto.setUnionId(unionId);
		dto.setSessionKey(sessionKey);

		UserEntity userEntity = userService.queryByOpenId(openId);
		if (Objects.nonNull(userEntity)) {
			List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userEntity.getUserId());
			dto.getRoleIdList().addAll(roleIdList);
		}
		return dto;
	}

}