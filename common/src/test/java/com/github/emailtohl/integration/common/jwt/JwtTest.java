package com.github.emailtohl.integration.common.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.gson.Gson;

public class JwtTest {
    JwtService jwtService;
    Gson gson = new Gson();
    static final String AUTHORIZATION = "Authorization";
    JwtFilter jwt = new JwtFilter();
    FilterChain chain = mock(FilterChain.class);
    Answer<Object> answer = new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            for (Object arg : invocation.getArguments()) {
                if (arg instanceof HttpServletRequest) {
                    System.out.println(((HttpServletRequest) arg).getRequestURI() + ":" + ((HttpServletRequest) arg).getMethod() + "  executed");
                }
            }
            return invocation.getMock();
        }
    };

    @Before
    public void setUp() throws Exception {
        Cache c = new KeyCacheForTest();

        CacheManager cm = mock(CacheManager.class);
        when(cm.getCache(JwtService.CACHE_MANAGER_NAME)).thenReturn(c);

        jwtService = new JwtService(cm);

        jwt.setJwtService(jwtService);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testJwt() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(DELETE.name(), "/role/batch");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        
        String username = "foo";
        Set<String> authorities = new HashSet<String>(Arrays.asList("role"));
        Profile p = new Profile();
        p.setUsername(username);
        p.setAuthorities(authorities);
        String compact = jwtService.encrypt(p);
        
        request.addHeader(AUTHORIZATION, compact);
        
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String compactJws = response.getHeader(JwtFilter.HEAD_NAME);
        Profile p2 = jwtService.decrypted(compactJws);
        System.out.println(p2);
        assertTrue(p2.getExp() > p.getExp());
        
        System.out.println(response.getHeaderNames());
        System.out.println(response.getHeader(JwtFilter.HEAD_NAME));
    }
    
    /**
     * 测试不需要被保护的路径
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testNoNeedBeProtected() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(GET.name(), "/role/query");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        
        String username = "foo";
        Set<String> authorities = new HashSet<String>(Arrays.asList("customer"));
        Profile p = new Profile();
        p.setUsername(username);
        p.setAuthorities(authorities);
        
        String compact = jwtService.encrypt(p);
        
        request.addHeader(AUTHORIZATION, compact);
        
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String compactJws = response.getHeader(JwtFilter.HEAD_NAME);
        Profile p2 = jwtService.decrypted(compactJws);
        System.out.println(p2);
        assertTrue(p2.getExp() > p.getExp());
    }
    
    /**
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testNoAuth() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(PUT.name(), "/role/grand");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        
        String username = "foo";
        Set<String> authorities = new HashSet<String>(Arrays.asList("customer_enabled"));
        Profile p = new Profile();
        p.setUsername(username);
        p.setAuthorities(authorities);
        
        String compact = jwtService.encrypt(p);
        
        request.addHeader(AUTHORIZATION, compact);
        
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }
    
    /**
     * 测试失效
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testExp() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(GET.name(), "/user/search");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        
        String username = "foo";
        Set<String> authorities = new HashSet<String>(Arrays.asList("query_all_user"));
        Profile p = new Profile();
        p.setUsername(username);
        p.setAuthorities(authorities);
        // 立即失效
        p.setExp(new Date().getTime());
        
        String compact = jwtService.encrypt(p);
        
        request.addHeader(AUTHORIZATION, compact);
        
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    /**
     * 没有JWT的情况
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testNoJwt() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(DELETE.name(), "/role");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        
        request = new MockHttpServletRequest(DELETE.name(), "/role");
        response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        
        request.addHeader(AUTHORIZATION, "");
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
    
    /**
     * 错误的JWT信息
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testErrJwt() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(DELETE.name(), "/role");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(answer).when(chain).doFilter(request, response);
        
        request.addHeader(AUTHORIZATION, "abc");
        jwt.doFilter(request, response, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

}
