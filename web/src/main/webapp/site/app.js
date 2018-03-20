define([
	'angular', 'i18n', 'WebsocketEndpoint', 'toastr', 
	'angular-animate', 'angular-cookies', 'angular-touch', 'ui-router', 
	'angular-datepicker', 'ng-verify', 'angular-translate',
	'common/context',
	'role/context',
	'employee/context',
	'customer/context',
	'cms/context',
	'encryption/context',
	'flow/context',
	'dashboard/context',
], function(angular, i18n, WebsocketEndpoint, toastr) {
	return angular.module('app', [
			'ui.router', 'ngAnimate', 'ngCookies', 'ngTouch', 'pascalprecht.translate',
			'commonModule',
			'roleModule',
			'employeeModule',
			'customerModule',
			'cmsModule',
			'encryptionModule',
			'flowModule',
			'dashboardModule',
		])
		.run(['$rootScope', '$state', '$stateParams', '$http', '$translate', 'util', function($rootScope, $state, $stateParams, $http, $translate, util) {
			// 让页面能同步状态，显示出该状态应有的效果，例如某菜单被激活的样式
			$rootScope.$state = $state;
			$rootScope.$stateParams = $stateParams;
			
			// websocket
			var SECURITY_CODE = "abcdefg0123456789";
			var site = util.getRootName() + '/websocket/';
			var isHttps = window.location.protocol == 'https:' ? true : false;
			var url = (isHttps ? 'wss://' : 'ws://') + window.location.host + site + SECURITY_CODE;
			$rootScope.websocketEndpoint = new WebsocketEndpoint(url);
			$rootScope.websocketEndpoint.addListener('flowNotify', function(message) {
				toastr.info(message.data && message.data.content);
			});
			
			// 获取当前用户的认证信息，页面可以直接通过{{authentication.username}}获取用户名
			$rootScope.getAuthentication = function(callback) {
				var promise = {
					then: function(fun) {
						this.fun = fun;
					}
				};
				$http.get('authentication').then(function(resp) {
					var data = resp.data;
					console.log('authentication:')
					console.log(data);
					$rootScope.authentication = data;
					if ($rootScope.websocketEndpoint.isOpen()) {// 反向刷新websocket endpoint中的userId
						var msg = JSON.stringify({
				    		messageType : 'refreshUserId',
				    		data : $rootScope.getUserId(),
				    	});
						$rootScope.websocketEndpoint.send(msg);
					} else {
						toastr.error('websocket disconnect, try reopen');
						$rootScope.websocketEndpoint = new WebsocketEndpoint(url);
					}
					if(callback) {
						callback(data);
					}
					if(promise.fun instanceof Function) {
						promise.fun(data);
					}
				})
				['catch'](function(resp) {
					
				})
				['finally'](function() {
					
				})
				;
				return promise;
			};
			$rootScope.getAuthentication();
			// 判断是否有此权限
			$rootScope.hasAuthority = function(/*authorities*/) {
				var authorities, i, j;
				authorities = $rootScope.authentication &&
					$rootScope.authentication.principal &&
					$rootScope.authentication.principal.authorities;
				if (authorities) {
					for (i = 0; i < authorities.length; i++) {
						for (j = 0; j < arguments.length; j++) {
							if (authorities[i].authority == arguments[j]) {
								return true;
							}
						}
					}
				}
				return false;
			};
			// 判断是否登录
			$rootScope.isAuthenticated = function() {
				return $rootScope.authentication &&
					$rootScope.authentication.principal &&
					$rootScope.authentication.principal.username;
			};
			// 获取当前userId
			$rootScope.getUserId = function() {
				return $rootScope.authentication &&
					$rootScope.authentication.principal &&
					$rootScope.authentication.principal.id;
			};

			// 个人资料
			$rootScope.profile = function() {
				if (!$rootScope.authentication || !$rootScope.authentication.principal) {
					return;
				}
				if ($rootScope.authentication.principal.userType === 'Employee') {
					$state.go('employee.detail', {id : $rootScope.authentication.principal.id});
				} else if ($rootScope.authentication.principal.userType === 'Customer') {
					$state.go('customer.detail', {id : $rootScope.authentication.principal.id});
				}
			};
			// 注销
			$rootScope.logout = function() {
				$http.post('logout').then(function(resp) {
					location.replace('login');
				});
			};

			// 进入全文搜索
			$('form[name="fulltextsearch"]').on('submit', function(e) {
				e.preventDefault();
				var query = $(this).find('input[name="search"]').val();
				$state.go('cms.resource', {query:query}, {
					reload: true
				});
			});

			// 获取图片信息
			$rootScope.getIconSrc = function() {
				var iconSrc = $rootScope.authentication && $rootScope.authentication.principal && $rootScope.authentication.principal.iconSrc;
				if(!iconSrc)
					iconSrc = 'lib/adminLTE/img/user2-160x160.jpg';
				return iconSrc;
			};
			$rootScope.getUserName = function() {
				var name = $rootScope.authentication && $rootScope.authentication.principal && $rootScope.authentication.principal.realName;
				if(!name)
					name = 'Alexander Pierce';
				return name;
			};
			
			$http.get('presetData').then(function(resp) {
				$rootScope.presetData = resp.data;
				console.log(resp.data);
			});
			
			var date = new Date();
			$rootScope._date = {
				year: date.getFullYear(),
				month: date.getMonth() + 1,
				day: date.getDate()
			};
			$rootScope.changeLang = function(langKey) {
				if (langKey) {
					$translate.use(langKey);
				} else {
					var lang = $translate.proposedLanguage();
					if (lang.startsWith('zh')) {
						$translate.use('en');
					} else {
						$translate.use('zh');
					}
				}
			};
			$rootScope.currentLanguage = function() {
				return $translate.proposedLanguage();
			};
		}])
		.animation('.pop', ["$animateCss", function($animateCss) {
			return {
				enter: function(element) {
					return $animateCss(element, {
						from: {
							opacity: 0
						},
						to: {
							opacity: 1
						},
						duration: 0.8
					});
				},
				leave: function(element) {
					return $animateCss(element, {
						from: {
							opacity: 1
						},
						to: {
							opacity: 0
						},
						duration: 0.8
					});
				}
			}
		}])
		.config(function($stateProvider, $urlRouterProvider) {
			$urlRouterProvider.otherwise('/dashboard');
		})
		// withCredentials是一个设置在底层 XMLHttpRequest(AJAX)对象的标记，可以跨站访问时携带cookie
		.config(function($httpProvider) {
			$httpProvider.defaults.withCredentials = true;
		})
		.config(['$translateProvider', function ($translateProvider) {
			for (var p in i18n) {
				if (!i18n.hasOwnProperty(p)) {
					continue;
				}
				$translateProvider.translations(p, i18n[p]);
			}
			$translateProvider.preferredLanguage('zh');
			// 浏览器默认语言
//			$translateProvider.determinePreferredLanguage();
		}])
		;
});