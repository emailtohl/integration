define(['user/module', 'user/service'], function(userModule) {
	return userModule
		.filter('join', function() {
			return function(data) {
				return data.join('， ');
			};
		})
		.controller('UserAuditList', ['$scope', '$http', '$state', 'userService', function($scope, $http, $state, userService) {
			var self = this;
			self.roleMap = {
				admin: '管理员',
				manager: '经理',
				employee: '雇员',
				user: '用户'
			};
			self.typeMap = {
				ADD: '新增',
				MOD: '修改',
				DEL: '删除'
			};
			$scope.getAuthentication();
			self.params = {
				page: 0,
				size: 10,
				email: '',
			};

			function userRevision() {
				userService.userRevision(self.params).then(function(resp) {
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
							user.roleNames.push(self.roleMap[user.roles[i].name]);
						}
					}
				});
			}
			userRevision();
			self.query = function() {
				userRevision();
			};
			self.btnClick = function(pageNumber) {
				self.params.page = pageNumber;
				userRevision();
			};
			self.reset = function() {
				self.params = {
					page: 0,
					size: 10,
					email: '',
				};
			};
			self.detail = function(id, revision) {
				$state.go('user.audit.detail', {
					id: id,
					revision: revision
				}, {
					reload: true
				});
			};

		}]);
});