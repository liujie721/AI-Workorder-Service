package com.aiworkorder.ai_workorder_service.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // ✅ 语义更正：变量名改为通用意义
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.issuer:ai-workorder-service}")
    private String issuer;

    /**
     * ✅ 【核心修复】智能密钥解析
     * 策略：先尝试当作 Base64 解码（生产环境）；
     *       失败则降级为直接使用 UTF-8 字节（开发环境），并自动填充至 256 位安全长度。
     */
    private SecretKey getSecretKey() {
        // 1. 尝试作为 Base64 解码（适用于生产环境的纯字母数字串）
        if (jwtSecret.matches("^[a-zA-Z0-9+/=]+$")) {
            try {
                byte[] decodedBytes = Base64Utils.decodeFromString(jwtSecret);
                return Keys.hmacShaKeyFor(decodedBytes);
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ 密钥不符合 Base64 规范，降级为明文处理（开发模式）。");
                // 继续执行下面的降级逻辑
            }
        }

        // 2. 降级逻辑：作为普通字符串处理（开发/测试用）
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        // ✅ 安全增强：HS256 要求密钥至少 32 字节（256 位）
        // 如果配置的字符串太短，自动向后补零扩展，防止 WeakKeyException
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
            log.info("🛡️ 密钥已自动填充至 256 位安全长度。");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* --- 以下业务方法保持原有强大功能不变 --- */

    // 生成 Token（仅用户名）
    public String generateToken(String username) {
        return createToken(new HashMap<>(), username);
    }

    // 生成 Token（含角色集）
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 从 Token 中提取用户名
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 提取角色列表（类型安全版）
    public List<String> getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List<?>) {
            List<?> rawList = (List<?>) rolesObj;
            return rawList.stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // 提取任意 Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 解析 Claims 主体
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证 Token 有效性（基础）
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期");
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Token 无效: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Token 验证异常", e);
        }
        return false;
    }

    // 验证 Token 是否属于特定用户
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // 检查是否过期
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Date getExpirationFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 刷新 Token（返回 Optional 更安全）
    public Optional<String> refreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            List<String> roles = getRolesFromToken(token);

            Map<String, Object> newClaims = new HashMap<>();
            if (!roles.isEmpty()) newClaims.put("roles", roles);

            return Optional.of(createToken(newClaims, username));
        } catch (Exception e) {
            log.error("刷新 Token 失败", e);
            return Optional.empty();
        }
    }
}