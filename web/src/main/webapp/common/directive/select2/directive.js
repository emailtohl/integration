/**
 * 封装select2，选择项可为字符串、甚至是对象（以json形式表示）
 * 注意：对于对象，在回调过程中select2会报异常，但对功能无明显影响
 * @author HeLei
 */
define([ 'common/module', 'common/service/util', 'select2' ], function(common) {
	return common.directive('select2', [ 'util', function(util) {
		util.loadasync('lib/select2/select2.min.css');
		return {
			restrict : 'A',
			scope : {
				onChange : '&'
			},
			priority : 2,// 这个属性很重要，如果不设置高优先级，会被其他指令的覆盖掉，导致自定义的ngModelCtrl.$render不能触发
			require : 'ngModel',
			link : function($scope, $element, $attrs, ngModelCtrl) {
//				$element.select2();
				// When data changes inside AngularJS
				// Notify the third party directive of the change
				ngModelCtrl.$render = function() {
					var newModelVal = ngModelCtrl.$viewValue;
					$element.val(newModelVal);
					updateOptions(newModelVal);
					$element.select2();
				};
				// When data changes outside of AngularJS
				$element.on('change', function(args) {
					// Also tell AngularJS that it needs to update the UI
					$scope.$apply(function() {
						// Set the data within AngularJS
						ngModelCtrl.$setViewValue($element.val());
					});
					$scope.onChange({value : $element.val()});
				});
				
				function updateOptions(model) {
					if (!model) {
						$element.find('option').prop('selected', false);
					} else {
						var type, i, temp = '';
						type = typeof model;
						if (type === 'string') {// 若option的值是对象，angular就会以json形式存储，所以如果是字符串，则直接去匹配是否有此option
							temp = model.replace(/"/g, '\\"');
//							$element.find('option').prop('selected', false).filter('[value="' + temp + '"]').prop('selected', true);
							$element.find('option[value="' + temp + '"]').prop('selected', true);
						} else if (type === 'object') {
							if (model instanceof Array) {
								for (i = 0; i < model.length; i++) {
									arguments.callee(model[i]);
								}
							} else {
								// 尝试是否以对象ID进行判断相等性
								if (model.id) {
									$element.find('option').prop('selected', false)
									.each(function(i, o) {
										try {
											var opv = JSON.parse(o.value);
											if (opv && opv.id == model.id) {
												o.selected = true;
												return false;
											}
										} catch (e) {}
									});
								} else {
									temp = JSON.stringify(model);
									arguments.callee(temp);
								}
							}
						}
					}
				}
			}
		};
	} ]);
});