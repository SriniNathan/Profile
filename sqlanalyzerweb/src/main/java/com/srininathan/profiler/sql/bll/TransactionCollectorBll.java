package com.srininathan.profiler.sql.bll;

import java.util.HashMap;
import java.util.Map;

import com.srininathan.profiler.sql.dcl.TransactionDcl;

public class TransactionCollectorBll {
	
	private static Map<Integer,TransactionDcl> transactionMap = new HashMap<Integer,TransactionDcl>();
	private static Map<String, TransactionDcl> currentTransactionMap = new HashMap<String, TransactionDcl>();
	
	public static synchronized void startTransaction( String connectionIdentifier,StackTraceElement[] trace, long time ) {
		TransactionDcl transactionDcl = new TransactionDcl();
		transactionDcl.startTime = time;
		transactionDcl.connectionIdentifier = connectionIdentifier;
		transactionDcl.startStackTraceElement = trace;
		transactionMap.put(transactionDcl.transactionIdentifier,transactionDcl);
		currentTransactionMap.put(connectionIdentifier, transactionDcl);
	}
	
	public static synchronized void endTransaction( String connectionIdentifier,StackTraceElement[] trace, long time, boolean isCommitted ) {
		TransactionDcl transactionDcl = currentTransactionMap.get(connectionIdentifier);
		if( transactionDcl == null ) return;
		transactionDcl.endTime = time;
		transactionDcl.endStackTraceElement = trace;
		transactionDcl.isCommitted = isCommitted;
		currentTransactionMap.remove(connectionIdentifier);
	}
	
	public static synchronized int getTransactionID( String connectionIdentifier ) {
		if( currentTransactionMap.containsKey(connectionIdentifier) ) return currentTransactionMap.get(connectionIdentifier).transactionIdentifier;
		return 0;
	}

}
