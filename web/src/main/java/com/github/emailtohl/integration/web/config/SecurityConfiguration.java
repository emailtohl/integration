package com.github.emailtohl.integration.web.config;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.core.auth.LoginEvent;
import com.github.emailtohl.integration.core.config.CoreConfiguration;
import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.web.filter.UserPasswordEncryptionFilter;

/**
 * Spring Security配置
 * @author HeLei
 */
@Configuration
//启动安全过滤器
@EnableWebSecurity
@Import({ CoreConfiguration.class })
class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Inject
	DataSource dataSource;
	
	/**
	 * 自定义AuthenticationProvider，可用它来定制如何认证用户
	 */
	@Inject
	@Named("authenticationProvider")
	AuthenticationProvider authenticationProvider;
	
	/**
	 * 自定义认证方式所需要的依赖
	 */
	@Inject
	@Named("userDetailsService")
	UserDetailsService userDetailsService;
	
	/**
	 * 登录成功后发布消息
	 */
	@Inject
	ApplicationEventPublisher publisher;
	
	/**
	 * 为了在应用程序中获取到用户身份信息，将该作为Spring管理的Bean暴露给外界
	 * @return SessionRegistry
	 */
	@Bean
	protected SessionRegistry sessionRegistryImpl() {
		return new SessionRegistryImpl();
	}
	
	/**
	 * 在应用程序中，有时需要访问用户的身份信息，默认的WebSecurityConfigurerAdapter没有暴露AuthenticationManager Bean
	 * 若在应用程序中使用AuthenticationManager，则可以将其注册进spring容器中
	 */
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	/**
	 * 创建认证管理器：AuthenticationManager，它是spring security的核心
	 * 在这里的配置中，可以直接告诉AuthenticationManager如何获取用户名、密码、授权
	 * 也可以自定义一个AuthenticationProvider，然后注册给它
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder builder) throws Exception {
		/* 基于内存的简单配置
		builder.inMemoryAuthentication().withUser("emailtohl@163.com").password("123456").authorities("USER", "ADMIN")
				.and().withUser("foo@test.com").password("123456").authorities("MANAGER")
				.and().withUser("bar@test.com").password("123456").authorities("EMPLOYEE");
		*/

		/* 基于数据库的配置
		builder.jdbcAuthentication().dataSource(dataSource)
				.usersByUsernameQuery("SELECT t.email as username, t.password, t.enabled FROM t_user AS t WHERE t.email = ?")
				.authoritiesByUsernameQuery("SELECT u.email AS username, a.name AS authority FROM t_user u INNER JOIN t_user_role ur ON u.id = ur.user_id INNER JOIN t_role_authority ra ON ur.role_id = ra.role_id INNER JOIN t_authority a ON ra.authority_id = a.id WHERE u.email = ?")
				.passwordEncoder(new BCryptPasswordEncoderProxy());
		 */
		
