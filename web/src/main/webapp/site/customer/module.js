define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('customerModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('customer', {
					'abstract': 'true',
					url: '/customer',
					template: '<div ui-view></div>'
				})
				.state('customer.list', {
					url: '/list',
					templateUrl: 'site/customer/list/template.html',
					controller: 'CustomerList as ctrl'
				})
				.state('customer.detail', {
					url: '/detail/{id}',
					templateUrl: 'site/customer/detail/template.html',
					controller: 'CustomerDetail as ctrl'
				})
				.state('customer.add', {
					url: '/add',
					templateUrl: 'site/customer/add/template.html',
					controller: 'CustomerAdd as ctrl'
				})
				.state('customer.edit', {
					url: '/edit/{id}',
					templateUrl: 'site/customer/edit/template.html',
					controller: 'CustomerEdit as ctrl'
				})
				.state('customer.audit', {
					'abstract': 'true',
					url: '/audit',
					template: '<div ui-view></div>',
				})
				.state('customer.audit.list', {
					url: '/list/{id}',
					templateUrl: 'site/customer/audit/list/template.html',
					controller: 'CustomerAuditList as ctrl'
				})
				.state('customer.audit.detail', {
					url: '/detail/id/{id}/revision/{revision}',
					templateUrl: 'site/customer/audit/detail/template.html',
					controller: 'CustomerAuditDetail as ctrl'
				});
		});
});