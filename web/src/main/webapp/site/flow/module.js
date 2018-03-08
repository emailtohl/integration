define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('flowModule', ['ui.router', 'commonModule', 'datePicker']).config(function($stateProvider) {
		$stateProvider
			.state('flow', {
				'abstract': 'true',
				url: '/flow',
				template: '<div ui-view></div>'
			})
			.state('flow.submit', {
				url: '/submit',
				templateUrl: 'site/flow/submit/template.html',
				controller: 'FlowSubmitCtrl as ctrl'
			})
			.state('flow.audit', {
				url: '/audit',
				templateUrl: 'site/flow/audit/template.html',
				controller: 'FlowAuditCtrl as ctrl'
			})
			.state('flow.history', {
				url: '/history',
				templateUrl: 'site/flow/history/template.html',
				controller: 'FlowHistoryCtrl as ctrl'
			});
	});
});