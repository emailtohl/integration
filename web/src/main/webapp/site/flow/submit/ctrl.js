/**
 * 提交申请单
 */
define(['flow/module', 'flow/service', 'toastr'], function(flowModule) {
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
			flowService.startWorkflow(self.form).then(function(resp) {
				if (resp.data.id) {
					$state.go('flow.detail', {id: resp.data.id, audit:0});
				}
			});
		};
		
	}])
	.controller('FlowReSubmitCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
		, function($scope, $http, $state, flowService, util) {
		var self = this;
		util.loadasync('lib/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css');
		$scope.getAuthentication();
		
		self.isReSubmit = function() {
			return $state.current.name === 'flow.reSubmit';
		};
		
		flowService.get($state.params.id).then(function(resp) {
			self.form = resp.data;
		});
		
		self.submit = function() {
			self.form.reApply = true;
			flowService.reApply(self.form).then(function(resp) {
				if (resp.data.ok) {
					$state.go('flow.detail', {id: $state.params.id, audit:0});
				} else {
					toastr.error('提交失败');
				}
			});
		};
		
		self.cancel = function() {
			self.form.reApply = false;
			flowService.reApply(self.form).then(function(resp) {
				if (resp.data.ok) {
					$state.go('flow.detail', {id: $state.params.id, audit:0});
				} else {
					toastr.error('提交失败');
				}
			});
		};
		
	}])
	;
});