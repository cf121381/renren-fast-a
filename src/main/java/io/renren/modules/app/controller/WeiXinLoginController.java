package io.renren.modules.app.controller;

import io.renren.common.utils.R;
import io.renren.modules.app.wx.MiniprogramHelper;
import io.renren.modules.app.wx.UserCredentialDto;
import io.renren.modules.app.wx.WechatIntegrationException;
import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author changbindong
 * @date 4/6/23
 * @description
 */
@RestController
@RequestMapping("/weixin/")
@Api("微信登录接口")
public class WeiXinLoginController {

	@Autowired
	private MiniprogramHelper miniprogramHelper;

	@GetMapping("/wxcredential")
	public R getWechatCredential(@RequestParam("jsCode") String jsCode) {
		try {
			UserCredentialDto user = miniprogramHelper.login(jsCode);
			return R.ok().put("userInfo", user);
		}
		catch (WechatIntegrationException e) {
			return R.error(e.getMessage());
		}
	}
}
