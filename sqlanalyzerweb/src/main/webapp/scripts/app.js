'use strict';

window.app_version = 3;

var app = angular.module('sqlanalyzer', [ 'ui.router', 'smart-table', 'ng-clipboard', 'ncy-angular-breadcrumb' ]);

app.config([ '$stateProvider', '$urlRouterProvider', '$locationProvider', function($stateProvider, $urlRouterProvider, $locationProvider) {

	$urlRouterProvider.when('/dashboard', '/dashboard/main');
	$urlRouterProvider.otherwise("/home");

	$stateProvider.state('home', {
		url : "/home",
		controller : "HomeControl"
	}).state('startAnalyzer', {
		url : "/startAnalyzer",
		templateUrl : 'views/startAnalyzer.html',
		controller : "StartAnalyzerControl"
	}).state('dashboard', {
		url : "/dashboard",
		abstract: true,
		templateUrl : 'views/dashboard.html',
		controller : "DashboardControl"
	}).state('main', {
		url : "/main",
		parent : 'dashboard',
		templateUrl : 'views/dashboardMain.html',
		controller : "DashboardMainControl",
		ncyBreadcrumb : {
			    label: 'Dashboard'
			  }
	}).state('detailedTableStats', {
		url : "/detailedTableStats",
		parent : 'dashboard',
		templateUrl : 'views/detailedTableStats.html',
		controller : "DetailedTableStatsControl",
		ncyBreadcrumb : {
		    label: 'Table Stats',
		    parent: 'main'
		  }
	}).state('psByTable', {
		url : "/ps/table/:tableName",
		parent : 'dashboard',
		templateUrl : 'views/preparedStatementStats.html',
		controller : "PreparedStatementStatsControl"
	});

} ]);