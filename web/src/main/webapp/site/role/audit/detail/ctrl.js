define(['role/module', 'role/service'], function(roleModule) {
	return roleModule
		.controller('RoleAuditDetail', ['$scope', '$http', '$state', 'roleService',
			function($scope, $http, $state, service) {
				var self = this;
				self.id = $state.params.id;
				self.revision = $state.params.revision;

				function roleAtRevision(roleId, revision) {
					service.roleAtRevision(roleId, revision).then(function(resp) {
						self.detail = resp.data;
					});
				}
				roleAtRevision(self.id, self.revision);

				/**
				 * 在详情中展示字符串，有的值是对象，所以需要处理
				 */
				self.getValue = function(k, v) {
					var result, i, j, temp /*, auth*/ ;
					return result;
				};
			}
		]);
});