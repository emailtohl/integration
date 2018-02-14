define(['customer/module', 'customer/service'], function(customerModule) {
	return customerModule
		.controller('CustomerDetail', ['$scope', '$http', '$state', 'customerService', 'roleService', 'util', function($scope, $http, $state, service, roleService, util) {
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
			/**
			 * 在详情中展示字符串，有的值是对象，所以需要处理
			 */
			self.getValue = function(k, v) {
				var result, i, j, temp, auth;
				switch(k) {
					case 'enabled':
						result = v ? '是' : '否';
						break;
					case 'accountNonExpired':
						result = v ? '是' : '否';
						break;
					case 'credentialsNonExpired':
						result = v ? '是' : '否';
						break;
					case 'accountNonLocked':
						result = v ? '是' : '否';
						break;
					case 'image':
						result = v && v.filename;
						break;
					case 'department':
						result = v && v.name;
						break;
					case 'address':
						result = v.city + ' ' + v.street + ' ' + v.zipcode;
						break;
					case 'gender':
						result = self.dictionary[v];
						break;
					case 'classify':
						result = self.dictionary[v];
						break;
					case 'roles':
						temp = [];
						for(i = 0; i < v.length; i++) {
							temp.push(v[i].name);
						}
						return temp.join(',');
						break;
					default:
						result = v;
						break;
				}
				return result;
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
		}]);
});