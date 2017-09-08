package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.common.jpa.Pager;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.utils.UpDownloader;
import com.github.emailtohl.integration.user.dto.UserDto;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.Role;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.service.UserService;
import com.google.gson.Gson;

/**
 * 用户管理的控制器
 * @author HeLei
 * @date 2017.02.04
 */
@Controller
@RequestMapping(value = "user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserCtrl {
	private static final Logger logger = LogManager.getLogger();
	public static final String ICON_DIR = "icon_dir";
	private UpDownloader upDownloader;
	@Inject @Named("resources") File resourcePath;
	@Inject UserService userService;
	Gson gson = new Gson();
	@Inject LoginCtrl loginCtrl;
	
	@PostConstruct
	public void createIconDir() {
		File f = new File(resourcePath, ICON_DIR);
		if (!f.exists()) {
			f.mkdir();
		}
		upDownloader = new UpDownloader(resourcePath);
	}
	
	/**
	 * 查询user资源下提供哪些方法
	 * @return
	 */
	@RequestMapping(value = "", method = OPTIONS)
	public ResponseEntity<Void> discover() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Allow", "OPTIONS,HEAD,GET");
		return new ResponseEntity<Void>(null, headers, HttpStatus.NO_CONTENT);
	}
	
	/**
	 * 查询user/id下支持哪些方法
	 * @param id
	 * @return
	 * @throws ResourceNotFoundException 
	 */
	@RequestMapping(value = "{id}", method = OPTIONS)
	public ResponseEntity<Void> discover(@PathVariable("id") long id) throws ResourceNotFoundException {
		if (userService.getUser(id) == null)
			throw new ResourceNotFoundException("未找到此资源");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Allow", "OPTIONS,HEAD,GET,PUT,DELETE");
		return new ResponseEntity<Void>(null, headers, HttpStatus.NO_CONTENT);
	}
	
    /**
     * 查询该角色名是否已用
     * @param username
     * @return
     */
    @RequestMapping(value = "exist", method = GET)
    @ResponseBody
    public String exist(@RequestParam(required = false, name = "username", defaultValue = "") String username) {
        if (!StringUtils.hasText(username)) {
            return String.format("{\"exist\":%b}", false);
        }
        return String.format("{\"exist\":%b}", userService.exist(username));
    }
    
	/**
	 * 通过id获取User
	 * 注意，userService获取的User对象，可能是一个普通的User，也可能是继承User的Employ或Manager
	 * 如果控制器返回一个User，由于没有相关的getter方法，Spring MVC调用的序列化方法不会将Employ或Manager中的属性解析
	 * 这里的解决方案是先序列化为JSON，然后以字符串形式返回到前端
	 * @param id
	 * @return
	 * @throws ResourceNotFoundException 
	 */
	@RequestMapping(value = "id/{id}", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public String getUserById(@PathVariable("id") Long id) throws ResourceNotFoundException {
		User u = userService.getUser(id);
		if (u == null) {
			throw new ResourceNotFoundException("未找到此资源");
		}
		return gson.toJson(u);
	}
	
	/**
	 * 通过email获取User
	 * 同getUserById中的注释一样，这里也是以字符串形式返回到前端
	 * 
	 * 这里不用@PathVariable从路径中获取ip地址，因为小数点后面的内容会被截取
	 * 
	 * @param email
	 * @return
	 * @throws ResourceNotFoundException 
	 */
	@RequestMapping(value = "email", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public String getUserByEmail(@RequestParam String email) throws ResourceNotFoundException {
		User u = userService.getUserByEmail(email);
		return gson.toJson(u);
	}
	
	/**
	 * 获取分页对象
	 * 这里返回的对象没有使用org.springframework.data.domain.Page<E>
	 * 主要是因为该对象在Spring MVC中不能序列化并传到前端
	 * @param u
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "pager", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public Pager<User> getUserPager(@ModelAttribute UserDto form, 
			@PageableDefault(page = 0, size = 10, sort = BaseEntity.ID_PROPERTY_NAME, direction = Direction.DESC) Pageable pageable) {
		User u = form.convertUser();
		return userService.getUserPager(u, pageable);
	}
	
	/**
	 * 获取用户权限列表
	 * @param form
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "pageByRoles", method = GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public Pager<User> getPageByRoles(String email, String roles,
			@PageableDefault(page = 0, size = 10, sort = BaseEntity.MODIFY_DATE_PROPERTY_NAME, direction = Direction.DESC) Pageable pageable) {
		Set<String> set = new HashSet<String>();
		if (roles != null && !roles.isEmpty()) {
			for (String role : roles.split(",")) {
				set.add(role);
			}
		}
		return userService.getPageByRoles(email, set, pageable);
	}
	
	/**
	 * 新增一个Employee
	 * 适用于管理员操作，或RestFull风格的调用，受安全策略保护
	 * 若注册页面中新增一个用户，可用/register，POST添加
	 * @param u
	 * @return
	 */
	@RequestMapping(value = "employee", method = POST)
	public ResponseEntity<?> addEmployee(@RequestBody @Valid UserDto form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Employee emp = form.convertEmployee();
		Long id = userService.addEmployee(emp).getId();
		String uri = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/user/{id}")
				.buildAndExpand(id).toString();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", uri);
		return new ResponseEntity<>(emp, headers, HttpStatus.CREATED);
	}
	
	/**
	 * 新增一个Customer
	 * 适用于管理员操作，或RestFull风格的调用，受安全策略保护
	 * 若注册页面中新增一个用户，可用/register，POST添加
	 * @param u
	 * @return
	 */
	@RequestMapping(value = "customer", method = POST)
	public ResponseEntity<?> addCustomer(@RequestBody @Valid UserDto form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Customer cus = form.convertCustomer();
		Long id = userService.addCustomer(cus).getId();
		String uri = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/user/{id}")
				.buildAndExpand(id).toString();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", uri);
		return new ResponseEntity<>(cus, headers, HttpStatus.CREATED);
	}
	
	/**
	 * 激活账户
	 * @param id
	 */
	@RequestMapping(value = "enableUser/{id}", method = PUT)
	@ResponseBody
	public void enableUser(@PathVariable(value = "id") long id) {
		userService.enableUser(id);
	}
	
	/**
	 * 禁用账户，权限在service接口处配置
	 * @param id
	 */
	@RequestMapping(value = "disableUser/{id}", method = PUT)
	@ResponseBody
	public void disableUser(@PathVariable(value = "id") long id) {
		userService.disableUser(id);
	}
	
	/**
	 * 对用户授权，权限识别由service控制
	 * @param id
	 * @param authorities
	 */
	@RequestMapping(value = "grantRoles/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public void grantRoles(@PathVariable @Min(1L) Long id, @RequestBody String[] roles) {
		userService.grantRoles(id, roles);
	}
	
	/**
	 * 修改一个User
	 * @param id
	 * @param user
	 */
	@RequestMapping(value = "employee/{id}", method = PUT)
	public ResponseEntity<Void> updateEmployee(@PathVariable("id") @Min(1L) long id, @Valid @RequestBody UserDto form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
		Employee emp = form.convertEmployee();
		User u = userService.getUser(id);
		userService.mergeEmployee(u.getEmail(), emp);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * 修改一个User
	 * @param id
	 * @param user
	 */
	@RequestMapping(value = "customer/{id}", method = PUT)
	public ResponseEntity<Void> updateCustomer(@PathVariable("id") @Min(1L) long id, @Valid @RequestBody UserDto form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
		Customer cus = form.convertCustomer();
		User u = userService.getUser(id);
		userService.mergeCustomer(u.getEmail(), cus);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * 删除一个User
	 * @param id
	 */
	@RequestMapping(value = "{id}", method = DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") @Min(1L) long id) {
		userService.deleteUser(id);
	}

	/**
	 * 获取所有角色
	 * @return
	 */
	@RequestMapping(value = "role", method = GET)
	@ResponseBody
	public List<Role> getRoles() {
		return userService.getRoles();
	}
	
	/**
	 * 用户上传头像
	 * @param icon
	 * @throws IOException 
	 */
	@RequestMapping(value = "icon", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void uploadIcon(@RequestParam("id") long id, @RequestPart("icon") Part icon) throws IOException {
		LocalDate date = LocalDate.now();
		String relativeDir = ICON_DIR + File.separator + date.getYear() + File.separator + date.getDayOfYear();
		File absoluteDir = new File(upDownloader.getAbsolutePath(relativeDir));
		if (!absoluteDir.exists()) {
			absoluteDir.mkdirs();
		}
		String iconName = relativeDir + File.separator + id + '_' + icon.getSubmittedFileName();

		User u = userService.getUser(id);
		// 删除原有的图片，且同步数据库中的信息
		String iconSrc = u.getIconSrc();
		if (iconSrc != null && !iconSrc.isEmpty()) {
			String baseName = upDownloader.getBaseName();
			if (iconSrc.startsWith(baseName)) {
				iconSrc = iconSrc.substring(baseName.length());
			}
			File exist = new File(upDownloader.getAbsolutePath(iconSrc));
			if (exist.exists()) {
				exist.delete();
			}
		}
		
		String absolutePath = upDownloader.upload(iconName, icon);
		String url = UpDownloader.getRelativeRootURL(absolutePath, resourcePath.getAbsolutePath());
		userService.updateIconSrc(id, url);
		loginCtrl.updateIconSrcMap(u.getEmail(), url);// 同时更新用户头像的缓存信息
		
		// 再保存一份到数据库中
		byte[] b = upDownloader.getFile(iconName);
		userService.updateIcon(id, b);
	}
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
}
