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
		bootstrap: '../lib/bootstrap/js/bootstrap.min',
		angular: '../lib/angular/angular',
		'angular-animate': '../lib/angular/angular-animate.min',
		'angular-cookies': '../lib/angular/angular-cookies.min',
		'angular-touch': '../lib/angular/angular-touch.min',
		'ui-router': '../lib/ui/router/angular-ui-router.min',
		'ui-select': '../lib/ui/select/select.min',
		'angular-translate': '../lib/angular-translate/angular-translate.min',
		'ui-bootstrap': '../lib/ui/bootstrap/require-ui-bootstrap',
		'jquery-slimscroll': '../lib/assets/js/jquery.slimscroll.min',
		fastclick: '../lib/assets/js/fastclick.min',
		adminlte: '../lib/adminLTE/js/adminlte.min',
		'adminlte-app': '../lib/adminLTE/js/app.min',
		select2: '../lib/select2/select2.full.min',
		ztree: '../lib/ztree/jquery.ztree.all.min',
		'ckeditor': '../lib/ckeditor/ckeditor',
		'ckeditorConfig': '../lib/ckeditor/config',
		'moment': '../lib/moment/moment',
		'angular-datepicker': '../lib/angular-datepicker/angular-datepicker',
		'ng-verify': '../lib/ng-verify/ng-verify',
		'sparkline' : '../lib/sparkline/jquery.sparkline.min',
		'knob' : '../lib/knob/jquery.knob',
		toastr : '../lib/toastr/toastr.min',
		rx : '../lib/RxJS/rx.all.min'
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
		bootstrap: {
			deps: ['jquery'],
			exports: 'bootstrap'
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
		'angular-translate': {
			deps: ['angular'],
			exports: 'translate',
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
			exports: 'CKEDITOR'
		},
		ckeditorConfig: {
			deps: ['ckeditor'],
			exports: 'ckeditorConfig'
		},
		moment: {
			deps: ['jquery'],
			exports: 'moment'
		},
		'angular-datepicker': {
			deps: ['angular'],
		},
		'ng-verify': {
			deps: ['angular'],
		},
		'sparkline' : {
			deps : [ 'jquery' ],
			exports : 'sparkline'
		},
		'knob' : {
			deps : [ 'jquery-ui' ],
			exports : 'knob'
		},
		toastr : {
            deps : ['jquery'],
            exports : 'toastr'
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
		// 加载项目
		require(['bootstrap', 'jquery-slimscroll', 'fastclick', 'adminlte', 'adminlte-app'], function() {
			// 加载项目所需的框架
			require(['angular', 'app'], function(angular) {
				angular.element(document).ready(function() {
					angular.bootstrap(document, [ 'app' ]);
					console.log('app started');
				});
			});
		});
	});
});