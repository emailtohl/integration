define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('roleModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('role', {
					url: '/config',
					templateUrl: 'site/role/config/template.html',
					controller: 'RoleConfig as ctrl'
				})
				.state('roleAudit', {
					'abstract': 'true',
					url: '/audit',
					template: '<div ui-view></div>',
				})
				.state('roleAudit.list', {
					url: '/list',
					templateUrl: 'site/roleAuthCfg/audit/template.html',
					controller: 'RoleAuditList as ctrl'
				})
				.state('roleAudit.detail', {
					url: '/detail/id/{id}/revision/{revision}',
					templateUrl: 'site/roleAuthCfg/audit/template.html',
					controller: 'RoleAuditDetail as ctrl'
				});
		});
});