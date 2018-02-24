define(['cms/module', 'common/context' ], function(cmsModule) {
	return cmsModule.factory('articleService', [ '$http', 'util', function($http, util) {
		return {
			search : function(query, page) {
				var param = {
					page : page,
					query : query,
				};
				param = util.encodeUrlParams(param);
				return $http.get('cms/article/search' + (param ? '?' + param : ''));
			},
			getCommentNumbers : function(articleIds) {
				return $http.get('cms/article/commentNumbers?articleIds=' + articleIds);
			},
			findArticle : function(id) {
				return $http.get('cms/article/' + id);
			},
			saveArticle : function(article) {
				return $http.post('cms/article', article);
			},
			updateArticle : function(id, article) {
				return $http.put('cms/article/' + id, article);
			},
			deleteArticle : function(id) {
				return $http['delete']('cms/article/' + id);
			},
			approveArticle : function(articleId) {
				return $http.post('cms/approveArticle?articleId=' + articleId);
			},
			rejectArticle : function(articleId) {
				return $http.post('cms/rejectArticle?articleId=' + articleId);
			},
			openComment : function(articleId) {
				return $http.post('cms/openComment?articleId=' + articleId);
			},
			closeComment : function(articleId) {
				return $http.post('cms/closeComment?articleId=' + articleId);
			},
			
		};
	}]);
});