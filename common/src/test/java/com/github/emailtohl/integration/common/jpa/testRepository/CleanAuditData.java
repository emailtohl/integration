package com.github.emailtohl.integration.common.jpa.testRepository;

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
@Component
public class CleanAuditData {
	@Inject
	JdbcTemplate jdbcTemplate;
	private static final String delete_revinfo = "DELETE FROM revinfo WHERE rev = ?";
	
	private static final String select_user_rev = "SELECT rev FROM t_user_aud WHERE id = ?";
	private static final String delete_user_role_aud = "DELETE FROM t_user_role_aud WHERE user_id = ?";
	private static final String delete_user_aud = "DELETE FROM t_user_aud t WHERE id = ?";
	public void cleanUserAudit(Long id) {
		List<Long> revs = jdbcTemplate.queryForList(select_user_rev, Long.class, id);
		List<Object[]> args = new ArrayList<Object[]>();
		revs.forEach(rev -> {
			args.add(new Long[] {rev});
		});
		jdbcTemplate.update(delete_user_role_aud, id);
		jdbcTemplate.update(delete_user_aud, id);
		jdbcTemplate.batchUpdate(delete_revinfo, args);
	}
	
	private static final String select_role_rev = "SELECT rev FROM t_role_aud WHERE id = ?";
	private static final String delete_role_authority_aud = "DELETE FROM t_role_authority_aud WHERE role_id = ?";
	private static final String delete_role_user_aud = "DELETE FROM t_user_role_aud WHERE role_id = ?";
	private static final String delete_user_aud_by_rev = "DELETE FROM t_user_aud WHERE rev = ?";
	private static final String delete_role_aud = "DELETE FROM t_role_aud WHERE id = ?";
	public void cleanRoleAudit(Long id) {
		List<Long> revs = jdbcTemplate.queryForList(select_role_rev, Long.class, id);
		List<Object[]> args = new ArrayList<Object[]>();
		revs.forEach(rev -> {
			args.add(new Long[] {rev});
		});
		jdbcTemplate.update(delete_role_authority_aud, id);
		jdbcTemplate.update(delete_role_user_aud, id);
		jdbcTemplate.update(delete_role_aud, id);
		jdbcTemplate.batchUpdate(delete_user_aud_by_rev, args);
		jdbcTemplate.batchUpdate(delete_revinfo, args);
	}
	
	private static final String select_authority_rev = "SELECT rev FROM t_authority_aud WHERE id = ?";
	private static final String delete_authority_aud = "DELETE FROM t_authority_aud WHERE id = ?";
	public void cleanAuthorityAudit(Long id) {
		List<Long> revs = jdbcTemplate.queryForList(select_authority_rev, Long.class, id);
		List<Object[]> args = new ArrayList<Object[]>();
		revs.forEach(rev -> {
			args.add(new Long[] {rev});
		});
		jdbcTemplate.update(delete_authority_aud, id);
		jdbcTemplate.batchUpdate(delete_revinfo, args);
	}
	
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
