package com.github.emailtohl.integration.common.jwt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * <p>JWT加密解密测试 </p>
 * @author HeLei
 */
public class JwtServiceTest {
    JwtService jwtService;

    @Before
    public void setUp() throws Exception {
        Cache c = new KeyCacheForTest();
        
        CacheManager cm = mock(CacheManager.class);
        when(cm.getCache(JwtService.CACHE_MANAGER_NAME)).thenReturn(c);
        
        jwtService = new JwtService(cm);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        Profile p = new Profile();
        p.setUsername("foo");
        p.getAuthorities().addAll(Arrays.asList("query_all_user"));
        
        String compact = jwtService.encrypt(p);
        
        System.out.println(compact);
        
        Profile p2 = jwtService.decrypted(compact);
        assertEquals(p2, p);
    }


}
