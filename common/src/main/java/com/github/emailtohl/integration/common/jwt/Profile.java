package com.github.emailtohl.integration.common.jwt;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>登录后的用户信息</p>
 * 
 * @author HeLei
 */
public class Profile {
    /**
     * 失效时间
     */
    public static final long EXP = 1000 * 60 * 20;
    /**
     * 用户名
     */
    private String username;
    /**
     * 权限
     */
    private Set<String> authorities = new HashSet<String>();
    /**
     * 失效时间
     */
    private long exp = new Date().getTime() + EXP;

    /**
     * @param username
     * @param authorities
     */
    public Profile(String username, Set<String> authorities) {
        this.username = username;
        this.authorities = authorities;
    }

    /**
     * 构造
     */
    public Profile() {}

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the authorities
     */
    public Set<String> getAuthorities() {
        return authorities;
    }

    /**
     * @param authorities the authorities to set
     */
    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    /**
     * @return the exp
     */
    public long getExp() {
        return exp;
    }
    /**
     * @param exp the exp to set
     */
    public void setExp(long exp) {
        this.exp = exp;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Profile [username=" + username + ", authorities=" + authorities + ", exp=" + exp + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Profile other = (Profile) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }
    
}
