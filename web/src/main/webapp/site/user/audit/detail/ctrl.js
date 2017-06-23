define(['user/module', 'user/service'], function(userModule) {
	return userModule
		.controller('UserAuditDetail', ['$scope', '$http', '$state', 'userService', function($scope, $http, $state, userService) {
			var self = this;
			self.id = $state.params.id;
			self.revision = $state.params.revision;
			self.dictionary = {
				// 'ADMIN' : '系统管理员',
				// 'EMPLOYEE' : '职员',
				// 'MANAGER' : '经理',
				'USER': '普通用户',
				'MALE': '男',
				'FEMALE': '女',
				'UNSPECIFIED': '未知',
			};

			function userAtRevision(userId, revision) {
				userService.userAtRevision(userId, revision).then(function(resp) {
					self.detail = resp.data;
				});
			}
			userAtRevision(self.id, self.revision);

			/**
			 * 在详情中展示字符串，有的值是对象，所以需要处理
			 */
			self.getValue = function(k, v) {
				var result, i, j, temp /*, auth*/ ;
				switch(k) {
					case 'department':
						result = v.name;
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
							for(j = 0; j < v[i].authorities.length; j++) {
								auth = v[i].authorities[j];
								if(auth instanceof String) {
									temp.push(auth);
								} else {
									temp.push(auth.name);
								}
								result = temp.join('，');
							}
						}
						break;
					default:
						result = v;
						break;
				}
				return result;
			};
		}]);
});