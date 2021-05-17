package com.community.soob.account.config;

import com.community.soob.account.domain.UserAccount;
import com.community.soob.account.service.CustomUserDetailsService;
import com.community.soob.util.CookieUtil;
import com.community.soob.util.JwtUtil;
import com.community.soob.util.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    // 한 Request 당 한번만 검사
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final Cookie accessToken = cookieUtil.getCookie(httpServletRequest, JwtUtil.ACCESS_TOKEN_NAME);

        String accountEmail = null;
        String jwt = null;
        String refreshJwt = null;

        try{
            // AccessToken 존재시 토큰내에서 accountEmail 을 가져옴
            if (accessToken != null){
                jwt = accessToken.getValue();
                accountEmail = jwtUtil.getAccountEmail(jwt);
            }

            // 가져온 accountEmail 로 UserDetails 생성
            if (accountEmail != null){
                UserAccount userAccount = (UserAccount) userDetailsService.loadUserByUsername(accountEmail);

                // 토큰이 유효하다면, SecurityContext 에 인증토큰을 넣어줌
                if (jwtUtil.validateToken(jwt, userAccount)){
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userAccount.getAccount(),null, userAccount.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // AccessToken 기간 만료시
            Cookie refreshToken = cookieUtil.getCookie(httpServletRequest, JwtUtil.REFRESH_TOKEN_NAME);
            if (refreshToken != null) {
                refreshJwt = refreshToken.getValue();
            }
        }

        // AccessToken 의 기간이 만료되어 RefreshToken 을 생성했을때
        try {
            if (refreshJwt != null){
                String refreshAccountEmail = redisUtil.getData(refreshJwt);

                if (refreshAccountEmail.equals(jwtUtil.getAccountEmail(refreshJwt))) {
                    UserAccount userAccount = (UserAccount) userDetailsService.loadUserByUsername(refreshAccountEmail);
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userAccount.getAccount(),null,userAccount.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                    String newToken = jwtUtil.generateToken(refreshAccountEmail);

                    Cookie newAccessToken = cookieUtil.createCookie(JwtUtil.ACCESS_TOKEN_NAME, newToken);
                    httpServletResponse.addCookie(newAccessToken);
                }
            }
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage(), e.getCause());
        }

        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
