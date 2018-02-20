define(['angular', 'ui-router', 'common/context'], function(angular) {
	return angular.module('cmsModule', ['ui.router', 'commonModule'])
		// 选择框中，过滤掉自身：ng-repeat="x in ctrl.typeList | excludeSelf:ctrl.form.id"
		.filter('excludeSelf', function() {
			return function(arr, selfId) {
				var i, newArr;
				if (!(arr instanceof Array) || !angular.isNumber(selfId))
					return arr;
				newArr = [];
				for (var i = 0; i < arr.length; i++) {
					if (arr[i].id != selfId)
						newArr.push(arr[i]);
				}
				return newArr;
			}
		})
		.config(function($stateProvider) {
			$stateProvider
				.state('cms', {
					'abstract': 'true',
					url: '/cms',
					template: '<div ui-view></div>'
				})
				.state('cms.resource', {
					url: '/resource?query={id}',
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