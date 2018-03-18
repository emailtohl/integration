define([ 'flow/module' ], function(flowModule) {
	return flowModule.factory('flowService', [ '$http', 'util', function($http, util) {
		return {
			startWorkflow : function(flowData) {
				return $http.post('flow/startWorkflow', flowData);
			},
			findTodoTasks : function() {
				return $http.get('flow/todoTasks');
			},
			claim : function(taskId) {
				return $http.post('flow/claim/' + taskId);
			},
			check : function(flowData) {
				return $http.post('flow/check', flowData);
			},
			reApply : function(flowData) {
				return $http.post('flow/reApply', flowData);
			},
			getCommentInfo : function(processInstanceId) {
				return $http.get('flow/commentInfo/' + processInstanceId);
			},
			page : function(param) {
				var query = util.encodeUrlParams(param);
				return $http.get('flow/query' + (query ? '?' + query : ''));
			},
			list : function(param) {
				var query = util.encodeUrlParams(param);
				return $http.get('flow/list' + (query ? '?' + query : ''));
			},
			get : function(id) {
				return $http.get('flow/' + id);
			},
			findByProcessInstanceId : function(processInstanceId) {
				return $http.get('flow/byProcessInstanceId/' + processInstanceId);
			},
			findByFlowNum : function(flowNum) {
				return $http.get('flow/byFlowNum/' + flowNum);
			},
		};
	}]);
});