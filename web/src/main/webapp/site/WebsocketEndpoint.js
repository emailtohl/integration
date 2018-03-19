/**
 * websocket端点
 */
define(['toastr'], function(toastr) {
	return function WebsocketEndpoint(url) {
		var self = this;
		self.connection = new WebSocket(url);
		self.map = {};
		self.addListener = function(eventName, listener) {
			self.map[eventName] = listener;
		};
		self.removeListener = function(eventName) {
			delete self.map[eventName];
		};
		self.connection.onmessage = function(e) {
			var message = JSON.parse(e.data);
			var listener = self.map[message.messageType];
			if (listener instanceof Function) {
				listener(message);
			}
		};
		self.connection.onclose = function(e) {
			toastr.error('WebSocketClosed! ' + e.data);
	    }
		self.connection.onerror = function(e) {
	    	toastr.error('WebSocketError! ' + e.data);
	    }
		self.isOpen = function() {
			return self.connection.readyState == WebSocket.OPEN;
		};
		self.send = function(json) {
			self.connection.send(json);
		};
		console.log('打开Websocket连接');
	}
});