/**
 * 审核申请单
 */
define(['flow/module', 'flow/service'], function(flowModule) {
	return flowModule
	.controller('FlowAuditCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		const initForm = JSON.stringify({
			page : 0,
			name : null,
			status : null
		});
		var self = this;
		util.loadasync('lib/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css');
		$scope.getAuthentication();
	}]);
});