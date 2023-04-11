package io.renren.modules.app.utils;

import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: ChuNingfan
 * @Date: 2021/9/16 17:37
 **/
public class WebUtil {

    public static String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies) || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie: cookies) {
            String k = cookie.getName();
            if (k.equalsIgnoreCase(key)) {
                return cookie.getValue();
            }
        }
        return null;
    }


}
