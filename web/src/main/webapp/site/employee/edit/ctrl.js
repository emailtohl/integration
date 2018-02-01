define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeEdit', ['$scope', '$http', '$state', 'employeeService', 'roleService', function($scope, $http, $state, service, roleService) {
			var self = this;
			$scope.getAuthentication();
			self.form = {};
			
			roleService.getRoles().then(function(resp) {
				self.roles = resp.data;
				service.get($state.params.id).then(function(resp) {
					self.form = resp.data;
					self.ignoreCellPhone = self.form.cellPhone;
					self.ignoreEmail = self.form.email;
				});
			});
			
			self.submit = function() {
				service.update(self.form.id, self.form).then(function(resp) {
					console.log(resp.data && resp.data.id);
					$state.go('employee.detail', {id : self.form.id}, {
						reload: true
					});
				});
			};
			// 异步校验邮箱
			self.emailValidation = function(exist) {
				$scope.editUserform.email.$setValidity('notexist', !exist);
				$scope.$apply();
			};
			// 异步校验手机号
			self.cellPhoneValidation = function(exist) {
				$scope.editUserform.cellPhone.$setValidity('notexist', !exist);
				$scope.$apply();
			};
		}]);
});