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
					templateUrl: 'site/forum/add/template.html',
					controller: 'ForumAdd as ctrl'
				})
				.state('forum.search', {
					url: '/search',
					templateUrl: 'site/forum/search/template.html',
					controller: 'ForumSearch as ctrl'
				});
		});
});