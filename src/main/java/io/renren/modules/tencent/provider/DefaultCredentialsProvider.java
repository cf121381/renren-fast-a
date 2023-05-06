package io.renren.modules.tencent.provider;


import io.renren.modules.tencent.Credential;
import io.renren.modules.tencent.exception.TencentCloudSDKException;

public class DefaultCredentialsProvider implements CredentialsProvider {
    @Override
    public Credential getCredentials() throws TencentCloudSDKException {
        Credential cred;
        try {
            cred = new EnvironmentVariableCredentialsProvider().getCredentials();
            return cred;
        } catch (TencentCloudSDKException e) {

        }
        try {
            cred = new ProfileCredentialsProvider().getCredentials();
            return cred;
        } catch (TencentCloudSDKException e) {

        }
        cred = new CvmRoleCredential();
        if (cred.getSecretId() != null && cred.getSecretKey() != null && cred.getToken() != null) {
            return cred;
        }
        throw new TencentCloudSDKException("No valid credential");
    }
}
