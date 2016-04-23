package com.srininathan.profiler.sql.dcl;

public class SQLDcl {
	
	public String statementIdentifier;
	public String connectionIdentifier;
	public long startTime;
	public long endTime;
	public StackTraceElement[] trace;
	public String sql;
	public Object values[];
	public String errorMessage;
	public int rowsRead = -1;
	public long rowsReadTimeTaken;
	public boolean[] isString;
	public boolean isInTransaction;
	public int transactionIdentifier;

}
