define(['angular', 'ui-router', 'ui-select', 'common/context'], function(angular) {
	return angular.module('userModule', ['ui.router', 'ui.select', 'ngSanitize', 'commonModule']).config(function($stateProvider) {
		$stateProvider
		.state('user', {
			'abstract' : 'true',
			url : '/user',
			template : '<div ui-view></div>'
		})
		.state('user.list', {
			url : '/list',
			templateUrl : 'site/user/manager/list.html',
			controller : 'UserListCtrl as ctrl'
		})
		.state('user.detail', {
			url : '/detail/{id}',
			templateUrl : 'site/user/manager/detail.html',
			controller : 'UserDetailCtrl as ctrl'
		})
		.state('user.addUser', {
			url : '/addUser',
			templateUrl : 'site/user/manager/addUser.html',
			controller : 'AddUserCtrl as ctrl'
		})
		.state('user.role', {
			url : '/role',
			templateUrl : 'site/user/role/role.html',
			controller : 'RoleAllocationCtrl as ctrl'
		})
		.state('user.audit', {
			'abstract' : 'true',
			url : '/audit',
			template : '<div ui-view></div>',
		})
		.state('user.audit.list', {
			url : '/list',
			templateUrl : 'site/user/audit/list.html',
			controller : 'UserAuditListCtrl as ctrl'
		})
		.state('user.audit.detail', {
			url : '/detail/id/{id}/revision/{revision}',
			templateUrl : 'site/user/audit/detail.html',
			controller : 'UserAuditDetailCtrl as ctrl'
		})
		;
	});
});