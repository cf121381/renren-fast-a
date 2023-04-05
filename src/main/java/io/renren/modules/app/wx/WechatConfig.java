package io.renren.modules.app.wx;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Random;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "mp")
public class WechatConfig {

	private static final String accessTokenURL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

	private static final Integer okCode = 0;

	private volatile int tokenExpSec = 0;

	private volatile long firstAccessTokenTimePoint = 0;

	private volatile String tempToken = null;

	@Value("${mp.secret}")
	private String secret;

	@Value("${mp.appId}")
	private String appId;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAccessToken() throws WechatIntegrationException {
		if (!needToRequest()) {
			return tempToken;
		}
		String result = HttpUtil.get(String.format(accessTokenURL, appId, secret), Charset.defaultCharset());
		if (StringUtils.isBlank(result)) {
			throw new RuntimeException("Cannot get access token");
		}
		JSONObject tokenResp = JSONObject.parseObject(result);
		Integer errcode = tokenResp.getInteger("errcode");
		if (Objects.isNull(errcode) || errcode.equals(okCode)) {
			firstAccessTokenTimePoint = System.currentTimeMillis();
			tokenExpSec = tokenResp.getInteger("expires_in");
			tempToken = tokenResp.getString("access_token");
			return tempToken;
		}
		else {
			throw new WechatIntegrationException(WechatIntegrationExceptionConstant.OTHER_ERROR.getCode(), errcode + ": " + tokenResp.getString("errmsg"));
		}
	}

	private boolean needToRequest() {
		Random ran = new Random();
		long nowTime = System.currentTimeMillis();
		if (StringUtils.isBlank(tempToken) || (nowTime - firstAccessTokenTimePoint) / 1000 >= (tokenExpSec - ran.nextInt(10))) {
			return true;
		}
		return false;
	}

}