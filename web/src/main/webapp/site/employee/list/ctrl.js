define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeList', ['$scope', '$http', '$state', 'employeeService', function($scope, $http, $state, service) {
			var self = this;
			$scope.getAuthentication();
			self.params = {
				query: '',
				page: 0,
				size: 5,
			};
			self.query = function() {
				service.search(self.params).then(function(resp) {
					self.page = resp.data;
					/*if (self.page && self.page.content instanceof Array) {
						for (var i = 0; i < self.page.content.length; i++) {
							if (!self.page.content[i]) {
								self.page.content.splice(i, 1);
							}
						}
					}*/
				});
			};
			self.reset = function() {
				self.params = {
					query: '',
					page: 0,
					size: 5,
				};
			};
			self.btnClick = function(pageNumber) {
				self.params.page = pageNumber;
				self.query();
			};

			self['delete'] = function(id) {
				if(confirm('确认删除吗？')) {
					service['delete'](id).then(function(resp) {
						self.query();
					});
				}
			};
			// 查询
			self.query();

			self.isPresetUserId = function(id) {
				return $scope.presetData && ($scope.presetData.user_bot_id === id
						|| $scope.presetData.user_admin_id === id
						|| $scope.presetData.user_anonymous_id === id);
			};
		}]);
});