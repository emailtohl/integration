define(['user/module', 'user/service'], function(userModule) {
	return userModule
		.controller('UserList', ['$scope', '$http', '$state', 'userService', function($scope, $http, $state, userService) {
			var self = this;
			$scope.getAuthentication();
			self.params = {
				page: 0,
				size: 5,
			};
			self.query = function() {
				userService.getUserPager(self.params).then(function(resp) {
					console.log(resp.data);
					self.pager = resp.data;
				});
			};
			self.reset = function() {
				self.params = {
					page: 0,
					size: 5,
					enabled: '',
				};
			};
			self.btnClick = function(pageNumber) {
				self.params.page = pageNumber;
				self.query();
			};

			self['delete'] = function(id) {
				if(confirm('确认删除吗？')) {
					userService['delete'](id).then(function(resp) {
						self.query();
					});
				}
			};
			// 查询
			self.query();

		}]);
});