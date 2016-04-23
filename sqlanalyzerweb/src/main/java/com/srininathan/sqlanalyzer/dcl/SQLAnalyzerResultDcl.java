package com.srininathan.sqlanalyzer.dcl;

import java.util.ArrayList;

public class SQLAnalyzerResultDcl {

	public boolean newUpdates = false;
	public String lastUpdateTime;
	public DashboardStatsDcl dashboardStats;
	public ArrayList<DetailTableStatsDcl> detailedTableStats;
	public ArrayList<PreparedStatementStatsDcl> preparedStatementStats;

}