//		自定义的AuthenticationProvider和UserDetailsService
		builder.authenticationProvider(authenticationProvider).userDetailsService(userDetailsService);
	}
	
	/**
	 * 配置Spring Security的Filter链
	 */
	@Override
	public void configure(WebSecurity security) {
		// 告诉Spring Security需要忽略的路径
		security.ignoring()
		.antMatchers("/main.js")
		.antMatchers("/lib/**")
		.antMatchers("/common/**")
		.antMatchers("/site/**")
		.antMatchers("/resources/**")
		.antMatchers("/download/**")
		.antMatchers("/article/**")
		.antMatchers("/detail/**");
	}
	/**
	 * 配置Http安全访问规则
	 */
	@Override
	protected void configure(HttpSecurity security) throws Exception {
		String[] permitUrl = {
			"/"/* 首页 */,
			"/ping"/* 内部服务器之间的连接 */,
			"/cluster/**"/* 内部服务器之间的连接 */,
			"/websocket/**"/* websocket通信 */,
			"/register"/* 获取注册页面GET以及注册新用户POST */,
			"/forgetPassword"/* 在邮箱中获取忘记密码页面 */,
			"/getUpdatePasswordPage"/* 在邮箱中获取忘记密码页面 */,
			"/updatePassword"/* 修改密码，自己修改自己的密码，是否允许是根据token，而非权限 */,
			"/authentication"/* 获取认证信息 */,
			"/index.html",
			"/home.html",
			"/signup",
			"/about",
			"/login",
			"/logout",
			"/article",
			"/detail",
			"/public/recentArticles",
			"/public/recentComments",
			"/public/classify",
		};
		security
			.authorizeRequests()
				// 跨域请求登录页面时，要发送一个预访问请求：PreflightRequest，让spring security不做拦截
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.antMatchers(permitUrl).permitAll()
				.antMatchers("/rest/**").fullyAuthenticated()
				.antMatchers(HttpMethod.GET, "/audit/role/**").hasAuthority(Authority.AUDIT_ROLE)
				.antMatchers(HttpMethod.GET, "/audit/customer/**", "/audit/employee/**").hasAuthority(Authority.AUDIT_USER)
				.antMatchers(HttpMethod.DELETE, "/cms/**").hasAuthority(Authority.CONTENT)
				.antMatchers(HttpMethod.POST, "/cms/type", "/cms/approveArticle", "/cms/rejectArticle", "/cms/openComment", "/cms/closeComment", "/cms/approvedComment", "/cms/rejectComment").hasAuthority(Authority.CONTENT)
				.antMatchers(HttpMethod.PUT, "/cms/type").hasAuthority(Authority.CONTENT)
				.antMatchers(HttpMethod.GET, "/cms/**").permitAll()
				.antMatchers(HttpMethod.POST, "/customer", "/customer/updatePassword").permitAll()
				.antMatchers(HttpMethod.GET, "/customer/exist", "/customer/token").permitAll()
				.antMatchers(HttpMethod.GET, "/customer/page", "/customer/search", "/customer/cellPhoneOrEmail").hasAuthority(Authority.CUSTOMER)
				.antMatchers(HttpMethod.PUT, "/customer").hasAuthority(Authority.CUSTOMER)
				.antMatchers(HttpMethod.DELETE, "/customer").hasAuthority(Authority.CUSTOMER_DELETE)
				.antMatchers(HttpMethod.POST, "/customer/enabled").hasAuthority(Authority.CUSTOMER_ENABLED)
				.antMatchers(HttpMethod.POST, "/customer/grandRoles").hasAuthority(Authority.CUSTOMER_ROLE)
				.antMatchers(HttpMethod.POST, "/customer/grandLevel").hasAuthority(Authority.CUSTOMER_LEVEL)
				.antMatchers(HttpMethod.POST, "/customer/resetPassword").hasAuthority(Authority.CUSTOMER_RESET_PASSWORD)
				.antMatchers("/customer/**").authenticated()
				.antMatchers(HttpMethod.GET, "/employee/exist").permitAll()
				.antMatchers(HttpMethod.POST, "/employee").hasAuthority(Authority.EMPLOYEE)
				.antMatchers(HttpMethod.PUT, "/employee").hasAuthority(Authority.EMPLOYEE)
				.antMatchers(HttpMethod.DELETE, "/employee").hasAuthority(Authority.EMPLOYEE_DELETE)
				.antMatchers(HttpMethod.POST, "/employee/grandRoles").hasAuthority(Authority.EMPLOYEE_ROLE)
				.antMatchers(HttpMethod.POST, "/employee/resetPassword").hasAuthority(Authority.EMPLOYEE_RESET_PASSWORD)
				.antMatchers(HttpMethod.POST, "/employee/enabled").hasAuthority(Authority.EMPLOYEE_ENABLED)
				.antMatchers("/employee/**").authenticated()
				.antMatchers(HttpMethod.GET, "/resource/**").permitAll()
				.antMatchers(HttpMethod.POST, "/resource/createDir", "/resource/reName", "/resource/delete").hasAuthority(Authority.RESOURCE)
				.antMatchers(HttpMethod.POST, "/resource/writeText").hasAuthority(Authority.CONTENT)
				.antMatchers("/resource/**").authenticated()
				.antMatchers(HttpMethod.GET, "/authority/**").permitAll()
				.antMatchers(HttpMethod.GET, "/role/**").permitAll()
				.antMatchers(HttpMethod.POST, "/role").hasAuthority(Authority.ROLE)
				.antMatchers(HttpMethod.PUT, "/role").hasAuthority(Authority.ROLE)
				.antMatchers(HttpMethod.DELETE, "/role").hasAuthority(Authority.ROLE)
				.antMatchers("/encryption/**").authenticated()
				.antMatchers("/secure").fullyAuthenticated()
				.anyRequest().authenticated()
			// 登录配置
			.and().addFilterBefore(new CORSFilter(), ChannelProcessingFilter.class)
			.formLogin()
				.loginPage("/login").loginProcessingUrl("/login").failureUrl("/login?error")
				.successHandler((request, response, authentication) -> {
					publisher.publishEvent(new LoginEvent(authentication));
					response.sendRedirect(request.getContextPath());
				})
				.usernameParameter("cellPhoneOrEmail").passwordParameter("password").permitAll()
			// 登出配置，注意：Spring security在启动CSRF时，默认只使用HTTP POST，这是为了确保注销需要CSRF令牌和恶意用户不能强行注销你的用户
			// 如果要使用<a>标签链接，get等方式退出，则必须更新下面的Java配置：logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.and().logout()
				.logoutUrl("/logout")
				/*.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))*/
				.logoutSuccessHandler((request, response, authentication) -> {
//					publisher.publishEvent(new LogoutEvent(authentication == null ? "anonymousUser" : authentication.getName()));
				})
				.logoutSuccessUrl("/login?loggedOut")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID").permitAll()
			// session管理，例如登录后切换sessionid，只允许一个人一处登录，控制用户同时登录等功能
			.and().sessionManagement()
			// 要启用并发控制,还必须配置一个特殊的Spring Security HttpListener发布HttpSession-related事件
			// 这允许Spring Security注册表建立一个会话它可以用来检测并发会话，见SecurityBootstrap.java
			// maxSessionsPreventsLogin设置为true时，不允许用户在第二个地方同时登录，默认为false：如果用户在第二个地方登录则将前一会话置为失效
			// 请注意,如果maxSessionsPreventsLogin设置为true,因异常导致浏览器更换sessionId后，则该用户将在会话到期前无法再次登录
				.sessionFixation().changeSessionId().maximumSessions(1)/*.maxSessionsPreventsLogin(true)*/
				.sessionRegistry(sessionRegistryImpl())
			.and().and().csrf()/*.disable()*/.ignoringAntMatchers("/cms/comment")
			.csrfTokenRepository(csrfTokenRepository())
			.and().addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
			// rememberMe默认的过期时间是2周，这里设为四周；默认的私钥名是SpringSecured，这里设为"integration-web"
			.rememberMe().tokenValiditySeconds(2419200).key("integration-web")
			//认证不通过后的处理
			.and().exceptionHandling()
			.authenticationEntryPoint((HttpServletRequest request, HttpServletResponse response,
					AuthenticationException authException) -> {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.addHeader("statusText", authException.getMessage());
			})
			.accessDeniedHandler((HttpServletRequest request,
					HttpServletResponse response, AccessDeniedException accessDeniedException) -> {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.addHeader("statusText", accessDeniedException.getMessage());
			})
			;
		
	}
	
	/**
	 * 另外对于CSRF中还需要做的一件事是告诉Spring security期望返回CSRF令牌的头名叫做“X-XRSF-TOKEN”而不是默认的“X-CSRF-TOKEN”
	 * @return
	 */
	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}
	
	/**
	 * 为了在应用层而非过滤器中使用spring security，还可以启动@EnableGlobalMethodSecurity功能
	 * 这时候spring会在调用Bean方法时再添加一个切面，执行spring security的安全检查
	 * 由于@EnableGlobalMethodSecurity是注解在class上的，而本class已经继承了WebSecurityConfigurerAdapter，所以只能新建一个配置类
	 * AuthorizationConfiguration作为静态的，带@Configuration的内部类，可以被spring识别，并导入到本配置类中
	 */
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true, order = 0, mode = AdviceMode.PROXY, proxyTargetClass = false)
	static class AuthorizationConfiguration extends GlobalMethodSecurityConfiguration {
	}
}


