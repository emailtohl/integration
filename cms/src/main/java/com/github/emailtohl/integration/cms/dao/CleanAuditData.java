package com.github.emailtohl.integration.cms.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
/**
 * 用于清理Hibernate Envers产生审计记录
 * @author HeLei
 * @date 2017.02.04
 */
@Component("cmsCleanAuditData")
public class CleanAuditData {
	@Inject JdbcTemplate jdbcTemplate;
	private static final String delete_revinfo = "DELETE FROM revinfo WHERE rev = ?";
	
	private static final String select_article_rev = "SELECT rev FROM t_article_aud WHERE id = ?";
	private static final String delete_article_aud = "DELETE FROM t_article_aud WHERE id = ?";
	public void cleanArticleAudit(Long id) {
		List<Long> revs = jdbcTemplate.queryForList(select_article_rev, Long.class, id);
		List<Object[]> args = new ArrayList<Object[]>();
		revs.forEach(rev -> {
			args.add(new Long[] {rev});
		});
		jdbcTemplate.update(delete_article_aud, id);
		jdbcTemplate.batchUpdate(delete_revinfo, args);
	}
	
}
