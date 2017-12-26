/**
 * jqueryui的插件
 * @author HeLei
 * @date 2017.02.04
 */
define([ 'common/module', 'common/service/util', 'jquery', 'jquery-ui' ], function(commonModule, util, $) {
	commonModule.directive('datepicker', [ 'util', function(util) {
		util.loadasync('lib/jquery/jquery-ui.min.css');
		$.datepicker.regional['zh-CN'] = {
			clearText : '清除',
			clearStatus : '清除已选日期',
			closeText : '关闭',
			closeStatus : '不改变当前选择',
			prevText : '<上月',
			prevStatus : '显示上月',
			prevBigText : '<<',
			prevBigStatus : '显示上一年',
			nextText : '下月>',
			nextStatus : '显示下月',
			nextBigText : '>>',
			nextBigStatus : '显示下一年',
			currentText : '今天',
			currentStatus : '显示本月',
			monthNames : [ '一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月',
					'九月', '十月', '十一月', '十二月' ],
			monthNamesShort : [ '一', '二', '三', '四', '五', '六', '七', '八', '九',
					'十', '十一', '十二' ],
			monthStatus : '选择月份',
			yearStatus : '选择年份',
			weekHeader : '周',
			weekStatus : '年内周次',
			dayNames : [ '星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六' ],
			dayNamesShort : [ '周日', '周一', '周二', '周三', '周四', '周五', '周六' ],
			dayNamesMin : [ '日', '一', '二', '三', '四', '五', '六' ],
			dayStatus : '设置 DD 为一周起始',
			dateStatus : '选择 m月 d日, DD',
			dateFormat : 'yy-mm-dd',
			firstDay : 1,
			initStatus : '请选择日期',
			isRTL : false
		};
		$.datepicker.setDefaults($.datepicker.regional['zh-CN']);
		
		var reg = /(\d{4})\d{4}/;
		
		return {
			restrict : 'A',
			scope : {
				config : '='
			},
			controller : function($scope) {
				/*$scope.$watch('config', function(newVal, oldVal) {
					 console.log("config: " + newVal);
				});*/
			},
			require : 'ngModel',
			link : function($scope, $element, $attrs, ngModelCtrl) {
				var options = {
					changeMonth: true,
					changeYear: true,
					showAnim : 'show',
					dateFormat : 'yyyy-mm-dd',
					showButtonPanel: true,
				};
				$.extend(options, $scope.config);
				// Angular将元素封装成了jqLite，可直接使用jQuery的接口
				$($element).datepicker(options);

				// When data changes inside AngularJS
				// Notify the third party directive of the change
				ngModelCtrl.$render = function() {
					$element.val(ngModelCtrl.$viewValue);
				};
				// When data changes outside of AngularJS
				$element.on('change', function(args) {
					// Also tell AngularJS that it needs to update the UI
					$scope.$apply(function() {
						// Set the data within AngularJS
						var s = $element.val();
						var m = s.match(reg);
						if (m && m[1]) {
							s = s.replace(reg, m[1]);
						}
						$element.val(s);
						ngModelCtrl.$setViewValue(s);
					});
				});
			}
		};
	} ]);
});