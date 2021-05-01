package com.community.soob.util;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Component
public class CookieUtil {

    /*
     HttpOnly(true) 로 설정했으니 로그아웃시에는 쿠키의 만료시간을 0 으로 주고
     accessKey, refreshKey 에 아무값도 주지 않는다.
     */
    public Cookie createCookie(String cookieName, String value){
        Cookie token = new Cookie(cookieName, value);
        token.setHttpOnly(true);
        token.setMaxAge((int) JwtUtil.TOKEN_VALIDATION_SECOND);
        token.setPath("/");
        return token;
    }

    public Cookie getCookie(HttpServletRequest request, String cookieName){
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for(Cookie cookie : cookies){
            if(cookie.getName().equals(cookieName))
                return cookie;
        }

        return null;
    }
}
