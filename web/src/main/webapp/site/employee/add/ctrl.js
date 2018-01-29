define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeAdd', ['$scope', '$http', '$state', 'employeeService', function($scope, $http, $state, service) {
			var self = this;
			$scope.getAuthentication();

			self.emailValidation = function(exist) {
				$scope.addUserform.email.$setValidity('exist', exist);
			};
			self.cellPhoneValidation = function(exist) {
				$scope.addUserform.cellPhone.$setValidity('exist', exist);
			};
			
			self.form = {};

			self.submit = function() {
				service.add(self.form).then(function(resp) {
					console.log('确认')
					$state.go('user.list', {}, {
						reload: true
					});
				});
			};

		}]);
});