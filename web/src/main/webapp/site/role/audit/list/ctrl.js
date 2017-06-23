define(['role/module', 'role/service'], function(roleModule) {
	return roleModule
		.controller('RoleAuditList', ['$scope', '$http', '$state', 'roleService', function($scope, $http, $state, service) {
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
				page: 1,
				size: 5,
				name: '',
			};

			function roleRevision() {
				service.roleRevision(self.params).then(function(resp) {
					self.pager = resp.data;
				});
			}
			roleRevision();
			self.query = function() {
				roleRevision();
			};
			self.btnClick = function(pageNumber) {
				self.params.page = pageNumber;
				roleRevision();
			};
			self.reset = function() {
				self.params = {
					page: 1,
					size: 10,
					name: '',
				};
			};
			self.detail = function(id, revision) {
				$state.go('roleAuthCfgAudit.detail', {
					id: id,
					revision: revision
				}, {
					reload: true
				});
			};

		}]);
});