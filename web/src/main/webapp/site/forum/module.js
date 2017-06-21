define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('forumModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('forum', {
					'abstract': 'true',
					url: '/forum',
					template: '<div ui-view></div>'
				})
				.state('forum.add', {
					url: '/add',
					templateUrl: 'site/forum/add/add.html',
					controller: 'ForumAddCtrl as ctrl'
				})
				.state('forum.search', {
					url: '/search/{query}',
					templateUrl: 'site/forum/search/list.html',
					controller: 'ForumSearchCtrl as ctrl'
				});
		});
});