define(['employee/module', 'common/context'], function(employeeModule) {
	return employeeModule.factory('employeeService', ['$http', 'util', function($http, util) {
		return {
			create: function(emp) {
				return $http.post('employee', emp);
			},
			get: function(id) {
				return $http.get('employee/' + id);
			},
			getByNum: function(empNum) {
				return $http.get('employee/empNum/' + empNum);
			},
			page: function(params) {
				return $http.get('employee/page?' + util.encodeUrlParams(params));
			},
			update: function(id, emp) {
				return $http.put('employee/' + id, emp);
			},
			'delete': function(id) {
				return $http['delete']('employee/' + id);
			},
			search: function(params) {
				return $http.get('employee/search?' + util.encodeUrlParams(params));
			},
			grandRoles: function(id, roleNames) {
				return $http.post('employee/grandRoles', {
					id : id,
					roleNames : roleNames
				});
			},
			updatePassword: function(empNum, oldPassword, newPassword) {
				return $http.post('employee/updatePassword', {
					empNum : empNum,
					oldPassword : oldPassword,
					newPassword : newPassword
				});
			},
			resetPassword: function(id) {
				return $http.post('employee/resetPassword', {
					id : id
				});
			},
			enabled: function(id, enabled) {
				return $http.post('employee/enabled', {
					id : id,
					enabled : enabled
				});
			},
			getRevision: function(id) {
				return $http.get('audit/employee/' + id);
			},
			getAtRevision: function(id, revision) {
				if(!id || !revision) {
					throw new ReferenceError('empId和修订版本号revision都不能为空');
				}
				return $http.get('audit/employee/' + id + '/revision/' + revision);
			},
			
			
			custCreate: function(cust) {
				return $http.post('customer', cust);
			},
			custGet: function(id) {
				return $http.get('customer/' + id);
			},
			custGetByCellPhoneOrEmail: function(cellPhoneOrEmail) {
				return $http.get('customer/cellPhoneOrEmail/' + cellPhoneOrEmail);
			},
			custPage: function(params) {
				return $http.get('customer/page?' + util.encodeUrlParams(params));
			},
			custUpdate: function(id, cust) {
				return $http.put('customer/' + id, cust);
			},
			custDelete: function(id) {
				return $http['delete']('customer/' + id);
			},
			custGrandRoles: function(id, roleNames) {
				return $http.post('customer/grandRoles', {
					id : id,
					roleNames : roleNames
				});
			},
			custResetPassword: function(id) {
				return $http.post('customer/resetPassword', {
					id : id
				});
			},
			custEnabled: function(id, enabled) {
				return $http.post('customer/enabled', {
					id : id,
					enabled : enabled
				});
			},
			custGetRevision: function(id) {
				return $http.get('audit/customer/' + id);
			},
			custGetAtRevision: function(id, revision) {
				if(!id || !revision) {
					throw new ReferenceError('customerId和修订版本号revision都不能为空');
				}
				return $http.get('audit/customer/' + id + '/revision/' + revision);
			},
			
		};
	}]);
});