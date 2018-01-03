package com.github.emailtohl.integration.web.service.cms;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.UserRef;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 文章服务实现
 * @author HeLei
 */
@Service
@Transactional
public class ArticleServiceImpl extends StandardService<Article>{
	ArticleRepository articleRepository;
	CommentRepository commentRepository;
	TypeRepository typeRepository;
	UserService userService;
	CustomerService customerService;
	
	private static final Pattern IMG_PATTERN = Pattern.compile("<img\\b[^<>]*?\\bsrc[\\s\\t\\r\\n]*=[\\s\\t\\r\\n]*[\"\"']?[\\s\\t\\r\\n]*(?<imgUrl>[^\\s\\t\\r\\n\"\"'<>]*)[^<>]*?/?[\\s\\t\\r\\n]*>");
	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "article_cache";

	public ArticleServiceImpl(ArticleRepository articleRepository, CommentRepository commentRepository,
			TypeRepository typeRepository, UserService userService, CustomerService customerService) {
		super();
		this.articleRepository = articleRepository;
		this.commentRepository = commentRepository;
		this.typeRepository = typeRepository;
		this.userService = userService;
		this.customerService = customerService;
	}

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Article create(Article entity) {
		// 实际上这里就可以校验body与title是必填项
		validate(entity);
		if (entity == null) {
			return null;
		}
		String body = entity.getBody();
		// 若没有设置封面，则将第一幅图作为封面
		if (!hasText(entity.getCover()) && hasText(body)) {
			Matcher m = IMG_PATTERN.matcher(entity.getBody());
			if (m.find()) {
				entity.setCover(m.group(1));
			}
		}
		// 若没有摘要，则选取前50字
		String summary = entity.getSummary();
		if (!hasText(summary) && hasText(body)) {
			int size = body.length();
			if (size > 50) {
				summary = body.substring(0, 50) + "……";
			} else {
				summary = body;
			}
			entity.setSummary(summary.replaceAll(IMG_PATTERN.pattern(), ""));
		}
		if (entity.getAuthor() != null && entity.getAuthor().getId() != null) {
			UserRef author = userService.getRef(entity.getAuthor().getId());
			entity.setAuthor(author);
		}
		if (entity.getType() != null && hasText(entity.getType().getName())) {
			Type type = typeRepository.findByName(entity.getType().getName());
			if (type != null) {
				entity.setType(type);
				type.getArticles().add(entity);
			}
		}
		return toTransient(articleRepository.save(entity));
	}

	@Override
	public boolean exist(Object matcherValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article get(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Paging<Article> query(Article params, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Article> query(Article params) {
		// TODO Auto-generated method stub
		return null;
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Article update(Long id, Article newEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 对于文章来说，只需要展示用户名字，头像等基本信息即可
	 * 注意：本方法将所有评论载入，而不管该评论是否允许开放
	 */
	@Override
	protected Article toTransient(Article source) {
		if (source == null) {
			return null;
		}
		Article target = new Article();
		BeanUtils.copyProperties(source, target, "author", "approver", "type", "comments");
		// 只获取作者必要信息
		target.setAuthor(transientUserRef(source.getAuthor()));
		target.setApprover(transientEmployeeRef(source.getApprover()));
		// 只获取类型一级父目录
		target.setType(getType(source.getType()));
		// 改变评论懒加载状态，且避免article与comment的交叉引用
		target.setComments(transientComments(source.getComments()));
		return target;
	}

	@Override
	protected Article transientDetail(Article entity) {
		// TODO Auto-generated method stub
		return null;
	}

	protected UserRef transientUserRef(UserRef source) {
		if (source == null) {
			return null;
		}
		UserRef target = new UserRef();
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setCellPhone(source.getCellPhone());
		target.setName(source.getName());
		target.setNickname(source.getNickname());
		target.setIconSrc(source.getIconSrc());
		return target;
	}
	
	protected EmployeeRef transientEmployeeRef(EmployeeRef source) {
		if (source == null) {
			return null;
		}
		EmployeeRef target = new EmployeeRef();
		target.setEmpNum(source.getEmpNum());
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setCellPhone(source.getCellPhone());
		target.setName(source.getName());
		target.setNickname(source.getNickname());
		target.setIconSrc(source.getIconSrc());
		return target;
	}
	
	/**
	 * 过滤文章类型
	 * 
	 * @param source
	 * @return
	 */
	protected Type getType(Type source) {
		if (source == null)
			return null;
		Type target = new Type();
		target.setId(source.getId());
		target.setCreateDate(source.getCreateDate());
		target.setModifyDate(source.getModifyDate());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setParent(getType(source.getParent()));
		// 只要文章长度，不加载具体的文章
		int size = source.getArticles().size();
		for (int i = 0; i < size; i++) {
			target.getArticles().add(new Article());
		}
		return target;
	}
	
	protected List<Comment> transientComments(List<Comment> comments) {
		if (comments == null) {
			return null;
		}
		return comments.stream().filter(c -> c == null || c.isApproved()).map(source -> {
			Comment target = new Comment();
			target.setId(source.getId());
			target.setCreateDate(source.getCreateDate());
			target.setModifyDate(source.getModifyDate());
			target.setContent(source.getContent());
			target.setReviewer(transientUserRef(source.getReviewer()));
			target.setApprover(transientEmployeeRef(source.getApprover()));
			target.setApproved(source.getIsApproved());
			return target;
		}).collect(Collectors.toList());
	}
}
