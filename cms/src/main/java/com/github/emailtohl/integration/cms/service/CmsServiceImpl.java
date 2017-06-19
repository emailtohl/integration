package com.github.emailtohl.integration.cms.service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.cms.dao.ArticleRepository;
import com.github.emailtohl.integration.cms.dao.CommentRepository;
import com.github.emailtohl.integration.cms.dao.TypeRepository;
import com.github.emailtohl.integration.cms.entities.Article;
import com.github.emailtohl.integration.cms.entities.Comment;
import com.github.emailtohl.integration.cms.entities.Type;
import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.common.jpa.Pager;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.user.dao.UserRepository;
import com.github.emailtohl.integration.user.entities.User;

/**
 * cms的服务层实现
 * 
 * @author HeLei
 * @data 2017.02.15
 */
@Service
public class CmsServiceImpl implements CmsService {
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern IMG_PATTERN = Pattern.compile("<img\\b[^<>]*?\\bsrc[\\s\\t\\r\\n]*=[\\s\\t\\r\\n]*[\"\"']?[\\s\\t\\r\\n]*(?<imgUrl>[^\\s\\t\\r\\n\"\"'<>]*)[^<>]*?/?[\\s\\t\\r\\n]*>");
	
	@Inject
	TypeRepository typeRepository;
	@Inject
	ArticleRepository articleRepository;
	@Inject
	CommentRepository commentRepository;
	@Inject
	UserRepository userRepository;

	@Override
	public Article getArticle(long id) throws ResourceNotFoundException {
		return articlefilter(articleRepository.findOne(id));
	}

	@Override
	public Pager<Article> searchArticles(String query, Pageable pageable) {
		Page<Article> page;
		if (StringUtils.hasText(query)) {
			page = articleRepository.find(query.trim(), pageable);
		} else {
			page = articleRepository.findAll(pageable);
		}
		List<Article> ls = page.getContent().stream().map(this::articlefilter).collect(Collectors.toList());
		return new Pager<>(ls, page.getTotalElements(), page.getNumber(), page.getSize());
	}

	@Override
	public Article saveArticle(String email, Article article, String typeName) {
		Article a = new Article();
		a.setTitle(article.getTitle());
		a.setKeywords(article.getKeywords());
		String body = article.getBody(), summary = article.getSummary();
		a.setBody(body);
		// 将第一幅图作为封面
		Matcher m = IMG_PATTERN.matcher(body);
		if (m.find()) {
			a.setCover(m.group(1));
		}
		// 若没有摘要，则选取前50字
		if (!StringUtils.hasText(summary)) {
			int size = body.length();
			if (size > 50) {
				summary = body.substring(0, 50) + "……";
			} else {
				summary = body;
			}
			a.setSummary(summary.replaceAll(IMG_PATTERN.pattern(), ""));
		}
		User author = userRepository.findByEmail(email);
		a.setAuthor(author);
		String tn = typeName;
		if (!StringUtils.hasText(tn) && article.getType() != null) {
			tn = article.getType().getName();
		}
		if (StringUtils.hasText(tn)) {
			Type t = typeRepository.findByName(tn);
			if (t != null) {
				a.setType(t);
				t.getArticles().add(a);
			}
		}
		articleRepository.save(a);
		return articlefilter(a);
	}

	@Override
	public Article updateArticle(long id, Article article) {
		Article pa = articleRepository.findOne(id);
		if (pa != null) {
			BeanUtils.copyProperties(article, pa, BaseEntity.getIgnoreProperties("author", "type", "cover"));
			Matcher m = IMG_PATTERN.matcher(article.getBody());
			if (m.find()) {
				pa.setCover(m.group(1));
			} else {
				pa.setCover(null);
			}
		}
		Type t = article.getType();
		if (t != null) {
			if (pa.getType() != null) {
				pa.getType().getArticles().remove(pa);
			}
			Type pt = typeRepository.findByName(t.getName());
			pa.setType(pt);
			pt.getArticles().add(pa);
		}
		return articlefilter(pa);
	}

	@Override
	public Article updateArticle(long id, String title, String keywords, String body, String summary, String type) {
		Article article = new Article();
		if (StringUtils.hasText(title))
			article.setTitle(title);
		if (StringUtils.hasText(keywords))
			article.setKeywords(keywords);
		if (StringUtils.hasText(body))
			article.setBody(body);
		if (StringUtils.hasText(summary))
			article.setSummary(summary);
		if (StringUtils.hasText(type)) {
			Type t = typeRepository.findByName(type);
			if (t != null) {
				article.setType(t);
			}
		}
		return updateArticle(id, article);
	}

