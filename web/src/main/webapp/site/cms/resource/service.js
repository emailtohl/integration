define(['cms/module', 'common/context' ], function(cmsModule) {
	return cmsModule.factory('resourceService', [ '$http', 'util', function($http, util) {
		return {
			/**
			 * 根据文本内容查询目录
			 */
			query : function(param) {
				return $http.get('resource/query' + (param ? '?param=' + param : ''));
			},
			/**
			 * 创建一个目录
			 */
			createDir : function(dirName) {
				return $http({
					method : 'POST',
					url  : 'resource/createDir',
					data : 'dirName=' + encodeURIComponent(dirName),
					headers : { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
				});
			},
			
			/**
			 * 为目录或文件改名
			 */
			reName : function(srcName, destName) {
				return $http({
					method : 'POST',
					url  : 'resource/reName',
					data : 'srcName=' + encodeURIComponent(srcName) + '&destName=' + destName,
					headers : { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
				});
			},
			
			/**
			 * 删除目录或文件
			 */
			'delete' : function(filename) {
				return $http({
					method : 'POST',
					url  : 'resource/delete',
					data : 'filename=' + encodeURIComponent(filename),
					headers : { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
				});
			},
			
			/**
			 * 获取可使用的字符
			 */
			getAvailableCharsets : function() {
				return $http.get('resource/availableCharsets');
			},
			
			/**
			 * 下载文件
			 */
			loadText : function(path, charset) {
				return $http({
					method : 'POST',
					url  : 'resource/loadText',
					data : 'path=' + path + '&charset=' + charset,
					headers : { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
				});
			},
			
			/**
			 * 编辑文件
			 */
			writeText : function(path, textContext, charset) {
				return $http.post('resource/writeText', {
					path : path,
					textContext : textContext,
					charset : charset
				});
				/*return $http({
					method : 'POST',
					url  : 'resource/writeText',
					data : 'path=' + path + '&textContext=' + textContext + '&charset=' + charset,
					headers : { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
				});*/
			},
		};
	}]);
});