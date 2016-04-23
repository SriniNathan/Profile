'use strict';

app.controller('DashboardControl', [ '$scope', '$state', 'sqlAnalyzerService', function($scope, $state, sqlAnalyzerService) {

} ]);

app.controller('DashboardMainControl', [ '$scope', '$timeout', 'sqlAnalyzerService', function($scope, $timeout, sqlAnalyzerService) {

	var loadData = function(clientUpdateTime) {
		var promise = sqlAnalyzerService.getDashboardHomeStats(clientUpdateTime);
		promise.then(function(data) {

			if (data.newUpdates === true) {
				$scope.sqlTypeStatsArray = data.dashboardStats.sqlTypeStatsArray;
				$scope.tableSummaryStatsArray = data.dashboardStats.tableSummaryStatsArray;
			}
			clientUpdateTime = data.lastUpdateTime;

			$timeout(function() {
				loadData(clientUpdateTime);
			}, 200);
		}, function(error) {
			$timeout(function() {
				loadData(clientUpdateTime);
			}, 10000);
		});
	};

	loadData("0");

} ]);

app.controller('DetailedTableStatsControl', [ '$scope', '$timeout', 'sqlAnalyzerService', function($scope, $timeout, sqlAnalyzerService) {

	var loadData = function(clientUpdateTime) {
		var promise = sqlAnalyzerService.getDetailedTableStats(clientUpdateTime);
		promise.then(function(data) {

			if (data.newUpdates === true) {
				$scope.detailedTableStatsArray = data.detailedTableStats;
			}
			clientUpdateTime = data.lastUpdateTime;

			$timeout(function() {
				loadData(clientUpdateTime);
			}, 200);
		}, function(error) {
			$timeout(function() {
				loadData(clientUpdateTime);
			}, 10000);
		});
	};

	loadData("0");

} ]);

app.controller('PreparedStatementStatsControl', [ '$scope', '$timeout', '$stateParams','$sce', 'sqlAnalyzerService', function($scope, $timeout, $stateParams,$sce, sqlAnalyzerService) {

	var loadData = function(clientUpdateTime) {
		var tableName = $stateParams.tableName;
		var promise = sqlAnalyzerService.getPreparedStatementStatsByTableName(tableName, clientUpdateTime);
		promise.then(function(data) {

			if (data.newUpdates === true) {
				$scope.preparedStatementStats = data.preparedStatementStats;
				for (var i = 0; i < $scope.preparedStatementStats.length; i++) {
					$scope.preparedStatementStats[i]["showSQL"] = true;
					$scope.preparedStatementStats[i]["formattedSQL_HTML"] = "";
				}
			}
			clientUpdateTime = data.lastUpdateTime;

			$timeout(function() {
				loadData(clientUpdateTime);
			}, 200);
		}, function(error) {
			$timeout(function() {
				loadData(clientUpdateTime);
			}, 10000);
		});
	};

	$scope.expandFormatSQL = function(row) {
		row.showSQL = false;
		if (row.formattedSQL_HTML.length == 0) {
			var formattedSQL_HTML = hljs.highlight("sql", row.formattedSQL, true).value;
			row.formattedSQL_HTML = $sce.trustAsHtml(formattedSQL_HTML);
		}
	}

	$scope.collapseFormatSQL = function(row) {
		row.showSQL = true;
	}
	
	$scope.formatSQL = function( row ) {
		
		return row.sql;

	}

	loadData("0");

} ]);
