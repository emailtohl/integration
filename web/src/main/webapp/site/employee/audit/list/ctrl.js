define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeAuditList', ['$scope', '$http', '$state', 'employeeService', function($scope, $http, $state, service) {
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
				$state.go('employeeAudit.detail', {id:id, revision:revision}, { reload : true });
			};
		}]);
});