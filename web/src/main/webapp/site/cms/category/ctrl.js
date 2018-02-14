define(['cms/module', 'cms/category/service'], function(cmsModule) {
	return cmsModule.controller('CategoryCtrl', ['$scope', '$http', '$state', 'categoryService',
	                                function($scope, $http, $state, service) {
		var self = this;
		$scope.getAuthentication();
		getTypes();
		// 用于切换界面，详情状态就是新增和编辑，反之则是列表页面
		self.isDetail = false;
		self.form = {};
		self.queryParam = {
			page : 0,
			name : null,
		};
		
		self.query = function() {
			service.getTypePage(self.queryParam.name, self.queryParam.page).then(function(resp) {
				self.page = resp.data;
				self.isDetail = false;
			});
		};
		
		self.query();
		
		self.btnClick = function(pageNumber) {
			self.query.page = pageNumber;
			self.query();
		};
		
		self.validation = function(exist) {
			$scope.f.name.$setValidity('notexist', !exist);
			$scope.$apply();
		};
		
		self.add = function() {
			self.form = {};
			self.ignoreName = null;
			self.isDetail = true;
		};
		
		self.edit = function(id) {
			service.findTypeById(id).then(function(resp) {
				self.form = resp.data;
				self.ignoreName = resp.data.name;
				if (self.form.parent) {
					self.form.parentId = self.form.parent.id;
				}
				self.isDetail = true;
			});
		};
		
		self.back = function() {
			self.isDetail = false;
		};
		
		self.submit = function() {
			if (self.form.id) {
				service.updateType(self.form.id, self.form).then(function(resp) {
					self.query();
					getTypes();
					self.isDetail = false;
				});
			} else {
				service.saveType(self.form).then(function(resp) {
					self.query();
					getTypes();
					self.isDetail = false;
				});
			}
		};

		self['delete'] = function() {
			if (!self.form.id)
				return;
			if (confirm('确定删除' + self.form.name + '吗？')) {
				service.deleteType(self.form.id).then(function(resp) {
					self.query();
					getTypes();
				});
			}
		};
		
		function getTypes() {
			service.getTypes().then(function(resp) {
				self.types = resp.data;
			});
		}
	}])
	;
});