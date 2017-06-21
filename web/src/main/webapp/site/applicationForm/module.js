define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('applicationFormModule', ['ui.router', 'commonModule']).config(function($stateProvider) {
		$stateProvider
			.state('applicationForm', {
				'abstract': 'true',
				url: '/applicationForm',
				template: '<div ui-view></div>'
			})
			.state('applicationForm.submit', {
				url: '/submit',
				templateUrl: 'site/applicationForm/application/application.html',
				controller: 'ApplicationCtrl as ctrl'
			})
			.state('applicationForm.audit', {
				url: '/audit',
				templateUrl: 'site/applicationForm/audit/audit.html',
				controller: 'ApplicationFormAuditCtrl as ctrl'
			})
			.state('applicationForm.history', {
				url: '/history',
				templateUrl: 'site/applicationForm/history/history.html',
				controller: 'ApplicationFormHistoryCtrl as ctrl'
			});
	});
});