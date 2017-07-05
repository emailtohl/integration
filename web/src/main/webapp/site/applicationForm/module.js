define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('applicationFormModule', ['ui.router', 'commonModule', 'datePicker']).config(function($stateProvider) {
		$stateProvider
			.state('applicationForm', {
				'abstract': 'true',
				url: '/applicationForm',
				template: '<div ui-view></div>'
			})
			.state('applicationForm.submit', {
				url: '/submit',
				templateUrl: 'site/applicationForm/application/template.html',
				controller: 'ApplicationCtrl as ctrl'
			})
			.state('applicationForm.audit', {
				url: '/audit',
				templateUrl: 'site/applicationForm/audit/template.html',
				controller: 'ApplicationFormAuditCtrl as ctrl'
			})
			.state('applicationForm.history', {
				url: '/history',
				templateUrl: 'site/applicationForm/history/template.html',
				controller: 'ApplicationFormHistoryCtrl as ctrl'
			});
	});
});