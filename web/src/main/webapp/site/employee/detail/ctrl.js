define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeDetail', ['$scope', '$http', '$state', 'employeeService', 'roleService', 'util', function($scope, $http, $state, service, roleService, util) {
			var self = this;
			$scope.getAuthentication();
			roleService.getRoles().then(function(resp) {
				self.roles = resp.data;
			});
			self.getDetail = function(id) {
				service.get(id).then(function(resp) {
					self.detail = resp.data;
				});
			};
			self.getDetail($state.params.id);
			self.whenDone = function() {
				self.getDetail($state.params.id);
			}
			self.dictionary = {
				'ADMIN' : '系统管理员',
				'EMPLOYEE' : '职员',
				'MANAGER' : '经理',
				'COOPERATE': '合作人',
				'CONSUMER': '消费者',
				'CONSIGNOR': '甲方',
				'MALE': '男',
				'FEMALE': '女',
				'UNSPECIFIED': '未知',
			};

			$('input[name="icon"]').on('change', function(e) {
				$('#submit-file').attr('disabled', null);
			});
			
			self.editable = function() {
				return $scope.presetData && !($scope.presetData.user_bot_id == $state.params.id
						|| $scope.presetData.user_admin_id == $state.params.id
						|| $scope.presetData.user_anonymous_id == $state.params.id);
			};
			// 异步校验邮箱
			self.emailValidation = function(exist) {
				$scope.f.email.$setValidity('notexist', !exist);
				$scope.$apply();
			};
			// 异步校验手机号
			self.cellPhoneValidation = function(exist) {
				$scope.f.cellPhone.$setValidity('notexist', !exist);
				$scope.$apply();
			};
			
			self.openModal = function(id, $event) {
				self.modal.open = true;
				for (var k = 0; k < self.roles.length; k++) {
					self.roles[k].selected = false;
				}
				for (var i = 0; i < self.detail.roles.length; i++) {
					for (var j = 0; j < self.roles.length; j++) {
						if (self.roles[j].name == self.detail.roles[i].name) {
							self.roles[j].selected = true;
						}
					}
				}
			}
			self.modal = {
				open: false,
				title: '角色',
				type: '',
				whenConfirm: function() {
					var i, roleNames = [];
					for (i = 0; i < self.roles.length; i++) {
						if (self.roles[i].selected) {
							roleNames.push(self.roles[i].name);
						}
					}
					service.grandRoles(self.detail.id, roleNames).then(function() {
						self.getDetail($state.params.id);
					});
				},
			};
		}])
		.filter('rolepipe', function() {
			return function(v) {
				if (! (v instanceof Array)) {
					return '';
				}
				temp = [];
				for(i = 0; i < v.length; i++) {
					temp.push(v[i].name);
				}
				return temp.join(',');
			}
		})
		;
});