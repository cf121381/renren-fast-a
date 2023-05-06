package io.renren.modules.tencent;

import javax.annotation.Resource;

import cn.hutool.json.JSONUtil;
import io.renren.modules.tencent.exception.TencentCloudSDKException;
import io.renren.modules.tencent.models.SendSmsRequest;
import io.renren.modules.tencent.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

	@Resource
	private Credential credential;

	@Value("${sms.sdkAppId}")
	private String sdkAppId;

	@Value("${sms.templateId}")
	private String templateId;

	public void sendSMS(String phone, String message) throws TencentCloudSDKException {

		SmsClient client = new SmsClient(credential, "ap-guangzhou");

		SendSmsRequest req = new SendSmsRequest();

		req.setSmsSdkAppId(sdkAppId);
		req.setSignName("佳杰订单Online");
		req.setTemplateId(templateId);
		req.setPhoneNumberSet(new String[] {phone});
		req.setTemplateParamSet(new String[] {message});

		SendSmsResponse res = client.SendSms(req);
		log.info("send auth code success, phone:{},code:{},result:{}", phone, message, JSONUtil.toJsonStr(res.getSendStatusSet()));
	}

}