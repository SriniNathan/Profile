package com.srininathan.profiler.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteAgentInterface extends Remote {
	

	public boolean isProfilingOn() throws RemoteException;
	
	// Returns a String Command
	public String commands() throws RemoteException;
	
	// Get the package names to profile.
	public String[] getPackageNames() throws RemoteException;
	
	
	public String newConnetion(String hostName,String processID,String threadName,String connectionIdentifier,String connectionURL,
								String databaseName,String databaseType,String databaseVersion,String driverName,String driverVersion,
								boolean isAutoCommit,StackTraceElement[] trace) throws RemoteException;
	
	// Sets all the list of table names
	public void setTableNames(String connectionURL, String databaseName, String[] tableNames) throws RemoteException;
	

	public String closeConnection(String threadName,String connectionIdentifier,StackTraceElement[] trace) throws RemoteException;
	

	public void startTransaction(String threadName,String connectionIdentifier,StackTraceElement[] trace, long time) throws RemoteException;
	

	public void commitTransaction(String threadName,String connectionIdentifier,StackTraceElement[] trace, long time) throws RemoteException;
	

	public void rollbackTransaction(String threadName,String connectionIdentifier,StackTraceElement[] trace, long time) throws RemoteException;
	

	public void executeSQL(String threadName,String connectionIdentifier,String statementIdentifier,
							String sql,Object values[],boolean isString[],long starttime,long endtime,
							StackTraceElement[] trace,String errorMessage ) throws RemoteException;
	

	public void executeBatchSQL(String threadName,String connectionIdentifier,String statementIdentifier,
			String[] sqls,Object values[],boolean isString[],long starttime,long endtime,
			StackTraceElement[] trace, String errorMessage ) throws RemoteException;
	

	public void resultSetMetrics(String threadName,String statementGuid,int numberOfRows,long starttime,long endtime)throws RemoteException;
	
	
	public void codeSequence( String sequenceString ) throws RemoteException;

}
