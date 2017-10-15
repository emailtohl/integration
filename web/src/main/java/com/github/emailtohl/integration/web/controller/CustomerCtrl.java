package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.user.dto.UserDto;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.service.CustomerService;
/**
 * 用户管理的控制器
 * @author HeLei
 * @date 2017.02.04
 */
@RestController
@RequestMapping(value = "customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CustomerCtrl {
	@Inject CustomerService customerService;
	
	/**
	 * 根据用户名和公司进行组合查询
	 * @param title
	 * @param affiliation
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "page", method = GET)
	public Paging<Customer> query(@RequestParam(required = false) String name, @RequestParam(required = false) String title, @RequestParam(required = false) String affiliation, 
			@PageableDefault(page = 0, size = 10, sort = {"name", "title", "affiliation"}, direction = Direction.DESC) Pageable pageable) {
		return customerService.query(name, title, affiliation, pageable);
	}
	
	/**
	 * 获取客户详情
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "{id}", method = GET)
	public Customer getCustomer(@PathVariable("id") Long id) {
		return customerService.getCustomer(id);
	}
	
	/**
	 * 修改客户的基本资料
	 * @param id
	 * @param form
	 */
	@RequestMapping(value = "{id}", method = PUT)
	public void update(@PathVariable("id") Long id, @RequestBody UserDto form) {
		customerService.update(id, form.convertCustomer());
	}
	
	/**
	 * 下载报表
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "download", method = GET)
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 设置响应头Content-Disposition，将强制浏览器询问客户是保存还是下载文件，而不是在浏览器中在线打开该文件
		response.setHeader("Content-Disposition", "attachment;filename=" + "customer_" + Instant.now().toString() + ".xls");
		// 设置文件ContentType类型，是通用的，二进制内容类型，这样容器就不会使用字符编码对该数据进行处理（当然更规范的是使用附件真正的MIME内容类型）
		response.setContentType("application/octet-stream");
		OutputStream out = response.getOutputStream();
		Workbook wb = customerService.getCustomerExcel();
		wb.write(out);
		out.close();
	}
	
	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}

}
