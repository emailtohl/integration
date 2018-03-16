/**
 * 我的流程单
 */
define(['flow/module', 'flow/service'], function(flowModule, service) {
	return flowModule
	.controller('FlowMineCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this;
		util.loadasync('lib/datatables/dataTables.bootstrap.css');
		$scope.getAuthentication().then(function() {
			flowService.list({applicantId: $scope.getUserId()}).then(function(resp) {
				self.list = resp.data;
			});
		});
		
		self.table = function() {
			requirejs(['jquery', 'dataTables', 'dataTables-bootstrap'], function($) {
				$("#flow-list").DataTable({
                    stateSave: true,
                    // 0行是checkbox，7行是操作按钮，不进行排序
//                    aoColumnDefs : [ { bSortable: false, aTargets: [ 0, 7 ] }],
                    language: {
                        search: "在表格中搜索:",
                        lengthMenu: '每页 _MENU_ 条记录',
                        zeroRecords: '没有找到记录',
                        info: '第 _PAGE_ 页 ( 总共 _PAGES_ 页 )',
                        infoEmpty: '无记录',
                        infoFiltered: '(从 _MAX_ 条记录过滤)',
                        paginate : {
                            first : '首页',
                            previous : '上一页',
                            next : '下一页',
                            last : '尾页'
                        },
                    },
                });
			});
		};
		
	}]);
});