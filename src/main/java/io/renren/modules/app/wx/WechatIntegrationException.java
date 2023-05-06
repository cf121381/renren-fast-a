package io.renren.modules.app.wx;

public class WechatIntegrationException extends Exception {

	private static final long serialVersionUID = 2072617178199548751L;
	
	private int code;

    public WechatIntegrationException(int code) {
        this.code = code;
    }

    public WechatIntegrationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public WechatIntegrationException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public WechatIntegrationException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public WechatIntegrationException(int code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
	

}
