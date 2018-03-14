/**
 * 提交申请单
 */
define(['flow/module', 'flow/service'], function(flowModule) {
	return flowModule
	.controller('FlowSubmitCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this;
		util.loadasync('lib/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css');
		$scope.getAuthentication();
		self.form = {
			flowType: null,
			content: null,
		};
		self.submit = function() {
			flowService.startWorkflow(self.form).then(function(data) {
				console.log(data);
			});
		};
		
	}]);
});