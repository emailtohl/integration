define(['angular', 'toastr', 'dashboard/module'], function(angular, toastr) {
	return angular.module('dashboardModule')
	.controller('DashboardCtrl', ['$scope', '$http', '$state', '$cookies', 'util', function($scope, $http, $state, $cookies, util) {
		var self = this, connection, isHttps = window.location.protocol == 'https:' ? true : false, SECURITY_CODE = "abcdefg0123456789";;
		$scope.getAuthentication();
		self.chatlist = [];
		self.send = function() {
	    	if (connection.readyState != WebSocket.OPEN) {
				toastr.error('WebSocket is Not Open, current state is： ' + connection.readyState);
				connection.onopen();
				return;
			}
	    	var msg = JSON.stringify({
	    		messageType : 'chat',
	    		userId : $scope.getUserId(),
	    		data : {
	    			content : self.message,
		    		iconSrc : $scope.getIconSrc()
	    		},
	    	});
	    	connection.send(msg); // 通过套接字传递该内容
	    	self.message = '';
		};
		// bootstrap-datepicker.js由require加载有问题
		util.loadasync('lib/datepicker/bootstrap-datepicker.js').success(function() {
			util.loadasync('lib/datepicker/locales/bootstrap-datepicker.zh-CN.js').success(function() {
				$('#calendar').datepicker();
			});
		});
		
		connection = openWebsocket();
		connection.onopen = function(e) {
			console.log('打开连接');
		};
		connection.onmessage = function(e) {
			var data = JSON.parse(e.data);
			if (data.messageType == 'chat') {
				chat(data);
			} else if (data.messageType == 'systemInfo') {
				systemInfo(data);
			} else if (data.messageType == 'flowNotify') {
				flowNotify(data);
			}
		};
		connection.onclose = function(e) {
			toastr.error('WebSocketClosed! ' + e.data);
	    }
		connection.onerror = function(e) {
	    	toastr.error('WebSocketError! ' + e.data);
	    	connection.onopen();
	    }
		
		function openWebsocket() {
			var site = util.getRootName() + '/websocket/';
			var url = (isHttps ? 'wss://' : 'ws://') + window.location.host + site + SECURITY_CODE;
			return new WebSocket(url);
		}
		
		function chat(data) {
			console.log(data);
			var time = (new Date(data.time)).toString();
			$scope.$apply(function() {
				self.chatlist.push({
					name : data.userId,
					message : data.content,
					time : time,
					iconSrc : data.iconSrc
				});
			});
			// 划动到底部
			var container = $('.direct-chat-messages');
			var h = container.scrollParent().height() + container.height();
			container.scrollTop(h);
		}
		
		function systemInfo(data) {
			console.log(data);
		}
		
		function flowNotify(data) {
			console.log(data);
		}
		

		
	  /**
	   ** Draw the little mouse speed animated graph
	   ** This just attaches a handler to the mousemove event to see
	   ** (roughly) how far the mouse has moved
	   ** and then updates the display a couple of times a second via
	   ** setTimeout()
	   **/
	  require(['sparkline'], function() {
	    var mrefreshinterval = 500; // update display every 500ms
	    var lastmousex = -1;
	    var lastmousey = -1;
	    var lastmousetime;
	    var mousetravel = 0;
	    var mpoints = [];
	    var mpoints_max = 30;
	    $('html').mousemove(function (e) {
	      var mousex = e.pageX;
	      var mousey = e.pageY;
	      if (lastmousex > -1) {
	        mousetravel += Math.max(Math.abs(mousex - lastmousex), Math.abs(mousey - lastmousey));
	      }
	      lastmousex = mousex;
	      lastmousey = mousey;
	    });
	    var mdraw = function () {
	      var md = new Date();
	      var timenow = md.getTime();
	      if (lastmousetime && lastmousetime != timenow) {
	        var pps = Math.round(mousetravel / (timenow - lastmousetime) * 1000);
	        mpoints.push(pps);
	        if (mpoints.length > mpoints_max)
	          mpoints.splice(0, 1);
	        mousetravel = 0;
	        $('#mousespeed').sparkline(mpoints, {width: mpoints.length * 2, tooltipSuffix: ' pixels per second'});
	      }
	      lastmousetime = timenow;
	      setTimeout(mdraw, mrefreshinterval);
	    };
	    // We could use setInterval instead, but I prefer to do it this way
	    setTimeout(mdraw, mrefreshinterval);
		  
	  });
	}]);
});