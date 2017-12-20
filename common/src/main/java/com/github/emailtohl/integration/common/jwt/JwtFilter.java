package com.github.emailtohl.integration.common.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * 实验形式的JWT过滤器
 * </p>
 * 
 * @author HeLei
 */
public class JwtFilter implements Filter {
    /**
     * 日志
     */
    private static final Logger LOG = LogManager.getLogger(Filter.class);
    /**
     * 线程上的username
     */
    public static final String USERNAME = "username";
    /**
     * Http请求头或Cookie名都用此名字，存放加密后的JWT
     */
    public static final String HEAD_NAME = "Authorization";
    /**
     * jwt的密钥的服务
     */
    private JwtService jwtService;
    /**
     * ant路径风格的匹配工具
     */
    private AntPathMatcher matcher = new AntPathMatcher();
    /**
     * 系统中定义的路径与权限匹配表
     */
    private List<PathAuthMap> ls;
    /**
     * Default constructor.
     */
    public JwtFilter() {
        ls = PathAuthMap.init();
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(fConfig.getServletContext());
        jwtService = context.getBean(JwtService.class);
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        String uri = req.getRequestURI();
        if (isIgnore(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        String compact = req.getHeader(HEAD_NAME);
        if (!StringUtils.hasText(compact)) {
            resp.sendError(HttpStatus.UNAUTHORIZED.value(), "没有JWT信息");
            return;
        }
        Profile profile = jwtService.decrypted(compact);
        if (profile == null) {
            resp.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT解析失败");
            return;
        }
        if (profile.getExp() < new Date().getTime()) {
            resp.sendError(HttpStatus.UNAUTHORIZED.value(), "已过期");
            return;
        }
        
        ThreadContext.put(USERNAME, profile.getUsername());
        
        if (check(uri, HttpMethod.valueOf(req.getMethod()), profile)) {
            chain.doFilter(req, resp);
            // 刷新时效
            profile.setExp(new Date().getTime() + Profile.EXP);
            compact = jwtService.encrypt(profile);
            resp.addHeader(HEAD_NAME, compact);
        } else {
            resp.sendError(HttpStatus.FORBIDDEN.value(), "没有权限");
        }
        ThreadContext.remove(USERNAME);
    }

    public boolean isIgnore(String uri) {
        if (matcher.match("/login", uri)) {
            return true;
        }
        if (matcher.match("/logout", uri)) {
            return true;
        }
        if (matcher.match("/authorityNames", uri)) {
            return true;
        }
        return false;
    }
    
    /**
     * 校验是否通过
     * @param uri 访问地址
     * @param method http方法
     * @param profile 用户身份信息
     * @return true or false
     */
    public boolean check(String uri, HttpMethod method, Profile p) {
        boolean isProtected = false; // 判断该uri是否被保护，若uri不在限制列表中，则应该返回true
        boolean res = false; // 最后结果，默认校验不通过
        for (PathAuthMap map : ls) {
            if (matcher.match(map.pattern, uri)) { // 匹配路径
                if (map.authorities.isEmpty() && (map.httpMethod == null || map.httpMethod == method)) { // 如果该路径所需的权限为空，表示不用保护，通过
                    LOG.debug(uri + " 没有对应权限，不需要被保护");
                    isProtected = false;
                    break;
                }
                isProtected = true; // 若匹配uri，则表明该uri在保护状态下
                // 若匹配所有http方法，并且两集合有交集
                if (map.httpMethod == null && !Collections.disjoint(map.authorities, p.getAuthorities())) {
                    res = true;
                    break;
                } else if (map.httpMethod == method && !Collections.disjoint(map.authorities, p.getAuthorities())) {
                    res = true;
                    break;
                }
                LOG.debug(String.format("%s:%s\n需要权限：%s\n与你拥有权限：%s，方法：%s 不匹配\n", uri, method.name(), map.authorities.toString(),
                        p.getAuthorities().toString(), map.httpMethod.name()));
            }
        }
        // 该uri不被保护则返回true，若在保护下，则返回校验结果
        return !isProtected || res;
    }

    /**
     * @param jwtService the jwtService to set
     */
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

}
