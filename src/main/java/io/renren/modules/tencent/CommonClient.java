package io.renren.modules.tencent;


import io.renren.modules.tencent.exception.TencentCloudSDKException;
import io.renren.modules.tencent.profile.ClientProfile;

public class CommonClient extends AbstractClient{
    public CommonClient(String productName, String version, Credential credential, String region) {
        this(productName, version, credential, region, new ClientProfile());
    }

    public CommonClient(String productName, String version,
                        Credential credential, String region, ClientProfile profile) {
        super(productName + ".tencentcloudapi.com", version, credential, region, profile);
    }

    public String commonRequest(AbstractModel req, String actionName) throws TencentCloudSDKException {
        String rspStr = "";
        rspStr = this.internalRequest(req, actionName);
        return rspStr;
    }
}