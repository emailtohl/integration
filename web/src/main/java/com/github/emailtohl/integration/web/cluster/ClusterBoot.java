package com.github.emailtohl.integration.web.cluster;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
/**
 * 集群系统的引导程序
 * 初始化时会监听网络中的广播：socket.receive(packet);
 * 该广播会在收到新加入集群的端点的url时触发。
 * 一旦接收到新加入集群的端点的url时，该引导程序就会新建一个Websocket连接：multicaster.registerNode(url);
 * @author HeLei
 */
@Component
public class ClusterBoot implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LogManager.getLogger();
	public static final String SECURITY_CODE = "abcdefg0123456789";
	public static final String RESPONSE_OK = "ok";
	
	private static final String HOST;
	private static final int PORT = 6789;
	private static final InetAddress GROUP;
	static {
		try {
			HOST = InetAddress.getLocalHost().getHostAddress();
			GROUP = InetAddress.getByName("224.0.0.3");
		} catch (UnknownHostException e) {
			throw new FatalBeanException("Could not initialize IP addresses.", e);
		}
	}

	private volatile boolean initialized = false, destroyed = false;
	private String pingUrl, messagingUrl;
	private MulticastSocket socket;
	private Thread listener;

	@Inject
	ServletContext servletContext;
	@Inject
	ClusterEventMulticaster multicaster;
	@Inject
	Environment env;

	@PostConstruct
	public void listenForMulticastAnnouncements() throws Exception {
		String scheme = env.getProperty("local.scheme");
		String localPort = env.getProperty("local.host");
		if (!StringUtils.hasText(scheme))
			scheme = "http";
		if (!StringUtils.hasText(localPort))
			localPort = "8080";
		pingUrl = scheme + "://" + HOST + ":" + localPort + servletContext.getContextPath() + "/ping";
		messagingUrl = "http".equals(scheme) ? "ws" : "wss" + "://" + HOST + ":" + localPort 
				+ servletContext.getContextPath() + "/cluster/" + SECURITY_CODE;

		synchronized (getClass()) {
			socket = new MulticastSocket(PORT);
			socket.joinGroup(GROUP);
			listener = new Thread(this::listen, "cluster-listener");
			listener.start();
		}
	}

	private void listen() {
		byte[] buffer = new byte[2048];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (true) {
			try {
				socket.receive(packet);
				String url = new String(buffer, 0, packet.getLength());
				if (url.length() == 0)
					logger.warn("Received blank multicast packet.");
				else if (url.equals(messagingUrl))
					logger.info("Ignoring our own multicast packet.");
				else
					multicaster.registerNode(url);
			} catch (IOException e) {
				if (!destroyed)
					logger.error(e);
				return;
			}
		}
	}

	@PreDestroy
	public void shutDownMulticastConnection() throws IOException {
		destroyed = true;
		try {
			listener.interrupt();
			socket.leaveGroup(GROUP);
		} finally {
			socket.close();
		}
	}

	@Async
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (initialized)
			return;
		initialized = true;
		try {
			URL url = new URL(pingUrl);
			logger.info("Attempting to connect to self at {}.", url);
			int tries = 0;
			while (true) {
				tries++;
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(100);
				try (InputStream stream = connection.getInputStream()) {
					String response = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
					if (response != null && response.equals(RESPONSE_OK)) {
						logger.info("Broadcasting multicast announcement packet.");
						DatagramPacket packet = new DatagramPacket(messagingUrl.getBytes(),
								messagingUrl.length(), GROUP, PORT);
						synchronized (getClass()) {
							socket.send(packet);
						}
						return;
					} else
						logger.warn("Incorrect response: {}", response);
				} catch (Exception e) {
					if (tries > 120) {
						logger.fatal("Could not connect to self within 60 seconds.", e);
						return;
					}
					Thread.sleep(400L);
				}
			}
		} catch (Exception e) {
			logger.fatal("Could not connect to self.", e);
		}
	}
}
