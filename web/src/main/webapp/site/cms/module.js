define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('cmsModule', ['ui.router', 'commonModule'])
		.config(function($stateProvider) {
			$stateProvider
				.state('cms', {
					'abstract': 'true',
					url: '/cms',
					template: '<div ui-view></div>'
				})
				.state('cms.resource', {
					url: '/resource',
					templateUrl: 'site/cms/resource/template.html',
					controller: 'ResourceCtrl as ctrl'
				})
				.state('cms.category', {
					url: '/category',
					templateUrl: 'site/cms/category/template.html',
					controller: 'CategoryCtrl as ctrl'
				})
				.state('cms.article', {
					url: '/article',
					templateUrl: 'site/cms/article/template.html',
					controller: 'ArticleCtrl as ctrl'
				})
				.state('cms.comment', {
					url: '/comment',
					templateUrl: 'site/cms/comment/template.html',
					controller: 'CommentCtrl as ctrl'
				});
		});
});