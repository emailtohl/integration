define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeAdd', ['$scope', '$http', '$state', 'employeeService', function($scope, $http, $state, service) {
			var self = this;
			$scope.getAuthentication();

			self.asynvalidation = function(exist) {
				console.log(exist);
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