define(['role/module', 'common/context'], function(roleModule) {
	return roleModule.factory('roleService', ['$http', 'util', function($http, util) {
		return {
			getRole: function(id) {
				return $http.get('role/' + id);
			},
			getRoles: function() {
				return $http.get('role');
			},
			getAuthorities: function() {
				return $http.get('authority');
			},
			createRole: function(role) {
				return $http.post('role', role);
			},
			updateRole: function(id, role) {
				return $http.put('role/' + id, role);
			},
			deleteRole: function(id) {
				return $http['delete']('role/' + id);
			},
			getRoleRevision: function(id) {
				return $http.get('audit/role/' + id);
			},
			getRoleAtRevision: function(roleId, revision) {
				if(!roleId || !revision) {
					throw new ReferenceError('角色Id和修订版本号revision都不能为空');
				}
				return $http.get('audit/role/' + roleId + '/revision/' + revision);
			},
		};
	}]);
});