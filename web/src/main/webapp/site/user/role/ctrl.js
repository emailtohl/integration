define(['user/module', 'user/service'], function(userModule) {
	return userModule
		.controller('UserRole', ['$scope', '$http', '$state', 'userService', function($scope, $http, $state, userService) {
			var self = this;
			$scope.getAuthentication();
			self.params = {
				page: 0,
				size: 10,
				email: '',
				roles: []
			};
			userService.getRoles().then(function(resp) {
				self.roles = resp.data;
			});
			self.query = function() {
				userService.getPageByRoles(self.params).then(function(resp) {
					self.pager = resp.data;
					for(var i = 0; i < self.pager.content.length; i++) {
						bindRoleNames(self.pager.content[i]);
					}
					/**
					 * 用户的角色是一个对象，为了在页面显示出来，所以再为用户模型绑上字符串的角色数组
					 */
					function bindRoleNames(user) {
						user.roleNames = [];
						for(var i = 0; i < user.roles.length; i++) {
							user.roleNames.push(user.roles[i].name);
						}
					}
				});
			}
			self.query();
			self.btnClick = function(pageNumber) {
				self.params.page = pageNumber;
				self.query();
			};
			self.reset = function() {
				self.params = {
					page: 0,
					size: 10,
					email: '',
					roles: []
				};
			};

			self.modal = {
				success: {
					open: false
				},
				fail: {
					open: false
				},
			};
			self.onChange = function(id, value) {
				userService.grantRoles(id, value).then(function(respose) {
					self.modal.success.open = true;
				}, function(respose) {
					self.query();
					self.modal.fail.open = true;
				});
			};
		}]);
});