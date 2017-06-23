define(['forum/module', 'forum/service'], function(forumModule) {
	return forumModule
		.controller('ForumSearch', ['$scope', '$http', '$state', 'forumService', function($scope, $http, $state, forumService) {
			var self = this;
			$scope.getAuthentication();
			var queryInput = $('form[name="fulltextsearch"]').find('input[name="search"]');

			if($state.params.query && $state.params.query.trim()) {
				forumService.search($state.params.query, 1).then(function(resp) {
					self.pager = resp.data;
				});
			} else {
				forumService.getPager(1).then(function(resp) {
					self.pager = resp.data;
				});
			}

			self.renderFinish = function() {
				CKEDITOR.replaceAll($('textarea'));
				CKEDITOR.on('instanceReady', function(ev) {
					var editor = ev.editor;
					if(editor.name != 'editor1') {
						editor.setReadOnly(true);
					}
				});
			};

			self.search = function(pageNumber) {
				var query = queryInput.val();
				if(query && query.trim()) {
					forumService.search(query, pageNumber).then(function(resp) {
						self.pager = resp.data;
					});
				} else {
					forumService.getPager(pageNumber).then(function(resp) {
						self.pager = resp.data;
					});
				}
			};

			self.getIconSrc = function(obj) {
				return obj && obj.user && obj.user.iconSrc;
			};
			self['delete'] = function(id, $event) {
				$event.stopPropagation();
				if(confirm('确定删除吗？')) {
					forumService['delete'](id).then(function(resp) {
						forumService.getPager(1).then(function(resp) {
							self.pager = resp.data;
						});
					});
				}
			};
		}]);
});