define(['role/module', 'role/service'], function(roleModule) {
	return roleModule
		.controller('RoleConfig', ['$scope', '$http', '$state', 'roleService', function($scope, $http, $state, roleService) {
			var self = this;
			$scope.getAuthentication();
			self.form = {}; // 要提交的表单数据
			function getRoles() {
				roleService.getRoles().then(function(resp) {
					self.roles = resp.data;
				});
			}

			function getAuthorities() {
				roleService.getAuthorities().then(function(resp) {
					var data = resp.data;
					self.authorities = data;
					// 创建一个以权限名为key，权限对象为value的map
					self.authMap = {};
					for(var i = 0; i < data.length; i++) {
						self.authMap[data[i].name] = data[i];
					}
				});
			}
			/**
			 * 在self.authMap中找到被选中的权限名数组
			 */
			function getAuthorityNames() {
				var authorityNames = [],
					p;
				for(p in self.authMap) {
					if(self.authMap.hasOwnProperty(p) && self.authMap[p].selected) {
						authorityNames.push(self.authMap[p].name);
					}
				}
				return authorityNames;
			}
			/**
			 * 清空self.authMap的被选属性
			 */
			function clearAuthMapSelected() {
				for(var p in self.authMap) {
					if(self.authMap.hasOwnProperty(p)) {
						self.authMap[p].selected = false;
					}
				}
			}

			getRoles();
			getAuthorities();

			self.modal = {
				open: false,
				title: '角色属性',
				type: '',
				whenConfirm: function() {
					if(self.form.id) { // 有id的是编辑
						self.form.authorityNames = getAuthorityNames();
						roleService.updateRole(self.form.id, self.form).then(function(resp) {
							getRoles();
						});
					} else { // 没有id的是新增
						self.form.authorityNames = getAuthorityNames();
						roleService.createRole(self.form).then(function(resp) {
							getRoles();
						});
					}
				},
			};
			self.openModal = function(id) {
				clearAuthMapSelected(); // 先清理self.authMap中的被属性
				if(id) { // 如果是编辑
					roleService.getRole(id).then(function(resp) {
						var data = resp.data;
						self.form = data;
						for(var i = 0; i < data.authorities.length; i++) {
							// 根据权限名查询权限对象，然后将其被选属性改为true
							self.authMap[data.authorities[i].name].selected = true;
						}
					});
				} else { // 否则是新增
					self.form = {
						name: null,
						description: null,
						authorityNames: []
					};
				}
				self.modal.open = true;
			}

			self['delete'] = function(id, name, $event) {
				$event.stopPropagation();
				if(confirm('确定删除“' + name + '”角色吗？')) {
					roleService.deleteRole(id).then(function(resp) {
						getRoles();
					});
				}
			};
		}])
});