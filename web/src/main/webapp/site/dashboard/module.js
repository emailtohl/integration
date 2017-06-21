define(['angular', 'angular-cookies', 'ui-router'/*, 'chartjs', 'jvectormap', 'jvectormap-world'*/], function(angular) {
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