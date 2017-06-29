define(['angular', 'angular-cookies', 'ui-router'], function(angular) {
	return angular.module('dashboardModule', ['ui.router', 'ngCookies'])
		.config(function($stateProvider) {
			$stateProvider
				.state('dashboard', {
					url: '/dashboard',
					templateUrl: 'site/dashboard/dashboard.html',
					controller: 'DashboardCtrl as ctrl'
				});
		});
});