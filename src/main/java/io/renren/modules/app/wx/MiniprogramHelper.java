package io.renren.modules.app.wx;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Resource;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import io.renren.common.exception.RRException;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.form.SaveUserInfoForm;
import io.renren.modules.app.service.UserService;
import io.renren.modules.app.utils.JwtUtils;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysUserRoleService;
import io.renren.modules.sys.service.SysUserService;
import io.renren.modules.tencent.SmsService;
import io.renren.modules.tencent.exception.TencentCloudSDKException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MiniprogramHelper {

	private static final Logger logger = LoggerFactory.getLogger(MiniprogramHelper.class);

	private static final String loginUrl = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

	private static final String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=%s";

	private static final String msgWeappTmpKey = "weappTemplateMsg";

	private static final String msgMpTmpKey = "mpTemplateMsg";

	private static final Map<String, String> phoneCodeMap = Maps.newConcurrentMap();

	@Resource
	private WechatConfig wechatConfig;

	@Resource
	private UserService userService;

	@Resource
	private SysUserRoleService sysUserRoleService;

	@Resource
	private SysUserService sysUserService;

	@Resource
	private JwtUtils jwtUtils;

	@Resource
	private SmsService smsService;

	public UserCredentialVo login(String jsCode) throws WechatIntegrationException {
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
		UserCredentialVo vo = new UserCredentialVo();
		String unionId = result.getString("unionid");
		String openId = result.getString("openid");
		String sessionKey = result.getString("session_key");
		vo.setOpenId(openId);
		vo.setUnionId(unionId);
		vo.setSessionKey(sessionKey);

		UserEntity userEntity = userService.queryByOpenId(openId);
		if (Objects.nonNull(userEntity)) {
			if (StringUtils.isNotBlank(userEntity.getMobile())) {
				List<Long> roleIdList = getRoleIdListByPhone(userEntity.getMobile());
				vo.getRoleIdList().addAll(roleIdList);
			}
		}
		else {
			userEntity = new UserEntity();
			userEntity.setUsername(openId);
			userEntity.setOpenId(openId);
			userEntity.setPassword("123456");
			userService.save(userEntity);
		}
		vo.setPhone(userEntity.getMobile());
		vo.setUserId(userEntity.getUserId());
		vo.setToken(jwtUtils.generateToken(userEntity.getUserId()));
		return vo;
	}

	private List<Long> getRoleIdListByPhone(String phone) {
		SysUserEntity sysUserEntity = sysUserService.queryByMobile(phone);
		if (Objects.isNull(sysUserEntity)) {
			return Lists.newArrayList();
		}
		List<Long> roleIdList = sysUserRoleService.queryRoleIdList(sysUserEntity.getUserId());
		return roleIdList;
	}

	private static final Map<String, Integer> checkCountCache = Maps.newConcurrentMap();

	public UserVo saveUserInfo(SaveUserInfoForm form) {
		UserEntity user = userService.queryByOpenId(form.getOpenId());
		if (Objects.isNull(user)) {
			throw new RRException("请先登录，再保存用户信息");
		}
		String key = form.getPhone() + form.getOpenId() + form.getToken();
		if (checkCountCache.containsKey(key)) {
			checkCountCache.put(key, checkCountCache.get(key) + 1);
		}
		else {
			checkCountCache.put(key, NumberUtils.INTEGER_ONE);
		}
		if (!phoneCodeMap.containsKey(key)) {
			clearCache(key);
			throw new RRException("验证码过期或者失效,请先发送验证码");
		}
		if (!phoneCodeMap.get(key).equals(form.getCode())) {
			if (checkCountCache.get(key) >= 3) {
				clearCache(key);
			}
			throw new RRException("验证码错误,请重新获取验证码");
		}
		user.setUsername(form.getPhone());
		user.setMobile(form.getPhone());
		userService.updateById(user);
		UserVo vo = new UserVo();
		vo.setToken(form.getToken());
		vo.setUserId(user.getUserId());
		vo.setPhone(form.getPhone());
		vo.setOpenId(form.getOpenId());
		vo.setRoleIdList(getRoleIdListByPhone(form.getPhone()));
		clearCache(key);
		return vo;
	}

	private void clearCache(String key) {
		checkCountCache.remove(key);
		phoneCodeMap.remove(key);
	}

	public UserVo getUserInfo(String openId, String token) {
		if (StringUtils.isEmpty(openId)) {
			throw new RRException("用户未登录，请先登录");
		}
		UserEntity user = userService.queryByOpenId(openId);
		if (Objects.isNull(user)) {
			throw new RRException("用户未登录，请先登录");
		}
		Claims claims = jwtUtils.getClaimByToken(token);
		if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
			throw new RRException(jwtUtils.getHeader() + "失效，请重新登录", HttpStatus.UNAUTHORIZED.value());
		}
		UserVo vo = new UserVo();
		vo.setToken(token);
		vo.setUserId(user.getUserId());
		vo.setPhone(user.getMobile());
		vo.setOpenId(openId);
		vo.setRoleIdList(getRoleIdListByPhone(user.getMobile()));
		return vo;
	}

	public UserVo getUserInfo(String openId) {
		if (StringUtils.isEmpty(openId)) {
			throw new RRException("用户未登录，请先登录");
		}
		UserEntity user = userService.queryByOpenId(openId);
		if (Objects.isNull(user)) {
			throw new RRException("用户未登录，请先登录");
		}
		UserVo vo = new UserVo();
		vo.setUserId(user.getUserId());
		vo.setPhone(user.getMobile());
		vo.setOpenId(openId);
		vo.setRoleIdList(getRoleIdListByPhone(user.getMobile()));
		return vo;
	}

	public String sendMsg(String openId, String token, String phone) throws TencentCloudSDKException {
		String code = generateCode(6);
		String key = phone + openId + token;
		phoneCodeMap.put(key, code);
		checkCountCache.put(key, NumberUtils.INTEGER_ZERO);
		smsService.sendSMS(phone, code);
		return null;
	}

	private static String generateCode(int length) {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			int digit = random.nextInt(10);
			sb.append(digit);
		}
		return sb.toString();
	}

}