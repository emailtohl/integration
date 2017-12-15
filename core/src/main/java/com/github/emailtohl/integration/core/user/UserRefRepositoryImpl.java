package com.github.emailtohl.integration.core.user;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 自定义的用户引用数据访问层的实现
 * @author HeLei
 */
class UserRefRepositoryImpl extends AbstractSearchableRepository<UserRef> implements UserRefRepositoryCustomization {

}