	@Override
	public void deleteArticle(long id) {
		Article a = articleRepository.findOne(id);
		Type t = a.getType();
		if (t != null) {
			t.getArticles().remove(a);
		}
		articleRepository.delete(a);
	}

	@Override
	public Article approveArticle(long articleId) {
		Article a = articleRepository.findOne(articleId);
		a.setApproved(true);
		return articlefilter(a);
	}

	@Override
	public Article rejectArticle(long articleId) {
		Article a = articleRepository.findOne(articleId);
		a.setApproved(false);
		return articlefilter(a);
	}

	@Override
	public Article openComment(long articleId) {
		Article a = articleRepository.findOne(articleId);
		a.setComment(true);
		return articlefilter(a);
	}

	@Override
	public Article closeComment(long articleId) {
		Article a = articleRepository.findOne(articleId);
		a.setComment(false);
		return articlefilter(a);
	}

	@Override
	public Pager<Comment> queryComments(String query, Pageable pageable) {
		Page<Comment> page;
		if (StringUtils.hasText(query))
			page = commentRepository.find(query, pageable);
		else
			page = commentRepository.findAll(pageable);
		List<Comment> ls = page.getContent().stream().map(c -> {
			Comment t = new Comment();
			BeanUtils.copyProperties(c, t, "article");
			t.setArticle(articlefilter(c.getArticle()));
			return t;
		}).collect(Collectors.toList());
		return new Pager<Comment>(ls, page.getTotalElements(), page.getNumber(), page.getSize());
	}
	
	@Override
	public Comment findComment(long id) {
		Comment c = commentRepository.findOne(id);
		Comment t = new Comment();
		BeanUtils.copyProperties(c, t, "article");
		t.setArticle(articlefilter(c.getArticle()));
		return t;
	}

	@Override
	public Article saveComment(String email, long articleId, String content) {
		Article article = articleRepository.findOne(articleId);
		if (article == null) {
			throw new IllegalArgumentException("没有此文章");
		}
		String critics = "匿名", icon = null;
		if (StringUtils.hasText(email)) {
			User u = userRepository.findByEmail(email);
			if (u != null) {
				critics = u.getUsername();
				icon = u.getIconSrc();
			}
		}
		Comment c = new Comment();
		c.setCritics(critics);
		c.setIcon(icon);
		c.setContent(content);
//		c.setApproved(false);
		c.setArticle(article);
		article.getComments().add(c);
		commentRepository.save(c);
		return articlefilter(article);
	}

	@Override
	public Article updateComment(String email, long id, String commentContent) {
		Comment c = commentRepository.findOne(id);
		if (!StringUtils.hasText(email) || !email.equals(c.getCritics())) {
			throw new AccessDeniedException("不是评论用户");
		}
		Article a = c.getArticle();
		a.getComments().parallelStream().forEach(comment -> {
			if (comment.equals(c)) {
				comment.setContent(commentContent);
			}
		});
		c.setContent(commentContent);
		return articlefilter(a);
	}

	@Override
	public Article deleteComment(long id) {
		Comment c = commentRepository.findOne(id);
		Article a = c.getArticle();
		a.getComments().remove(c);
		commentRepository.delete(c);
		return articlefilter(a);
	}

	@Override
	public Article approvedComment(long commentId) {
		Comment c = commentRepository.findOne(commentId);
		c.setApproved(true);
		return articlefilter(c.getArticle());
	}

	@Override
	public Article rejectComment(long commentId) {
		Comment c = commentRepository.findOne(commentId);
		c.setApproved(false);
		return articlefilter(c.getArticle());
	}

	@Override
	public Pager<Type> getTypePager(String typeName, Pageable pageable) {
		Page<Type> page;
		if (StringUtils.hasText(typeName))
			page = typeRepository.findByNameLike(typeName.trim() + "%", pageable);
		else
			page = typeRepository.findAll(pageable);
		return new Pager<>(page.getContent().stream().map(this::typeFilter).collect(Collectors.toList()),
				page.getTotalElements(), page.getNumber(), page.getSize());
	}

	@Override
	public Type findTypeById(long id) {
		Type p = typeRepository.findOne(id);
		return typeFilter(p);
	}

	@Override
	public Type findTypeByName(String name) {
		Type p = typeRepository.findByName(name);
		return typeFilter(p);
	}

