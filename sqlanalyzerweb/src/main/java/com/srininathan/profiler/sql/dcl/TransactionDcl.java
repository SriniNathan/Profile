package com.srininathan.profiler.sql.dcl;

public class TransactionDcl {
	
	public long startTime;
	public long endTime;
	public int transactionIdentifier;
	public String connectionIdentifier;
	public boolean isCommitted;
	public StackTraceElement[] startStackTraceElement;
	public StackTraceElement[] endStackTraceElement;
	
	public TransactionDcl() {
		transactionIdentifier = System.identityHashCode(this);
	}
	

}
