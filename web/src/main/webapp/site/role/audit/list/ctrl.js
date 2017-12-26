define(['role/module', 'role/service'], function(roleModule) {
	return roleModule
		.controller('RoleAuditList', ['$scope', '$http', '$state', 'roleService', function($scope, $http, $state, service) {
			var self = this;
			self.id = $state.params.id;
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
			service.getRoleRevision(self.id).then(function(resp) {
				self.list = resp.data;
			});
			
			self.detail = function(id, revision) {
				$state.go('roleAudit.detail', {id:id, revision:revision}, { reload : true });
			};
		}]);
});