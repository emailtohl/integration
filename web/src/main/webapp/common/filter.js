/**
 * 过滤器
 */
define(['common/module'], function(commonModule) {
	commonModule
	/**
	 * 指定可信任的html代码
	 */
	.filter('trustHtml', function($sce) {
		return function(input) {
			return $sce.trustAsHtml(input);
		}
	})
	.filter('boolToString', function() {
		return function(boolVal) {
			return boolVal ? '是' : '否';
		}
	})
	.filter('arrayToString', function() {
		return function(array) {
			if (!(array instanceof Array)) {
				return '';
			}
			return array.join(', ');
		}
	})
	.filter('omit', function() {
		return function(content) {
			if (typeof content != 'string')
				return content;
			var i = content.length;
			if (i > 20)
				i = 20;
			return content.substring(0, i) + '……';
		}
	})
	;
});