	@Override
	public long saveType(String name, String description, String parent) {
		Type t = new Type();
		t.setName(name);
		t.setDescription(description);
		if (StringUtils.hasText(parent)) {
			Type p = typeRepository.findByName(parent);
			if (p != null) {
				t.setParent(p);
			}
		}
		typeRepository.save(t);
		return t.getId();
	}

	@Override
	public void updateType(long id, String name, String description, String parent) {
		Type pt = typeRepository.findOne(id);
		if (pt == null)
			return;
		if (StringUtils.hasText(name))
			pt.setName(name);
		if (StringUtils.hasText(description))
			pt.setDescription(description);
		if (StringUtils.hasText(parent)) {
			Type pa = typeRepository.findByName(parent);
			if (pa != null) {
				pt.setParent(pa);
			}
		}
	}

	@Override
	public void deleteType(long id) {
		Type t = typeRepository.findOne(id);
		if (t == null)
			return;
		t.getArticles().forEach(a -> {
			a.setType(null);
		});
		typeRepository.delete(t);
	}

	@Override
	public List<Article> recentArticles() {
		return articleRepository.findAll().stream().limit(10).filter(pa -> pa.isApproved()).map(this::articlefilter)
				.peek(this::filterCommentOfArticle).collect(Collectors.toList());
	}

	@Override
	public Article readArticle(long id) {
		Article ta = articlefilter(articleRepository.findOne(id));
		if (ta != null) {
			if (ta.isApproved())
				filterCommentOfArticle(ta);
			else
				ta = null;
		}
		return ta;
	}
	
	@Override
	public List<Comment> recentComments() {
		return commentRepository.findAll().stream().limit(10).filter(pc -> pc.isApproved())
				.collect(Collectors.toList());
	}

	@Override
	public long commentCount(Long articleId) {
		return commentRepository.countByArticleId(articleId);
	}
	
	@Override
	public List<Type> getTypes() {
		return typeRepository.findAll().stream().map(this::typeFilter).collect(Collectors.toList());
	}

	@Override
	public Map<Type, List<Article>> classify() {
		return articleRepository.findAll().stream().limit(100).filter(a -> a.isApproved()).map(this::articlefilter)
				.peek(this::filterCommentOfArticle).collect(Collectors.groupingBy(article -> {
					Type t = article.getType();
					if (t == null) {
						t = new Type();
						t.setName("未分类");
						t.setDescription("系统不存在的分类");
					}
					return t;
				}));
	}

	/**
	 * 对于文章来说，只需要展示用户名字，头像等基本信息即可
	 * 注意：本方法将所有评论载入，而不管该评论是否允许开放
	 * 
	 * @param pa
	 * @return
	 */
	private Article articlefilter(Article pa) {
		if (pa == null)
			return null;
		User tu = new User();
		User pu = pa.getAuthor();
		tu.setId(pu.getId());
		tu.setEmail(pu.getEmail());
		tu.setUsername(pu.getUsername());
		tu.setName(pu.getName());
		tu.setIconSrc(pu.getIconSrc());

		Article ta = new Article();
		BeanUtils.copyProperties(pa, ta, "author", "type", "comments");
		// 只获取作者必要信息
		ta.setAuthor(tu);
		// 只获取类型一级父目录
		ta.setType(typeFilter(pa.getType()));
		// 改变评论懒加载状态，且避免article与comment的交叉引用
		ta.setComments(pa.getComments().stream().filter(c -> c.isApproved()).map(pc -> {
			Comment tc = new Comment();
			BeanUtils.copyProperties(pc, tc, "article");
			return tc;
		}).collect(Collectors.toList()));
		return ta;
	}

	/**
	 * 过滤文章类型
	 * 
	 * @param pt
	 * @return
	 */
	private Type typeFilter(Type pt) {
		if (pt == null)
			return null;
		Type t = new Type();
		BeanUtils.copyProperties(pt, t, "parent", "articles");
		Type pp = pt.getParent();
		if (pp != null) {
			Type tp = new Type();
			BeanUtils.copyProperties(pp, tp, "parent", "articles");
			t.setParent(tp);
		}
		// 只要文章长度，不加载具体的文章
		int size = pt.getArticles().size();
		for (int i = 0; i < size; i++) {
			t.getArticles().add(new Article());
		}
		return t;
	}

	/**
	 * 过滤文章下的评论，主要用于前端
	 * 
	 * @param article
	 */
	private void filterCommentOfArticle(Article article) {
		if (!article.isComment()) {
			article.getComments().clear();
		} else {
			article.getComments().removeIf(comment -> !comment.isApproved());
		}
	}

}
