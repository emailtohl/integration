define(['customer/module', 'common/context'], function(customerModule) {
	return customerModule.factory('customerService', ['$http', 'util', function($http, util) {
		return {
			create: function(cust) {
				return $http.post('customer', cust);
			},
			get: function(id) {
				return $http.get('customer/' + id);
			},
			getByCellPhoneOrEmail: function(cellPhoneOrEmail) {
				return $http.get('customer/cellPhoneOrEmail/' + cellPhoneOrEmail);
			},
			page: function(params) {
				return $http.get('customer/page?' + util.encodeUrlParams(params));
			},
			update: function(id, cust) {
				return $http.put('customer/' + id, cust);
			},
			'delete': function(id) {
				return $http['delete']('customer/' + id);
			},
			search: function(params) {
				return $http.get('customer/search?' + util.encodeUrlParams(params));
			},
			grandRoles: function(id, roleNames) {
				return $http.post('customer/grandRoles', {
					id : id,
					roleNames : roleNames
				});
			},
			resetPassword: function(id) {
				return $http.post('customer/resetPassword', {
					id : id
				});
			},
			enabled: function(id, enabled) {
				return $http.post('customer/enabled', {
					id : id,
					enabled : enabled
				});
			},
			getRevision: function(id) {
				return $http.get('audit/customer/' + id);
			},
			getAtRevision: function(id, revision) {
				if(!id || !revision) {
					throw new ReferenceError('customerId和修订版本号revision都不能为空');
				}
				return $http.get('audit/customer/' + id + '/revision/' + revision);
			},
			
		};
	}]);
});