package io.renren.modules.tencent.provider;


import io.renren.modules.tencent.Credential;
import io.renren.modules.tencent.exception.TencentCloudSDKException;

public interface CredentialsProvider {
    public Credential getCredentials() throws TencentCloudSDKException;
}