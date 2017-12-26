define(['employee/module', 'employee/service'], function(employeeModule) {
	return employeeModule
		.controller('EmployeeDetail', ['$scope', '$http', '$state', 'employeeService', 'roleService', 'util', function($scope, $http, $state, service, roleService, util) {
			var self = this;
			self.form = {
				roleNames : [],
				department : {},
			}; // 要提交的表单数据
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
				setTimeout(function() {
					self.getDetail($state.params.id);
					$scope.getAuthentication();
				}, 1000);
			};
			self.dictionary = {
				// 'ADMIN' : '系统管理员',
				// 'EMPLOYEE' : '职员',
				// 'MANAGER' : '经理',
				'USER': '普通用户',
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
					case 'subsidiary':
						result = v.country + ' ' + v.province + ' ' + v.city;
						break;
					case 'gender':
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

			self.modal = {
				open: false,
				title: '编辑用户信息',
				type: '',
				whenConfirm: function() {
					self.form.roles = [];
					for (var i = 0; i < self.form.roleNames.length; i++) {
						self.form.roles.push({name : self.form.roleNames[i]});
					}
					service.update(self.form.id, self.form).then(function(resp) {
						// $state.go('user.detail', { id : self.form.id }, { reload : true });
						self.getDetail(self.form.id);
					});
				},
			};
			self.edit = function() {
				self.form = util.clone(self.detail);
				self.form.roleNames = [];
				for (var i = 0; i < self.detail.roles.length; i++) {
					self.form.roleNames.push(self.detail.roles[i].name);
				}
				self.modal.open = true;
			};

			self.enableUser = function() {
				var id = self.detail.id;
				if(!id) {
					return;
				}
				service.enableUser(id).then(function(resp) {
					self.getDetail(id);
				});
			};
			self.disableUser = function() {
				var id = self.detail.id;
				if(!id) {
					return;
				}
				service.disableUser(id).then(function(resp) {
					self.getDetail(id);
				});
			};
			$('input[name="icon"]').on('change', function(e) {
				$('#submit-file').attr('disabled', null);
			});
		}]);
});