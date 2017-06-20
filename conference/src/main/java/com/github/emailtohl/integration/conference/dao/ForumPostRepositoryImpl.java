package com.github.emailtohl.integration.conference.dao;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.conference.entities.ForumPost;
/**
 * 论坛模块的数据源
 * @author HeLei
 * @date 2017.02.04
 */
public class ForumPostRepositoryImpl extends AbstractSearchableRepository<ForumPost>
		implements SearchableRepository<ForumPost> {
}
