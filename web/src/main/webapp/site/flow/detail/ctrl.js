define(['flow/module', 'flow/service'], function(flowModule, service) {
	return flowModule
	.controller('FlowDetailCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this;
		$scope.getAuthentication();
		flowService.get($state.params.id).then(function(resp) {
			self.flowData = resp.data;
			console.log(self.flowData);
		});
		
	}]);
});