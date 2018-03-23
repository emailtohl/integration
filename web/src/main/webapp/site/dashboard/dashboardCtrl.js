define(['angular', 'toastr', 'dashboard/module', 'knob'], function(angular, toastr) {
	return angular.module('dashboardModule')
	.controller('DashboardCtrl', ['$rootScope', '$scope', '$http', '$state', '$cookies', 'util', function($rootScope, $scope, $http, $state, $cookies, util) {
		var self = this, $knob = $(".knob"), isCreated = false;
		$scope.getAuthentication();
		self.chatlist = [];
		self.systemInfo = {};
		// bootstrap-datepicker.js由require加载有问题
		util.loadasync('lib/datepicker/bootstrap-datepicker.js').success(function() {
			util.loadasync('lib/datepicker/locales/bootstrap-datepicker.zh-CN.js').success(function() {
				$('#calendar').datepicker();
			});
		});
		
		$scope.websocketEndpoint.addListener('chat', chat);
		$scope.websocketEndpoint.addListener('systemInfo', systemInfo);
		
		$scope.$on('$destroy',function () {
			$scope.websocketEndpoint.removeListener('chat');
			$scope.websocketEndpoint.removeListener('systemInfo');
        });
		
		function chat(message) {
			$scope.$apply(function() {
				self.chatlist.push({
					nickname : message.data && message.data.nickname,
					message : message.data && message.data.content,
					time : message.time,
					iconSrc : message.data && message.data.iconSrc
				});
			});
			// 划动到底部
			var container = $('.direct-chat-messages');
			var h = container.scrollParent().height() + container.height();
			container.scrollTop(h);
		}
		
		function systemInfo(message) {
			var cpuPoints = [], $cpu = $('span#cpuInfo'), memoryPoints = [], $memory = $('span#memoryInfo'), swapPoints = [], $swap = $('span#swapInfo'), mpoints_max = 30;
			if (!$state.includes('dashboard'))
				return;
			var data = message.data;
			if (data.getFreePhysicalMemorySize && data.getTotalPhysicalMemorySize) {
				self.systemInfo.memory = (data.getFreePhysicalMemorySize / data.getTotalPhysicalMemorySize) * 100;
				memoryPoints.push(self.systemInfo.memory);
				if (memoryPoints.length > mpoints_max)
					memoryPoints.splice(0, 1);
				if ($memory.sparkline instanceof Function) {
					$memory.sparkline(memoryPoints);
				}
			}
			if (data.getFreeSwapSpaceSize && data.getTotalSwapSpaceSize) {
				self.systemInfo.swap = (data.getFreeSwapSpaceSize / data.getTotalSwapSpaceSize) * 100;
				swapPoints.push(self.systemInfo.swap);
				if (swapPoints.length > mpoints_max)
					swapPoints.splice(0, 1);
				$swap.sparkline(swapPoints);
			}
			if (data.getSystemCpuLoad) {
				self.systemInfo.cpu = data.getSystemCpuLoad * 100;
				cpuPoints.push(self.systemInfo.cpu);
				if (cpuPoints.length > mpoints_max)
					cpuPoints.splice(0, 1);
				$cpu.sparkline(cpuPoints);
			}
			if (data.getCommittedVirtualMemorySize) {
				self.systemInfo.committedVirtualMemorySize = data.getCommittedVirtualMemorySize / 1024 / 1024 / 1024;
			}
			$scope.$apply(function() {
				if (isCreated) {
					$knob.trigger('change');
				} else {
					$knob.knob().trigger('change');
					isCreated = true;
				}
			});
		
		}
		
		self.send = function() {
	    	var msg = JSON.stringify({
	    		messageType : 'chat',
	    		userId : $scope.getUserId(),
	    		data : {
	    			nickname : $scope.authentication && $scope.authentication.username,
	    			content : self.message,
		    		iconSrc : $scope.getIconSrc()
	    		},
	    	});
	    	$rootScope.websocketEndpoint.send(msg); // 通过套接字传递该内容
	    	self.message = '';
		};
		
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