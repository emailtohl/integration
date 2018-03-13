/**
 * 我的流程单
 */
define(['flow/module', 'flow/service'], function(flowModule, service) {
	return flowModule
	.controller('FlowMineCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this;
		$scope.getAuthentication().then(function() {
			flowService.list({applicantId: $scope.getUserId()}).then(function(resp) {
				console.log(resp);
				self.list = resp.data;
			});
		});
		
	}]);
});