/**
 * 在BCryptPasswordEncoder上生成代理，用于解密用户密码
 * @author HeLei
 */
class BCryptPasswordEncoderProxy extends BCryptPasswordEncoder {
	private final transient Logger logger = LogManager.getLogger();
	Encipher encipher = new Encipher();
	
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		String userPassword = rawPassword.toString();
		HttpSession httpSession = UserPasswordEncryptionFilter.CONCURRENT_SESSION.get();
		if (httpSession != null) {
			String privateKey = (String) httpSession.getAttribute(UserPasswordEncryptionFilter.PRIVATE_KEY_PROPERTY_NAME);
			try {
				userPassword = encipher.decrypt(userPassword, privateKey);
			} catch (Exception e) {
				logger.info("前端使用的加密密码不正确", e);
			}
		}
		return super.matches(userPassword, encodedPassword);
	}
}


/**
 * 这是为ajax提供csrf认证的解决方案
 * 
 * 首先看看基于表单方式是如何提供csrf令牌的，在表单中只需添加一个含有令牌的隐藏域即可，该令牌服务器动态页面提供的
 * 例如JSP中通过EL表达式：name="${_csrf.parameterName}" value="${_csrf.token}"获取令牌
 * 现在考虑如REST风格的客户程序，如Angular前端，它不能在每次提交数据时向服务器要一个令牌
 * 实际上Spring Security除了在表单中查询csrf令牌外，还可以在http请求头中查询cookie看是否含有csrf令牌
 * 所以可以配置一个过滤器，在每次返回到客户端数据时，在header的cookie中携带csrf令牌，等客户端下次访问时可以带上该令牌
 * OncePerRequestFilter就是这样的过滤器，它可以将CSRF令牌保存在cookie中
 * 注意：OncePerRequestFilter需要在Spring security过滤链之后，这样才能访问得到CsrfToken
 */
