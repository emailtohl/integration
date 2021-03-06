/**
 * 在详情里面审批
 */
define(['flow/module', 'flow/service', 'toastr'], function(flowModule, service, toastr) {
	return flowModule
	.controller('FlowDetailCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this;
		$scope.getAuthentication().then(function() {
			load();
		});

		var subscription = $scope.websocketEndpoint.messageStream.subscribe(function(message) {
			if ('flowNotify' == message.messageType) {
				load();
			}
		});
		
		$scope.$on('$destroy',function () {
			subscription.completed();
        });
		
		function load() {
			flowService.get($state.params.id).then(function(resp) {
				self.flowData = resp.data;
				if (self.isAudit()) {
					self.check = {
						id : self.flowData.id,
						taskId : self.flowData.taskId
					};
				}
				flowService.getCommentInfo(self.flowData.processInstanceId).then(function(resp) {
					console.log(resp.data);
				});
			});
		}

		self.claim = function() {
			flowService.claim(self.flowData.taskId).then(function(resp) {
				if (resp.data.ok) {
					$('#modal-audit').modal();
				} else {
					toastr.error('该任务已被人签收');
					$state.go('flow.candidate');
				}
			});
		};
		
		self.audit = function() {
			flowService.check(self.check).then(function(resp) {
				$('#modal-audit').modal('hide');
				$('.modal-backdrop').filter('.fade').filter('.in').remove();
				if (!resp.data.ok) {
					toastr.error(resp.data.cause);
				}
				$state.go('flow.candidate');
			});
		};
		
		self.isAudit = function() {
			return $state.params.audit == 1;
		}
	}]);
});