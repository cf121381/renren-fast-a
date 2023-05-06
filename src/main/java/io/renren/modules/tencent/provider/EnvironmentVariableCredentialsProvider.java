package io.renren.modules.tencent.provider;


import io.renren.modules.tencent.Credential;
import io.renren.modules.tencent.exception.TencentCloudSDKException;

public class EnvironmentVariableCredentialsProvider implements CredentialsProvider {

	@Override
	public Credential getCredentials() throws TencentCloudSDKException {
		String secretId = System.getenv("TENCENTCLOUD_SECRET_ID");
		String secretKey = System.getenv("TENCENTCLOUD_SECRET_KEY");
		if (secretId == null || secretKey == null) {
			throw new TencentCloudSDKException("Not found secretId or secretKey");
		}
		if (secretId.length() == 0) {
			throw new TencentCloudSDKException("Environment variable secretId cannot be empty");
		}
		else if (secretKey.length() == 0) {
			throw new TencentCloudSDKException("Environment variable secretKey cannot be empty");
		}
		else {
			return new Credential(secretId, secretKey);
		}
	}
}
