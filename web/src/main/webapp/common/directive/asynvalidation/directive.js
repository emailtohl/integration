/**
 * 异步校验
 * 在local属性上输入校验的地址，然后将结果反馈到result属性上，如：
 * <input asynvalidation local="customer/exist?cellPhoneOrEmail=" result="ctrl.asynvalidation(exist)" type="text" ng-model="ctrl.form.name">
 * @author HeLei
 */
define([ 'common/module', 'rx', 'toastr' ], function(common, Rx, toastr) {
	common.directive('asynvalidation', [ '$http', function($http) {
		return {
			restrict : 'A',
			scope : {
				local : '@',
				result : '&'
			},
			link : function($scope, $element, $attrs) {
				// Search for a given term
				function validator(term) {
					return $http.get($scope.local + term);
				}
				// Get all distinct key up events from the input and only fire
				// if long enough and distinct
				var keyup = Rx.Observable.fromEvent($element, 'keyup')
						.map(function(e) {
							return e.target.value; // Project the text from the input
						}).filter(function(text) {
							return text.length > 2; // Only if the text is longer than 2 characters
						}).debounce(750 /* Pause for 750ms */)
						.distinctUntilChanged(); // Only if the value has changed
				var searcher = keyup.flatMapLatest(validator);
				searcher.subscribe(function(respose) {
					$scope.result(respose.data)
				}, function(error) {
					toastr.error(error.data);
				});
			}
		};
	} ]);
});