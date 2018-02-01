/**
 * 基于angularjs的国际化方案
 */
define([], function() {
	return {
		zh : {
			'date': '{{year}}年{{month}}月{{day}}日',
			'home': '首页',
			'forum': '论坛',
			'flow': '流程',
			'content': '内容管理',
			'keys': '密码管理',
			'user': '用户管理',
			'user-platform' : '平台账号',
			'user-customer' : '客户账号',
			'user-audit' : '账号审计',
			'role': '角色权限关系',
			'role-config': '配置',
			'role-audit': '审计',
			'resource': '资源',
		},
		en : {
			'date': '{{month}}/{{day}}/{{year}}',
			'home': 'home',
			'forum': 'forum',
			'flow': 'flow',
			'content': 'content',
			'keys': 'keys',
			'user': 'user',
			'user-platform' : 'platform',
			'user-customer' : 'customer',
			'user-audit' : 'audit',
			'role': 'role',
			'role-config': 'config',
			'role-audit': 'audit',
			'resource': 'resource',
		},
	};
});