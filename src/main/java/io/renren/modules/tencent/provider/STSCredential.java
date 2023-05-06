package io.renren.modules.tencent.provider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.renren.modules.tencent.CommonClient;
import io.renren.modules.tencent.Credential;
import io.renren.modules.tencent.exception.TencentCloudSDKException;
import io.renren.modules.tencent.profile.ClientProfile;
import io.renren.modules.tencent.profile.HttpProfile;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class STSCredential extends Credential {
    private String secretId;
    private String secretKey;
    private String roleArn;
    private String roleSessionName;
    private String tmpSecretId;
    private String tmpSecretKey;
    private String token;
    private int expiredTime;

    public STSCredential(String secretId, String secretKey, String roleArn, String roleSessionName) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.roleArn = roleArn;
        this.roleSessionName = roleSessionName;
    }

    public String getSecretId() {
        if (tmpSecretId == null || needRefresh()) {
            try {
                updateCredential();
            } catch (TencentCloudSDKException e) {
                return null;
            }
        }
        return tmpSecretId;
    }

    public String getSecretKey() {
        if (tmpSecretKey == null || needRefresh()) {
            try {
                updateCredential();
            } catch (TencentCloudSDKException e) {
                return null;
            }
        }
        return tmpSecretKey;
    }

    public String getToken() {
        if (token == null || needRefresh()) {
            try {
                updateCredential();
            } catch (TencentCloudSDKException e) {
                return null;
            }
        }
        return token;
    }

    private void updateCredential() throws TencentCloudSDKException {
        Credential cred = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("sts.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        CommonClient client = new CommonClient("sts", "2018-08-13", cred, "ap-guangzhou", clientProfile);
        String resp = client.call("AssumeRole", "{\"RoleArn\":\"" + roleArn + "\","
                + "\"RoleSessionName\":\"" + roleSessionName + "\"}");
        Map<String, Object> map = new Gson().fromJson(resp, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        Map<String, Object> respmap = (Map<String, Object>) map.get("Response");
        Map<String, String> credmap = (Map<String, String>) respmap.get("Credentials");
        tmpSecretId = credmap.get("TmpSecretId");
        tmpSecretKey = credmap.get("TmpSecretKey");
        token = credmap.get("Token");
        expiredTime = ((Double) respmap.get("ExpiredTime")).intValue();
    }

    private boolean needRefresh() {
        if (expiredTime - new Date().getTime() / 1000 <= 300) {
            return true;
        } else {
            return false;
        }
    }
}
