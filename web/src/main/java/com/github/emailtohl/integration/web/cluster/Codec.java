package com.github.emailtohl.integration.web.cluster;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
/**
 * websocket通信时序列化信息的实现
 * @author HeLei
 */
public class Codec implements Encoder.BinaryStream<ClusterEvent>, Decoder.BinaryStream<ClusterEvent> {
	@Override
	public ClusterEvent decode(InputStream stream) throws DecodeException, IOException {
		try (ObjectInputStream input = new ObjectInputStream(stream)) {
			return (ClusterEvent) input.readObject();
		} catch (ClassNotFoundException e) {
			throw new DecodeException((String) null, "Failed to decode.", e);
		}
	}

	@Override
	public void encode(ClusterEvent event, OutputStream stream) throws IOException {
		try (ObjectOutputStream output = new ObjectOutputStream(stream)) {
			output.writeObject(event);
		}
	}

	@Override
	public void init(EndpointConfig endpointConfig) {
	}

	@Override
	public void destroy() {
	}
}