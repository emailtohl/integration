/**
利用websocket实现集群功能

该包使用了Spring-context包中的消息发布-订阅(Publish-Subscribe)技术，能很好地解决观察者模式的紧耦合问题，利用该技术再结合websocket，可使集群环境下各服务端通过广播地址创建连接，从而发布集群消息。
(1) 首先创建出继承ApplicationEvent的ClusterEvent，将原java.util.EventObject中瞬时的source改为持久化；
(2) 关注ClusterManager，当spring的上下文初始化或刷新时，会触发ContextRefreshedEvent，这时候就发起连接到本服务地址上；
(3) 经过短暂的响应后ClusterManager就会将自身的地址通过socket发到广播地址上；
(4) ClusterManager的listener属性是一个线程，它也使用socket（基于TCP双向收发消息）监听广播地址上的消息（没有消息时会在receive处阻塞）；
(5) 当收到消息时，如果是自己的地址就忽略，否则就根据该消息创建一个websocket连接，并将该websocket连接注册到ClusterEventMulticaster中；
(6) 一旦使用ApplicationEventPublisher#publishEvent(ClusterEvent event)，ClusterEventMulticaster的multicastEvent(ApplicationEvent event)就会广播该消息，不仅实现ApplicationListener的类会收到，websocket中的各节点也会收到。

注意：端点的IP是通过InetAddress.getLocalHost().getHostAddress();获取，注意多个端点在同一网段中；此外，若端点的端口号不是8080，则需要配置config.properties文件中的local.host值。
*/
/**
 * @author HeLei
 */
package com.github.emailtohl.integration.web.message;