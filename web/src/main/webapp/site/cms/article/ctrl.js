define(['cms/module', 'cms/article/service', 'cms/category/service'], function(cmsModule) {
	return cmsModule
	.controller('ArticleCtrl', ['$scope', '$http', '$state', 'articleService', 'categoryService', 'util',
	                                function($scope, $http, $state, service, categoryService, util) {
		const editorID = "article-editor";
		var self = this, promise;
		promise = $scope.getAuthentication();
		categoryService.getTypes().then(function(resp) {
			self.types = resp.data;
		});
		
		// 用于切换界面，详情状态就是新增和编辑，反之则是列表页面
		self.isDetail = false;
		self.article = {};
		self.queryParam = {
			page : 0,
			query : '',
		};
		
		self.query = function() {
			service.search(self.queryParam.query, self.queryParam.page).then(function(resp) {
				self.page = resp.data;
				self.isDetail = false;
			});
		};
		
		self.query();
		
		self.btnClick = function(pageNumber) {
			self.query.page = pageNumber;
			self.query();
		};
		
		self.add = function() {
			self.article = {};
			self.isDetail = true;
			refresh();
		};
		
		self.edit = function(id) {
			service.findArticle(id).then(function(resp) {
				self.article = resp.data;
				if (resp.data.type) {
					self.article.typeId = resp.data.type.id;
				}
				self.isDetail = true;
				refresh();
			});
		};
		
		self.back = function() {
			// 如果有管理权限，则可能做了启动评论、关闭评论，允许发布、禁止发布等功能，所以需要刷新列表。反之则可以直接返回列表页面
			if ($scope.hasAuthority('content_manager')) {
				self.query();
			} else {
				self.isDetail = false;
			}
		};
		
		self.submit = function() {
			if (self.article.id) {
				service.updateArticle(self.article.id, self.article).then(function(resp) {
					self.query();
					self.isDetail = false;
				});
			} else {
				service.saveArticle(self.article).then(function(resp) {
					self.query();
					self.isDetail = false;
				});
			}
		};

		self['delete'] = function() {
			if (!self.article.id)
				return;
			if (confirm('确定删除《' + self.article.title + '》吗？')) {
				service.deleteArticle(self.article.id).then(function(resp) {
					self.query();
				});
			}
		};
		
		self.approveArticle = function() {
			if (!self.article.id)
				return;
			service.approveArticle(self.article.id).then(function(resp) {
				self.article.canApproved = true;
			});
		};
		
		self.rejectArticle = function() {
			if (!self.article.id)
				return;
			service.rejectArticle(self.article.id).then(function(resp) {
				self.article.canApproved = false;
			});
		};
		
		self.openComment = function() {
			if (!self.article.id)
				return;
			service.openComment(self.article.id).then(function(resp) {
				self.article.canComment = true;
			});
		};
		
		self.closeComment = function() {
			if (!self.article.id)
				return;
			service.closeComment(self.article.id).then(function(resp) {
				self.article.canComment = false;
			});
		};
		
		/**
		 * 刷新编辑器区的内容
		 */
		function refresh() {
			// 'ckeditor', 'ckeditorConfig'在define的顶部加载会有问题
			require(['ckeditor', 'ckeditorConfig'], function() {
				var editor = CKEDITOR.instances[editorID]; // 编辑器的"name"属性的值
				if (editor) {
					editor.destroy(true);// 销毁编辑器
				}
				editor = CKEDITOR.replace(editorID, {
					filebrowserImageUploadUrl : getUrlWithCsrfParam(),
				}); // 替换编辑器，editorID为ckeditor的"id"属性的值
				editor.on('change', function(event) {
					self.article.body = this.getData();// 内容
					$scope.$apply();
				});
			});
		}
		
		function getUrlWithCsrfParam() {
			var url = null;
			// 只有登录了，才有CSRF TOKEN，不然上传文件会报错，如果没登录则屏蔽上传文件功能
			if ($scope.isAuthenticated()) {
				url = 'resource/image?type=image';
				var token = util.getCookie('XSRF-TOKEN');
				if (token) {
					url += '&_csrf=' + token;
				}
			}
			return url;
		}
		
	}])
	;
});