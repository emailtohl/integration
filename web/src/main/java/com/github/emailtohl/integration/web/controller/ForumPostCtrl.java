package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.jpa.Pager;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.utils.UpDownloader;
import com.github.emailtohl.integration.conference.dto.ForumPostDto;
import com.github.emailtohl.integration.conference.service.ForumPostService;
import com.github.emailtohl.integration.web.filter.PreSecurityLoggingFilter;
/**
 * 论坛控制器
 * @author HeLei
 * @date 2017.02.04
 */
@RestController
@RequestMapping("forum")
public class ForumPostCtrl {
	private static final Logger logger = LogManager.getLogger();
	public static final String IMAGE_DIR = "image_dir";
	private UpDownloader upDownloader;
	@Inject ForumPostService forumPostService;
	@Inject @Named("resources") File resources;
	
	@PostConstruct
	public void createIconDir() {
		File f = new File(resources, IMAGE_DIR);
		if (!f.exists()) {
			f.mkdir();
		}
		upDownloader = new UpDownloader(f);
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST)
	public void add(@RequestBody @Valid ForumPostDto form, Errors e) {
		if (e.hasErrors()) {
			for (ObjectError oe : e.getAllErrors()) {
				logger.info(oe);
			}
			return;
		}
		this.forumPostService.save(CtrlUtil.getCurrentUsername(), form.getTitle(), form.getKeywords(), form.getBody());
	}
	
	@RequestMapping(value = "search", method = RequestMethod.GET)
	public Pager<ForumPostDto> search(@RequestParam String query,
			@PageableDefault(page = 0, size = 5, sort = BaseEntity.CREATE_DATE_PROPERTY_NAME, direction = Direction.DESC) Pageable pageable) {
//		演示时使用完全查询，因为Hibernate Search FullTextQuery getResultSize never matches getResultList().size()
//		return this.forumPostService.find(query, pageable);
		return this.forumPostService.findAllAndPaging(query, pageable);
	}

	@RequestMapping(value = "pager", method = RequestMethod.GET)
	Pager<ForumPostDto> searchPager(@PageableDefault(page = 0, size = 5, sort = BaseEntity.CREATE_DATE_PROPERTY_NAME, direction = Direction.DESC) Pageable pageable) {
		return this.forumPostService.getPager(pageable);
	}
	
	/**
	 * 特殊情况，管理员删除论坛帖子
	 * @param id
	 */
	@RequestMapping(value = "{id}", method = DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") @Min(1L) long id) {
		this.forumPostService.delete(id);
	}
	
	/**
	 * 上传图片,针对前端CKEditor接口编写的控制器方法
	 * @param image
	 * @return 返回一个CKEditor识别的回调函数
	 * @throws IOException
	 */
	@RequestMapping(value = "image", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void uploadImage(@RequestParam("CKEditorFuncNum") String CKEditorFuncNum/* 回调显示图片的位置 */, 
			@RequestPart("upload") Part image
			, HttpServletResponse response) throws IOException {
		// 先确定存储的相对路径
		String dir = getDir();
		String absolutePath = null;
		try (InputStream in = image.getInputStream()) {
			String imageName = dir + File.separator + getFilename(image);
			absolutePath = upDownloader.upload(imageName, in);
		} catch (Exception e) {
			logger.warn("上传失败，可能是文件名后缀不对，或是IO异常", e);
		}
		String html;
		if (absolutePath == null) {
			// 第三个参数为空表示没有错误，不为空则会弹出一个对话框显示　error　message　的内容
			html = "<script type=\"text/javascript\">window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'','上传的文件名冲突');</script>";
		} else {
			String url = absolutePath = UpDownloader.getRelativeRootURL(absolutePath, resources.getAbsolutePath());
			html = "<script type=\"text/javascript\">window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'" + url + "','');</script>";
		}
		response.addHeader("X-Frame-OPTIONS", "SAMEORIGIN");
		response.setContentType("text/html; charset=utf-8");  
        PrintWriter out = response.getWriter();
        out.println(html);
        out.close();
	}
	
	private String getDir() {
		LocalDate date = LocalDate.now();
		String dir = date.getYear() + File.separator + date.getDayOfYear();
		File fdir = new File(upDownloader.getAbsolutePath(dir));
		if (!fdir.exists()) {
			fdir.mkdirs();
		}
		return dir;
	}
	
	/**
	 * 由于是论坛上显示的名字供机器识别，再加上部署集群需要唯一性防冲突，故使用UUID
	 * @return
	 */
	private String getFilename(Part image) {
		String filename = image.getSubmittedFileName();
		int i = filename.lastIndexOf(".");
		String suffix = filename.substring(i);
		return ThreadContext.get(PreSecurityLoggingFilter.ID_PROPERTY_NAME) + suffix;
	}
	
	public void setForumPostService(ForumPostService forumPostService) {
		this.forumPostService = forumPostService;
	}

	public void setUpDownloader(UpDownloader upDownloader) {
		this.upDownloader = upDownloader;
	}
	
}
