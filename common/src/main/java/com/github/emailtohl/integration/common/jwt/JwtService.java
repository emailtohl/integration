package com.github.emailtohl.integration.common.jwt;

import java.security.Key;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;

import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.crypto.MacProvider;

/**
 * <p>缓存Key的服务</p>
 * @author HeLei
 */
//@Service
public class JwtService {
    /**
     * 日志
     */
    private static final Logger LOG = LogManager.getLogger(JwtService.class);
    /**
     * 缓存名
     */
    public static final String CACHE_MANAGER_NAME = "cache_key";
    /**
     * 在ServletContext里面传递jwt的密钥
     */
    public static final String JWT_KEY_NAME = "jwt_key_name";
    /**
     * 存储Key的缓存
     */
    private Cache cache;
    /**
     * 解码器
     */
    private volatile JwtParser jwtParser;
    /**
     * gson
     */
    private Gson gson;

    /**
     * @param manager
     */
//  @Autowired
    public JwtService(CacheManager manager) {
        super();
        gson = new Gson();
        cache = manager.getCache(CACHE_MANAGER_NAME);
        refresh();
    }
    
    public Key getKey() {
        return cache.get(JWT_KEY_NAME, Key.class);
    }
    
    public synchronized void setKey(Key key) {
        cache.put(JWT_KEY_NAME, key);
        jwtParser = Jwts.parser().setSigningKey(key);
    }
    
    /**
     * 定时刷新Key
     */
    @Scheduled(fixedRate = 1000 * 3600 * 24)
    public synchronized void refresh() {
        Key key = MacProvider.generateKey();
        cache.put(JWT_KEY_NAME, key);
        jwtParser = Jwts.parser().setSigningKey(key);
    }
    
    /**
     * 将用户信息加密成jwt
     * @param profile
     * @return
     */
    public String encrypt(Profile profile) {
        String subject = gson.toJson(profile);
        String compact = Jwts.builder().setSubject(subject).signWith(SignatureAlgorithm.HS512, getKey()).compact();
        return compact;
    }
    
    /**
     * 解密jwt
     * @param compact
     * @return
     */
    public Profile decrypted(String compact) {
        String text = null;
        try {
            text = jwtParser.parseClaimsJws(compact).getBody().getSubject();
        } catch (UnsupportedJwtException e) {
            LOG.trace("claimsJws argument does not represent an Claims JWS", e);
        } catch (MalformedJwtException e) {
            LOG.trace("claimsJws string is not a valid JWS", e);
        } catch (SignatureException e) {
            LOG.trace("claimsJws JWS signature validation fails", e);
        } catch (ExpiredJwtException e) {
            LOG.trace("JWT is a Claims JWT and the Claims has an expiration time before the time this method is invoked", e);
        }
        return gson.fromJson(text, Profile.class);
    }
}
