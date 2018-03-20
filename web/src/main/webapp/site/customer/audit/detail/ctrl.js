define(['customer/module', 'customer/service'], function(customerModule) {
	return customerModule
		.controller('CustomerAuditDetail', ['$scope', '$http', '$state', 'customerService',
			function($scope, $http, $state, service) {
				var self = this;
				$scope.getAuthentication();
				self.id = $state.params.id;
				self.revision = $state.params.revision;

				function getAtRevision(roleId, revision) {
					service.getAtRevision(roleId, revision).then(function(resp) {
						self.detail = resp.data;
					});
				}
				getAtRevision(self.id, self.revision);

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
			}
		]);
});