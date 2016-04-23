package com.srininathan.sqlanalyzer.dcl;

import java.util.List;

public class PreparedStatementStatsDcl {
	
	public String uniqueIdentifier;
	public String sqlType;
	public String sql;
	public String formattedSQL;
	public int count;
	public long timetaken;
	public int rowsRead;
	public long rowsReadTimeTaken;
	public List<String> tableNames;	

}
