package com.teamhub.teamhub.config;

import com.teamhub.teamhub.entity.User;
import com.teamhub.teamhub.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 1. 直接放行登录、注册和 CORS 预检请求
        if ("/api/user/login".equals(requestURI) 
                || "/api/user/register".equals(requestURI)
                || "OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 原有的 JWT 验证逻辑，加上 try-catch 防异常中断
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    // 校验用户是否仍然存在（防止被删除后旧 token 还能用）
                    User user = userMapper.selectById(userId);
                    if (user == null) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"登录已失效，请重新登录\"}");
                        return;
                    }
                    String role = jwtUtil.getRole(token);
                    String username = jwtUtil.getUsername(token);
                    Long teamId = jwtUtil.getTeamId(token);

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            new AuthUser(userId, username, role, teamId),
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            // Token 解析失败，不设置认证信息，继续走过滤器链
            // 让后面的 AuthorizationFilter 决定是否放行
        }

        filterChain.doFilter(request, response);
    }
}