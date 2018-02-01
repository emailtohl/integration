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
	;
});