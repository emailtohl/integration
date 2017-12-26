define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('employeeModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('employee', {
					'abstract': 'true',
					url: '/employee',
					template: '<div ui-view></div>'
				})
				.state('employee.list', {
					url: '/list',
					templateUrl: 'site/employee/list/template.html',
					controller: 'EmployeeList as ctrl'
				})
				.state('employee.detail', {
					url: '/detail/{id}',
					templateUrl: 'site/employee/detail/template.html',
					controller: 'EmployeeDetail as ctrl'
				})
				.state('employee.add', {
					url: '/add',
					templateUrl: 'site/employee/add/template.html',
					controller: 'EmployeeAdd as ctrl'
				})
				.state('employeeAudit.list', {
					url: '/list/{id}',
					templateUrl: 'site/employee/audit/list/template.html',
					controller: 'EmployeeAuditList as ctrl'
				})
				.state('employeeAudit.detail', {
					url: '/detail/id/{id}/revision/{revision}',
					templateUrl: 'site/employee/audit/detail/template.html',
					controller: 'EmployeeAuditDetail as ctrl'
				});
		});
});