package com.github.emailtohl.integration.common.jwt;

import java.security.Key;
import java.util.concurrent.Callable;

import org.springframework.cache.Cache;

/**
 * <p>测试所用</p>
 * @author HeLei
 */
public class KeyCacheForTest implements Cache {
    /**
     * key
     */
    private Key key;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        return (T) this.key;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        this.key = (Key) value;
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return null;
    }

    @Override
    public void evict(Object key) {
        
    }

    @Override
    public void clear() {
        
    }
    
}
