define(['user/module', 'user/service'], function(userModule) {
	return userModule
		.controller('UserAdd', ['$scope', '$http', '$state', 'userService', function($scope, $http, $state, userService) {
			var self = this;
			$scope.getAuthentication();

			self.form = {};

			self.submit = function() {
				userService.addUser(self.form).then(function(resp) {
					console.log('确认')
					$state.go('user.list', {}, {
						reload: true
					});
				});
			};

		}]);
});