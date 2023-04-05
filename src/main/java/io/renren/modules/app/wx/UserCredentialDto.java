package io.renren.modules.app.wx;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * @Author: ChuNingfan
 * @Date: 2021/9/29 9:50
 **/
public class UserCredentialDto implements Serializable {

	private static final long serialVersionUID = 7116900186668594532L;

	private String unionId;

    private String openId;

    private String sessionKey;

    private List<Long> roleIdList = Lists.newArrayList();

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public List<Long> getRoleIdList() {
        return roleIdList;
    }

    public void setRoleIdList(List<Long> roleIdList) {
        this.roleIdList = roleIdList;
    }
}
