package com.github.emailtohl.integration.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.web.WebTestData;
import com.github.emailtohl.integration.web.webTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.web.webTestConfig.ServiceConfiguration;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
/**
 * 本类测试依赖DataSourceConfiguration配置中的templatesPath
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles({ DataSourceConfiguration.H2_RAM_DB })
public class CmsCtrlTest {
	@Inject
	Configuration cfg;
	@Inject
	@Named("templatesPath")
	File templatesPath;
	String filename = "index.html";
	File test;

	@Before
	public void setUp() throws Exception {
		test = new File(templatesPath, filename);
		FileUtils.write(test, "hello ${name}", StandardCharsets.UTF_8);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteQuietly(test);
	}

	@Test
	public void test() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
		WebTestData td = new WebTestData();
		Template temp = cfg.getTemplate(filename);
		 /* Merge data-model with template */
        Writer out = new OutputStreamWriter(System.out);
        temp.process(td.foo, out);
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output
        out.close();
	}

}
