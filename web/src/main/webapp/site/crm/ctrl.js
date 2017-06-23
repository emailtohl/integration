define(['crm/module', 'crm/service'], function(crmModule) {
	return crmModule
	.controller('CrmCtrl', [ '$scope', '$http', '$state', 'crmService'
	                         , function($scope, $http, $state, crmService) {
		const init = JSON.stringify({
			page : 1,
			name : '',
			title : '',
			affiliation : ''
		});
		var self = this;
		$scope.getAuthentication();
		self.form = JSON.parse(init);
		self.customer = {};
		function query() {
			crmService.query(self.form).then(function(resp) {
				self.pager = resp.data;
			});
		}
		query();
		self.query = function() {
			query();
		};
		self.btnClick = function(pageNumber) {
			self.form.page = pageNumber;
			query();
		};
		self.reset = function() {
			self.form = JSON.parse(init);
		};
		self.openModal = function(id) {
			if (id) {// 有id表示编辑
				crmService.get(id).then(function(resp) {
					self.customer = resp.data;
				});
			} else {// 否则就是新增
				self.customer = {};
			}
			self.modal.open = true;
		};
		self.modal = {
			open : false,
			title : '客户信息',
			type : '',
			whenConfirm : function() {
				if (self.customer.id) {
					crmService.update(self.customer).then(function(resp) {
						query();
					});
				} else {
					crmService.add(self.customer).then(function(resp) {
						query();
					});
				}
			},
		};
		self.download = function() {
			crmService.download();
		};
	}]);
});