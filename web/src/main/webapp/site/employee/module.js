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
				.state('employee.edit', {
					url: '/edit/{id}',
					templateUrl: 'site/employee/edit/template.html',
					controller: 'EmployeeEdit as ctrl'
				})
				.state('employee.audit', {
					'abstract': 'true',
					url: '/audit',
					template: '<div ui-view></div>',
				})
				.state('employee.audit.list', {
					url: '/list/{id}',
					templateUrl: 'site/employee/audit/list/template.html',
					controller: 'EmployeeAuditList as ctrl'
				})
				.state('employee.audit.detail', {
					url: '/detail/id/{id}/revision/{revision}',
					templateUrl: 'site/employee/audit/detail/template.html',
					controller: 'EmployeeAuditDetail as ctrl'
				});
		});
});