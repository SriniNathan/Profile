'use strict';

app.controller('HomeControl', [ '$scope', '$state', 'sqlAnalyzerService', function($scope, $state, sqlAnalyzerService) {
	var promise = sqlAnalyzerService.isAnalyzerPortListening();
	promise.then(function(data) {
		if (data.listening === false) {
			$state.go("startAnalyzer");
		} else {
			$state.go("main");
		}
	});

} ]);