class CsrfHeaderFilter extends OncePerRequestFilter {
	private String csrfTokenName = CsrfToken.class.getName();
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		CsrfToken csrf = (CsrfToken) request.getAttribute(csrfTokenName);
		if (csrf != null) {
			request.getSession().setAttribute(csrfTokenName, csrf);
			Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
			String token = csrf.getToken();
			if (cookie == null || token != null && !token.equals(cookie.getValue())) {
				cookie = new Cookie("XSRF-TOKEN", token);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}
		filterChain.doFilter(request, response);
	}
}

/**
 * 有时候需要支持跨域访问，例如登录页面，所以需要设置HTTP响应头
 * 
 * 注意：浏览器在默认情况下通过CORS这样的方式是不会传递cookie，一般强制性将cookie添加到header的做法,也会被浏览器拒绝并报错。
 * angular：
 * $http.post(url, {withCredentials: true, ...})
 * 或者
 * http({withCredentials: true, ...}).post(...)
 * 或者
 * .config(function ($httpProvider) {
 * 		$httpProvider.defaults.withCredentials = true;
 * })
 * 
 * jQuery：
 * $.ajax("www.cros.com/api/data", {
 * 		type: "GET",
 * 		xhrFields: {
 * 			withCredentials: true
 * 		},
 * 		crossDomain: true,
 * 		success: function(data, status, xhr) {
 * 
 * 		}
 * })
 * 
 * 不过这是全局设置不推荐，最好使用Spring提供的@CrossOrigin注解指定支持跨域访问的接口
 */
class CORSFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		if (req.getHeader("Origin") == null || "null".equalsIgnoreCase(req.getHeader("Origin"))) {
			resp.setHeader("Access-Control-Allow-Origin", "*");// Access-Control-Allow-Origin: <origin> | * // 授权的源控制  
			resp.setHeader("Access-Control-Max-Age", "1000");// Access-Control-Max-Age: <delta-seconds> // 用来指定本次预检（preflight）请求的有效期，单位为秒
			resp.setHeader("Access-Control-Allow-Credentials", "true");// Access-Control-Allow-Credentials: true | false // 控制是否开启与Ajax的Cookie提交方式，如果用到Session必须要打开，AJAX方面需要做这样的设置：var xhr = new XMLHttpRequest(); xhr.withCredentials = true; 需要注意的是，如果要发送Cookie，Access-Control-Allow-Origin就不能设为星号，必须指定明确的、与请求网页一致的域名
			resp.setHeader("Access-Control-Allow-Methods", "HEAD,GET,POST,OPTIONS");// Access-Control-Allow-Methods: <method>[, <method>]* // 允许请求的HTTP Method
			String reqHead = resp.getHeader("Access-Control-Request-Headers");// CORS请求时，XMLHttpRequest对象的getResponseHeader()方法只能拿到6个基本字段：Cache-Control、Content-Language、Content-Type、Expires、Last-Modified、Pragma。如果想拿到其他字段，就必须在Access-Control-Expose-Headers里面指定
            if (StringUtils.hasText(reqHead)) {
            	resp.addHeader("Access-Control-Allow-Headers", reqHead);
            }
		}
		chain.doFilter(request, resp);
	}

	@Override
	public void destroy() {
	}
	
}

