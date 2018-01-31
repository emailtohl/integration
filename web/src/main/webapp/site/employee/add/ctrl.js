define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeAdd', ['$scope', '$http', '$state', 'employeeService', function($scope, $http, $state, service) {
			var self = this;
			$scope.getAuthentication();
			self.form = {};

			self.submit = function() {
				service.create(self.form).then(function(resp) {
					$state.go('user.list', {}, {
						reload: true
					});
				});
			};
			// 异步校验邮箱
			self.emailValidation = function(exist) {
				$scope.addUserform.email.$setValidity('notexist', !exist);
				$scope.$apply();
			};
			// 异步校验手机号
			self.cellPhoneValidation = function(exist) {
				$scope.addUserform.cellPhone.$setValidity('notexist', !exist);
				$scope.$apply();
			};
		}]);
});