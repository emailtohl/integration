/**
 * 我的流程单
 */
define(['flow/module', 'flow/service'], function(flowModule, service) {
	return flowModule
	.controller('FlowMineCtrl', [ '$scope', '$http', '$state', 'flowService', 'util'
	                         , function($scope, $http, $state, flowService, util) {
		var self = this, datatable;
		util.loadasync('lib/datatables/dataTables.bootstrap.css');
		$scope.getAuthentication().then(function() {
			load();
		});

		var subscription = $scope.websocketEndpoint.messageStream.subscribe(function(message) {
			if ('flowNotify' == message.messageType) {
				load();
			}
		});
		
		$scope.$on('$destroy',function () {
			subscription.completed();
        });
		
		function load() {
			flowService.list({applicantId: $scope.getUserId()}).then(function(resp) {
				self.list = resp.data;
			});
		}
		
		self.table = function() {
			requirejs(['jquery', 'dataTables', 'dataTables-bootstrap'], function($) {
				datatable = $("#flow-list").DataTable({
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
                    destroy: true, //Cannot reinitialise DataTable,解决重新加载表格内容问题,
                    columnDefs: [
                     	{
							orderSequence : [ "desc" ],
							targets : [ 1 ]
						},
                    ]
                });
			});
		};
		
		self.getTaskName = function(flowData) {
			if (flowData.taskName) {
				return flowData.taskName;
			}
			if (flowData.pass == null) {
				return '';
			}
			if (flowData.pass === false) {
				return '未通过';
			}
			return '通过';
		};
	}]);
});