define(['flow/module', 'flow/service'], function(flowModule, service) {
	return flowModule
	.controller('FlowDetailCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this;
		self.openModal = false;
		$scope.getAuthentication();
		
		self.isAudit = function() {
			return $state.params.audit == 1;
		}
		
		flowService.get($state.params.id).then(function(resp) {
			self.flowData = resp.data;
			console.log(self.flowData);
		});
		
		
		self.claim = function() {
			flowService.claim(self.flowData.taskId).then(function(resp) {
				if (resp.data.ok) {
					self.openModal = true;
				}
			});
		};
		
		self.check = {
			checkApproved : true,
			checkComment : null,
		};
		
		self.audit = function() {
			flowService.check(self.check).then(function(resp) {
				self.openModal = false;
				$state.go('flow.audit');
			});
		};
		
	}]);
});