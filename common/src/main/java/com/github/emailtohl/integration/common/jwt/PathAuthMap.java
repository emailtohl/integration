package com.github.emailtohl.integration.common.jwt;

import static org.springframework.http.HttpMethod.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpMethod;

/**
 * <p>路径与权限的映射对象 </p>
 * @author HeLei
 */
class PathAuthMap {
    /**
     * 路径
     */
    final String pattern;
    /**
     * Http方法
     */
    final HttpMethod httpMethod;
    /**
     * 权限集
     */
    final Set<String> authorities;
    /**
     * @param pattern
     * @param httpMethod
     * @param authorities
     */
    public PathAuthMap(String pattern, HttpMethod httpMethod, String... authorities) {
        this.pattern = pattern;
        this.httpMethod = httpMethod;
        this.authorities = new HashSet<String>(Arrays.asList(authorities));
    }
    
	public static List<PathAuthMap> init() {
		return Arrays.asList(
			// 不需要被保护
			new PathAuthMap("/role/**", GET),
			new PathAuthMap("/role/**", POST, "role"),
			new PathAuthMap("/role/**", PUT, "role"),
			new PathAuthMap("/role/**", DELETE, "role"),
			// 任何http方法都需要这个权限
			new PathAuthMap("/user/search", null, "query_all_user"),
			// 需要两个权限之一
			new PathAuthMap("/customer/enabled", POST, "customer", "customer_enabled")
		);
	}
}
