define(['customer/module', 'customer/service'], function(customerModule) {
	return customerModule
		.controller('CustomerAdd', ['$scope', '$http', '$state', 'customerService', function($scope, $http, $state, service) {
			var self = this;
			$scope.getAuthentication();
			self.form = {};

			self.submit = function() {
				service.create(self.form).then(function(resp) {
					$state.go('customer.list', {}, {
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
			// 异步校验证件号
			self.IDCardValidate = function(exist, vaild) {
				$scope.addUserform.identification.$setValidity('notexist', !exist);
				$scope.addUserform.identification.$setValidity('pattern', vaild);
				$scope.$apply();
			};
		}]);
});