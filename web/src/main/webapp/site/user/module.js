define(['angular', 'ui-router', 'common/context', 'ui-bootstrap'], function(angular) {
	return angular.module('userModule', ['ui.router', 'commonModule', 'ui.bootstrap'])
		.config(function($stateProvider) {
			$stateProvider
				.state('user', {
					'abstract': 'true',
					url: '/user',
					template: '<div ui-view></div>'
				})
				.state('user.list', {
					url: '/list',
					templateUrl: 'site/user/list/template.html',
					controller: 'UserList as ctrl'
				})
				.state('user.detail', {
					url: '/detail/{id}',
					templateUrl: 'site/user/detail/template.html',
					controller: 'UserDetail as ctrl'
				})
				.state('user.add', {
					url: '/add',
					templateUrl: 'site/user/add/template.html',
					controller: 'UserAdd as ctrl'
				})
				.state('user.role', {
					url: '/role',
					templateUrl: 'site/user/role/template.html',
					controller: 'UserRole as ctrl'
				})
				.state('user.audit', {
					'abstract': 'true',
					url: '/audit',
					template: '<div ui-view></div>',
				})
				.state('user.audit.list', {
					url: '/list',
					templateUrl: 'site/user/audit/list/template.html',
					controller: 'UserAuditList as ctrl'
				})
				.state('user.audit.detail', {
					url: '/detail/id/{id}/revision/{revision}',
					templateUrl: 'site/user/audit/detail/template.html',
					controller: 'UserAuditDetail as ctrl'
				});
		});
});