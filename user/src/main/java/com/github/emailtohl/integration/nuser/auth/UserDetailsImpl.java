package com.github.emailtohl.integration.nuser.auth;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.emailtohl.integration.common.exception.InnerDataStateException;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * org.springframework.security.core.userdetails.UserDetailsService接口需要返回的实例
 * @author HeLei
 */
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1635134127318665555L;

	/**
	 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
	 */
	private Long id;
	private String realName;
	private UserType userType;
	private String nickname;
	private String cellPhone;
	private String email;
	private Integer empNum; // 跟平台账号相关
	private Customer.Level level; // 跟客户账号相关
	private Set<String> authorities;
	private String password;
	private String iconUrl;// 额外携带用户图片信息
	private Boolean accountNonLocked;
	private Boolean accountNonExpired;
	private Boolean credentialsNonExpired;
	
	public UserDetailsImpl() {
	}

	public UserDetailsImpl(Long id, String realName, UserType userType, String nickname, String cellPhone, String email,
			Integer empNum, Set<String> authorities, String password, String iconSrc, Boolean accountNonLocked,
			Boolean accountNonExpired, Boolean credentialsNonExpired) {
		super();
		this.id = id;
		this.realName = realName;
		this.userType = userType;
		this.nickname = nickname;
		this.cellPhone = cellPhone;
		this.email = email;
		this.empNum = empNum;
		this.authorities = authorities;
		this.password = password;
		this.iconUrl = iconSrc;
		this.accountNonLocked = accountNonLocked;
		this.accountNonExpired = accountNonExpired;
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public UserDetailsImpl(User u) {
		this.id = u.getId();
		this.realName = u.getName();
		this.nickname = u.getNickname();
		this.cellPhone = u.getCellPhone();
		this.email = u.getEmail();
		if (u instanceof Employee) {
			this.userType = UserType.Employee;
			this.empNum = ((Employee) u).getEmpNum();
		} else if (u instanceof Customer) {
			this.userType = UserType.Customer;
			this.level = ((Customer) u).getLevel();
		}
		this.authorities = u.authorityNames();
		this.password = u.getPassword();
		if (u.getImage() != null) {
			this.iconUrl = u.getImage().getUrl();
		}
		this.accountNonExpired = u.getAccountNonExpired();
		this.accountNonLocked = u.getAccountNonLocked();
		this.credentialsNonExpired = u.getCredentialsNonExpired();
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList(this.authorities.toArray(new String[this.authorities.size()]));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		if (userType == UserType.Employee) {
			return empNum.toString();
		}
		if (cellPhone != null) {
			return cellPhone;
		}
		if (email != null) {
			return email;
		}
		throw new InnerDataStateException("未存储用户的标识");
	}
	
	public Long getId() {
		return this.id;
	}

	public String getIconUrl() {
		return this.iconUrl;
	}
	
	public String getRealName() {
		return this.realName;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired == null ? false : accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked == null ? false : accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired == null ? false : credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return accountNonLocked == null ? false : accountNonLocked;
	}

	// 认证的时候存储密码，用过之后会擦除
	public void eraseCredentials() {
		this.password = null;
	}

	@Override
	public String toString() {
		return "UserDetailsImpl [id=" + id + ", userType=" + userType + ", nickname=" + nickname + ", cellPhone="
				+ cellPhone + ", email=" + email + ", empNum=" + empNum + ", level=" + level + ", authorities="
				+ authorities + ", password=" + password + ", iconSrc=" + iconUrl + ", accountNonLocked="
				+ accountNonLocked + ", accountNonExpired=" + accountNonExpired + ", credentialsNonExpired="
				+ credentialsNonExpired + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	/**
	 * 从数据库中查询出来的用户身份，以id为相等标识
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDetailsImpl other = (UserDetailsImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
