define(['angular', 'ui-router', 'angular-cookies', 'common/context'], function(angular) {
	return angular.module('dashboardModule', ['ui.router', 'ngCookies', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('dashboard', {
					url: '/dashboard',
					templateUrl: 'site/dashboard/dashboard.html',
					controller: 'DashboardCtrl as ctrl'
				});
		});
});