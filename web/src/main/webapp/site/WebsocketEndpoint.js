/**
 * websocket端点
 * 提供注册函数和订阅两种方式
 */
define(['toastr', 'rx'], function(toastr) {
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
		self.observers = [];
		self.messageStream = Rx.Observable.create(function(observer) {
			self.observers.push(observer);
		});
		self.connection.onmessage = function(e) {
			var message, listener, i, observers = [];
			message = JSON.parse(e.data);
			listener = self.map[message.messageType];
			if (listener instanceof Function) {
				listener(message);
			}
			for (i = 0; i < self.observers.length; i++) {
				if (!self.observers[i].isStopped) {
					observers.push(self.observers[i]);
					self.observers[i].next(message);
				}
			}
			self.observers.length = 0;
			for (i = 0; i < observers.length; i++) {
				self.observers.push(observers[i]);
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