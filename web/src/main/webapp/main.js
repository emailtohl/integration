/**
 * Created by helei on 2017/5/17.
 */
requirejs.config({
	//By default load any module IDs from js/lib
	baseUrl: 'site',
	//except, if the module ID starts with "app",
	//load it from the js/app directory. paths
	//config is relative to the baseUrl, and
	//never includes a ".js" extension since
	//the paths config could be for a directory.
	paths: {
		lib: '../lib',
		common: '../common',

		jquery: '../lib/jquery/jquery-3.2.1.min',
		'jquery-ui': '../lib/jquery/jquery-ui.min',
		angular: '../lib/angular/angular.min',
		'angular-animate': '../lib/angular/angular-animate.min',
		'angular-cookies': '../lib/angular/angular-cookies.min',
		'angular-touch': '../lib/angular/angular-touch.min',
		'ui-router': '../lib/ui/router/angular-ui-router.min',
		'ui-select': '../lib/ui/select/select.min',
		bootstrap: '../lib/bootstrap/js/bootstrap.min',
		'jquery-slimscroll': '../lib/assets/js/jquery.slimscroll.min',
		fastclick: '../lib/assets/js/fastclick.min',
		adminlte: '../lib/adminLTE/js/adminlte.min',
		'adminlte-app': '../lib/adminLTE/js/app.min',
		select2: '../lib/select2/select2.full.min',
		ztree: '../lib/ztree/jquery.ztree.all.min',
		'ckeditor': '../lib/ckeditor/ckeditor',
		'ckeditorConfig': '../lib/ckeditor/config',
		'moment': '../lib/moment/moment',
	},
	shim: {
		// 他们都遵循AMD规范，所以不必声明
		jquery: {
			exports: '$'
		},
		'jquery-ui': {
			deps: ['jquery'],
			exports: 'ui',
		},
		angular: {
			exports: 'angular',
		},
		'angular-animate': {
			deps: ['angular'],
			exports: 'animate',
		},
		'angular-cookies': {
			deps: ['angular'],
			exports: 'cookies',
		},
		'angular-touch': {
			deps: ['angular'],
			exports: 'touch',
		},
		'ui-router': {
			deps: ['angular'],
			exports: 'uiRouter',
		},
		bootstrap: {
			deps: ['jquery'],
			exports: 'bootstrap'
		},
		'jquery-slimscroll': {
			deps: ['jquery'],
			exports: 'jquerySlimscroll'
		},
		fastclick: {
			deps: ['jquery-slimscroll', 'bootstrap'],
			exports: 'fastclick'
		},
		adminlte: {
			deps: ['fastclick'],
			exports: 'adminlte'
		},
		'adminlte-app': {
			deps: ['adminlte'],
			exports: 'adminlteApp'
		},
		select2: {
			deps: ['jquery'],
			exports: 'select2'
		},
		ztree: {
			deps: ['jquery'],
			exports: 'ztree'
		},
		ckeditor: {
			deps: ['jquery'],
			exports: 'ckeditor'
		},
		ckeditorConfig: {
			deps: ['ckeditor'],
			exports: 'ckeditorConfig'
		},
		moment: {
			deps: ['jquery'],
			exports: 'moment'
		},
	},
	// Do not use RequireJS' paths option to configure the path to CodeMirror, since it will break loading submodules through relative paths. Use the packages configuration option instead
	packages: [{
		name: "codemirror",
		location: "../lib/codemirror", // codemirror base 的目录
		main: "lib/codemirror" // 这是相对于codemirror base目录主程序所在的位置
	}],
});

// Start the main app logic.
requirejs(['jquery', 'jquery-ui'], function($) {
	// 当dom加载完成时
	$(function() {
		// Resolve conflict in jQuery UI tooltip with Bootstrap tooltip
		$.widget.bridge('uibutton', $.ui.button);
		// 加载页面主题需要的js文件
		require(['bootstrap', 'jquery-slimscroll', 'fastclick', 'adminlte', 'adminlte-app'], function() {
			// 加载项目所需的框架
			require(['angular', 'angular-animate', 'angular-cookies', 'angular-touch', 'ui-router'], function() {
				// 加载项目
				require(['app'], function() {
					console.log('app started');
				});
			});
		});
	});
});