define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('flowModule', ['ui.router', 'commonModule', 'datePicker']).config(function($stateProvider) {
		$stateProvider
			.state('flow', {
				'abstract': 'true',
				url: '/flow',
				template: '<div ui-view></div>'
			})
			.state('flow.mine', {
				url: '/mine',
				templateUrl: 'site/flow/mine/template.html',
				controller: 'FlowMineCtrl as ctrl'
			})
			.state('flow.detail', {// audit参数代表是否审批，如果是申请人进去则是0，如果是审核人进去，则是1可以签收并提交意见
				url: '/detail/{id}/audit/{audit}',
				templateUrl: 'site/flow/detail/template.html',
				controller: 'FlowDetailCtrl as ctrl'
			})
			.state('flow.submit', {
				url: '/submit',
				templateUrl: 'site/flow/submit/template.html',
				controller: 'FlowSubmitCtrl as ctrl'
			})
			.state('flow.reSubmit', {
				url: '/reSubmit/{id}',
				templateUrl: 'site/flow/submit/template.html',
				controller: 'FlowReSubmitCtrl as ctrl'
			})
			.state('flow.candidate', {
				url: '/candidate',
				templateUrl: 'site/flow/candidate/template.html',
				controller: 'FlowCandidateCtrl as ctrl'
			})
			;
	})
	;
});