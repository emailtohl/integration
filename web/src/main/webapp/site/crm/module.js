define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('crmModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('crm', {
					url: '/crm',
					templateUrl: 'site/crm/template.html',
					controller: 'Crm as ctrl'
				});
		});
});