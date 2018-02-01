define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeAuditList', ['$scope', '$http', '$state', 'employeeService', function($scope, $http, $state, service) {
			var self = this;
			$scope.getAuthentication();
			self.id = $state.params.id;
			self.typeMap = {
				ADD: '新增',
				MOD: '修改',
				DEL: '删除'
			};
			service.getRevision(self.id).then(function(resp) {
				self.list = resp.data;
			});
			
			self.detail = function(id, revision) {
				$state.go('employee.audit.detail', {id:id, revision:revision}, { reload : true });
			};
		}]);
});