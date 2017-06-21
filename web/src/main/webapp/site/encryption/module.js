define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('encryptionModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('encryption', {
					'abstract': 'true',
					url: '/encryption',
					template: '<div ui-view></div>'
				})
				.state('encryption.rsa', {
					url: '/rsa',
					templateUrl: 'site/encryption/rsa/template.html',
					controller: 'RsaCtrl as ctrl'
				})
				.state('encryption.aes', {
					url: '/aes',
					templateUrl: 'site/encryption/aes/template.html',
					controller: 'AesCtrl as ctrl'
				});
		});
});