define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('roleAuthCfgModule', ['ui.router', 'commonModule'])
	.config(function($stateProvider) {
		$stateProvider
		.state('roleAuthCfg', {
			url : '/roleAuthCfg',
			templateUrl : 'site/roleAuthCfg/roleAuthCfg.html',
			controller : 'RoleAuthCfgCtrl as ctrl'
		})
		.state('roleAuthCfgAudit', {
			'abstract' : 'true',
			url : '/audit',
			template : '<div ui-view></div>',
		})
		.state('roleAuthCfgAudit.list', {
			url : '/list',
			templateUrl : 'site/roleAuthCfg/audit/list.html',
			controller : 'RoleAuthAuditListCtrl as ctrl'
		})
		.state('roleAuthCfgAudit.detail', {
			url : '/detail/id/{id}/revision/{revision}',
			templateUrl : 'site/roleAuthCfg/audit/detail.html',
			controller : 'RoleAuthAuditDetailCtrl as ctrl'
		})
		;
	});
});