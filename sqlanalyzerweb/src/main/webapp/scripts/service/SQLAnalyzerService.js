'use strict';

app.factory('sqlAnalyzerService', [ '$http', '$q', function($http, $q) {

	return {

		isAnalyzerPortListening : function() { // Fetches category list from
			// server.
			return $http.get('/service/isAnalyzerRunning').then(function(response) {
				return response.data;
			}, function(errResponse) {
				console.error('Error while fetching Items');
				return $q.reject(errResponse);
			});
		},

		startAnalyzerAtPort : function(port) {

			return $http.post("/service/startAnalyzer/", port).then(function(response) {
				return response.data;
			}, function(errResponse) {
				console.error('Error while fetching Items');
				return $q.reject(errResponse);
			});
		},

		getDashboardHomeStats : function(lastUpdateTime) {

			return $http.get("/service/dashboardHomeStats/?lastUpdateTime=" + lastUpdateTime).then(function(response) {
				return response.data;
			}, function(errResponse) {
				console.error('Error while fetching Items');
				return $q.reject(errResponse);
			});
		},

		getDetailedTableStats : function(lastUpdateTime) {

			return $http.get("/service/detailedTableStats/?lastUpdateTime=" + lastUpdateTime).then(function(response) {
				return response.data;
			}, function(errResponse) {
				console.error('Error while fetching Items');
				return $q.reject(errResponse);
			});
		},

		getPreparedStatementStatsByTableName : function(tableName, lastUpdateTime) {

			return $http.get("/service/ps/table/" + tableName + "/?lastUpdateTime=" + lastUpdateTime).then(function(response) {
				return response.data;
			}, function(errResponse) {
				console.error('Error while fetching Items');
				return $q.reject(errResponse);
			});
		},
		
		/*formatSQL : function(sql) {

			sql = '"' + sql + '"';
			return $http.post("/service/sql/format", sql).then(function(response) {
				return response.data;
			}, function(errResponse) {
				console.error('Error while fetching Items');
				return $q.reject(errResponse);
			});
		},*/

	};

} ]);