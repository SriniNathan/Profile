package com.srininathan.profiler.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.srininathan.profiler.sql.bll.SQLAnalyzerBll;
import com.srininathan.profiler.sql.bll.SQLCollectorBll;

@Component
@Scope("singleton")
public class AgentDataCollector implements RemoteAgentInterface {

	private boolean alive = false;
	private List<String> commandsToAgentList = new ArrayList<String>();
	private int portNumber = 1025;
	private Registry registry = null;
	private String[] packageNames = { "com.inspro", "com.atiam" };
	private RemoteAgentInterface stub = null;

	// @Autowired
	// private MethodCollectorBll methodCollectorBll;
	//
	@Autowired
	private SQLCollectorBll sqlCollectorBll;

	@Autowired
	private SQLAnalyzerBll sqlAnalyzerBll;

	private AgentDataCollector() {

		super();
	}

	public boolean isProfilingOn() {

		return alive;
	}

	public void start(int portNumber) throws Exception {

		String name = "AgentInterface";
		try {
			try {
				registry = java.rmi.registry.LocateRegistry.createRegistry(portNumber);
			} catch (Exception e1) {
				registry = java.rmi.registry.LocateRegistry.getRegistry(portNumber);
			}
			if (stub == null)
				stub = (RemoteAgentInterface) UnicastRemoteObject.exportObject(this, 0);
			registry.bind(name, stub);
			this.portNumber = portNumber;
			alive = true;
		} catch (Exception e) {
			alive = false;
			throw e;
		}

		if (sqlAnalyzerBll.getWorkList() == null) {
			ExecutorService service = Executors.newSingleThreadExecutor();
			sqlAnalyzerBll.setWorkList(sqlCollectorBll.getWorkList());
			service.execute(sqlAnalyzerBll);
		}

	}

	public int getPortNumber() {

		return portNumber;
	}

	public void stop() throws Exception {

		try {
			String name = "AgentInterface";
			registry.unbind(name);
			alive = false;
		} catch (Exception e) {
			throw e;
		}
	}

	public void addCommandToAgent(String command) {

		synchronized (this) {
			commandsToAgentList.add(command);
		}
	}

	public boolean isStarted() {

		return alive;
	}

	@Override
	public String[] getPackageNames() throws RemoteException {

		return packageNames;
	}

	public void setPackageNames(String[] packageNames) {

		this.packageNames = packageNames;
	}

	@Override
	public String newConnetion(String hostName, String processID, String threadName, String connectionIdentifier,
			String connectionURL, String databaseName, String databaseType, String databaseVersion, String driverName,
			String driverVersion, boolean isAutoCommit, StackTraceElement[] trace) throws RemoteException {

		// ConnectionStatsBll connectionStatsBll =
		// ConnectionStatsBll.getConnectionStatsBll(connectionURL,
		// databaseName);
		// if (connectionStatsBll.getTableNames() == null ||
		// connectionStatsBll.getTableNames().length == 0)
		// return "setTableNames";
		// ConnectionCollectorBll.newConnetion(hostName, processID, threadName,
		// connectionIdentifier, connectionURL,
		// databaseName, databaseType, databaseVersion, driverName,
		// driverVersion, isAutoCommit, trace);
		return null;
	}

	@Override
	public void setTableNames(String connectionURL, String databaseName, String[] tableNames) throws RemoteException {

		// ConnectionStatsBll connectionStatsBll =
		// ConnectionStatsBll.getConnectionStatsBll(connectionURL,
		// databaseName);
		// connectionStatsBll.setTableNames(tableNames);
	}

	@Override
	public String closeConnection(String threadName, String connectionIdentifier, StackTraceElement[] trace)
			throws RemoteException {

		// ConnectionCollectorBll.closeConnection(threadName,
		// connectionIdentifier, trace);
		return null;
	}

	@Override
	public void startTransaction(String threadName, String connectionIdentifier, StackTraceElement[] trace, long time)
			throws RemoteException {

		// TransactionCollectorBll.startTransaction(connectionIdentifier, trace,
		// time);
	}

	@Override
	public void commitTransaction(String threadName, String connectionIdentifier, StackTraceElement[] trace, long time)
			throws RemoteException {

		// TransactionCollectorBll.endTransaction(connectionIdentifier, trace,
		// time, true);
	}

	@Override
	public void rollbackTransaction(String threadName, String connectionIdentifier, StackTraceElement[] trace,
			long time) throws RemoteException {

		// TransactionCollectorBll.endTransaction(connectionIdentifier, trace,
		// time, false);
	}

	@Override
	public void executeSQL(String threadName, String connectionIdentifier, String statementIdentifier, String sql,
			Object[] values, boolean[] isString, long starttime, long endtime, StackTraceElement[] trace,
			String errorMessage) throws RemoteException {

		sqlCollectorBll.executeSQL(threadName, connectionIdentifier, statementIdentifier, sql, values, isString,
				starttime, endtime, trace, errorMessage);
	}

	@Override
	public void executeBatchSQL(String threadName, String connectionIdentifier, String statementIdentifier,
			String[] sqls, Object[] values, boolean[] isString, long starttime, long endtime, StackTraceElement[] trace,
			String errorMessage) throws RemoteException {

		System.out.println("execute batch called");
	}

	@Override
	public void resultSetMetrics(String threadName, String statementGuid, int numberOfRows, long starttime,
			long endtime) throws RemoteException {

		sqlCollectorBll.resultSetMetrics(threadName, statementGuid, numberOfRows, starttime, endtime);
	}

	@Override
	public String commands() throws RemoteException {

		return null;
	}

	public static void main(String[] args) throws Exception {

		// AgentDataCollector.getInstance().start( 1023 );
		System.out.println("Done");
	}

	@Override
	public void codeSequence(String sequenceString) {

		// ObjectMapper objectMapper = new ObjectMapper();
		// MethodDcl methodDcl = null;
		// try {
		// methodDcl = objectMapper.readValue(sequenceString, MethodDcl.class);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// methodCollectorBll.addMethodDcl(methodDcl);

	}

}
