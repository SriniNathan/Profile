'use strict';

app.controller('StartAnalyzerControl', [ '$scope', '$state', '$location', 'sqlAnalyzerService', function($scope, $state, $location, sqlAnalyzerService) {

	$scope.rmiport = 1025;

	$scope.submit = function() {

		var promise = sqlAnalyzerService.startAnalyzerAtPort($scope.rmiport);

		promise.then(function(data) {
			if (data.listening === true) {
				$state.go("main");
			}
		});

	}

} ]);
