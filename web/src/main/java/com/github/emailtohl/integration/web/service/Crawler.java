package com.github.emailtohl.integration.web.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 可解析网页
 * @author HeLei
 */
@Component
public class Crawler {
	private static final Logger logger = LogManager.getLogger();
	private static final String HOST;
	static {
		try {
			HOST = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new FatalBeanException("Could not initialize IP addresses.", e);
		}
	}
	
	private String location;
	
	@Inject
	public Crawler(Environment env, ServletContext servletContext) {
		String scheme = env.getProperty("local.scheme", "http");
		String localPort = env.getProperty("local.port", "8080");
		if (!StringUtils.hasText(scheme))
			scheme = "http";
		if (!StringUtils.hasText(localPort))
			localPort = "8080";
		location = scheme + "://" + HOST + ":" + localPort + servletContext.getContextPath();
	}
	
	public Connection getConnection(String url) {
		Connection conn = Jsoup.connect(url)
		.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.header("Accept-Encoding", "gzip, deflate, sdch")
		.header("Accept-Language", "zh-CN,zh;q=0.8")
		.header("Host", "epub.cqvip.com")
		.header("Proxy-Connection", "keep-alive")
		.header("Referer", "http://epub.cqvip.com/manage/_main_left.aspx")
		.header("Upgrade-Insecure-Requests", "1")
		.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
//		if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {
//			conn.proxy(proxyHost, Integer.valueOf(proxyPort));
//		}
		return conn;
	}
	
	@Scheduled(fixedDelay = 1000 * 3600)
	public void fetch() {
		Connection conn = getConnection(location + "/article");
//		conn.cookie("cookie_admin_username", "zt")
//		.cookie("cookie_admin_password", "4da64b5779c9d82140c450b33124ccc3");
		Document doc = null;
		try {
			doc = conn.get();
		} catch (IOException e1) {
			logger.warn("{} 获取{} 失败", getClass().getName(), location + "/article");
		}
		if (doc == null) {
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug(doc);
		}
		doc.getElementsByTag("a").forEach(a -> {
			String href = a.attr("href");
			if (StringUtils.hasText(href) && href.startsWith("detail?id=")) {
				Connection subConn = getConnection(location + "/" + href);
				try {
					Document detailDoc = subConn.get();
					String body = detailDoc.getElementById("article-body").text();
					logger.debug(body);
				} catch (IOException e) {
					logger.catching(e);
				}
			}
		});
	}
}
