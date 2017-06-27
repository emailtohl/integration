define(['forum/module', 'common/module'], function(forumModule) {
	return forumModule.factory('forumService', ['$http', 'util', function($http, util) {
		return {
			getPager: function(page) {
				var param = page ? '?page=' + page : '';
				return $http.get('forum/pager' + param);
			},
			search: function(query, page) {
				var param = {
					query: query,
					page: page
				};
				param = util.encodeUrlParams(param);
				return $http.get('forum/search' + (param ? '?' + param : ''));
			},
			'delete': function(id) {
				return $http['delete']('forum/' + id);
			},
			add: function(forumPost) {
				return $http.post('forum', forumPost);
			},
		};
	}]);
});