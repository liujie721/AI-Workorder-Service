package com.aiworkorder.ai_workorder_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final org.springframework.context.ApplicationContext applicationContext;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/auth/login", "/api/auth/register", "/api/auth/logout", "/auth/**",
            "/captcha/**", "/api/public/**", "/public/**", "/swagger-ui/**",
            "/swagger-ui.html", "/v3/api-docs/**", "/doc.html", "/webjars/**",
            "/swagger-resources/**", "/static/**", "/favicon.ico", "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) return true;
        return EXCLUDE_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response); return;
            }

            String token = authHeader.substring(7).trim();
            if (!StringUtils.hasText(token)) {
                chain.doFilter(request, response); return;
            }

            String username = jwtUtil.getUsernameFromToken(token);
            if (!StringUtils.hasText(username)) {
                chain.doFilter(request, response); return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // ✅ 运行时获取 UserDetailsService
                UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtUtil.validateToken(token, userDetails)) {
                    chain.doFilter(request, response); return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT 认证成功: username={}", username);
            }
        } catch (Exception e) {
            log.error("JWT 过滤器异常: {}", e.getMessage(), e);
        }
        chain.doFilter(request, response);
    }
}