package com.github.emailtohl.integration.core.coreTestConfig;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.core.Profiles;

/**
 * 环境配置只改此处即可
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreTestConfiguration.class)
@ActiveProfiles({ Profiles.DB_RAM_H2, Profiles.ENV_NO_SERVLET })
public abstract class CoreTestEnvironment {

}
