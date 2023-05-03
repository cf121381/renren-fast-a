package io.renren.modules.app.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.renren.common.annotation.WxSysLog;
import io.renren.common.utils.R;
import io.renren.modules.app.form.SaveUserInfoForm;
import io.renren.modules.app.utils.WebUtil;
import io.renren.modules.app.wx.MiniprogramHelper;
import io.renren.modules.app.wx.UserCredentialVo;
import io.renren.modules.app.wx.UserVo;
import io.renren.modules.app.wx.WechatIntegrationException;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

	@Resource
	private MiniprogramHelper miniprogramHelper;

	@GetMapping("/wxcredential")
	public R getWechatCredential(@RequestParam("jsCode") String jsCode) {
		try {
			UserCredentialVo user = miniprogramHelper.login(jsCode);
			return R.ok().put("userInfo", user);
		}
		catch (WechatIntegrationException e) {
			return R.error(e.getMessage());
		}
	}

	@GetMapping("/sendMsg")
	public R sendMsg(@RequestParam("openId") String openId, @RequestParam("token") String token, @RequestParam("phone") String phone) {
//		String token = getToken();
		String code = miniprogramHelper.sendMsg(openId, token, phone);
		return R.ok().put("smsCode", code);
	}

	@PostMapping("save_userInfo")
	public R saveUserInfo(@RequestBody SaveUserInfoForm form) {
		UserVo vo = miniprogramHelper.saveUserInfo(form);
		return R.ok().put("data", vo);
	}

	@WxSysLog("user_login_query")
	@GetMapping("/get_user_info")
	public R getUserInfo(@RequestParam("token")String token,@RequestParam("openId")String openId){
		UserVo vo = miniprogramHelper.getUserInfo(openId,token);
		return R.ok().put("data", vo);
	}

	private String getToken() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		String token = request.getHeader("token");
		if (StringUtils.isBlank(token)) {
			token = WebUtil.getCookie(request, "token");
		}
		return token;
	}
}
