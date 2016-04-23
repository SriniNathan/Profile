package com.srininathan.profiler.sql.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.srininathan.profiler.sql.dcl.SQLDcl;

@Component
@Scope("singleton")
public class SQLCollectorBll {

	private Map<String, SQLDcl> sqlDclMap = new HashMap<String, SQLDcl>();
	private List<String> workList = new ArrayList<String>();

	@Autowired
	private SQLAnalyzerBll sqlAnalyzerBll;

	public void executeSQL(String threadName, String connectionIdentifier, String statementIdentifier, String sql, Object[] values, boolean[] isString, long starttime, long endtime, StackTraceElement[] trace, String errorMessage) {

		SQLDcl sqlDcl = new SQLDcl();
		sqlDcl.statementIdentifier = statementIdentifier;
		sqlDcl.connectionIdentifier = connectionIdentifier;
		sqlDcl.startTime = starttime;
		sqlDcl.endTime = endtime;
		sqlDcl.trace = trace;
		sqlDcl.sql = sql;
		sqlDcl.values = values;
		sqlDcl.errorMessage = errorMessage;
		sqlDcl.isString = isString;
		sqlDcl.transactionIdentifier = TransactionCollectorBll.getTransactionID(connectionIdentifier);
		if (sqlDcl.transactionIdentifier == 0)
			sqlDcl.isInTransaction = false;
		else
			sqlDcl.isInTransaction = true;
		synchronized (sqlDclMap) {
			sqlDclMap.put(statementIdentifier, sqlDcl);
		}
		synchronized (workList) {
			workList.add(statementIdentifier);
			workList.notify();
		}
	}

	public List<String> getWorkList() {

		return workList;
	}

	public SQLDcl getSQLDcl(String statementIdentifier) {

		return sqlDclMap.get(statementIdentifier);
	}

	public synchronized List<SQLDcl> getSQLDclList() {

		ArrayList<SQLDcl> resultSQLDclList = new ArrayList<SQLDcl>();
		resultSQLDclList.addAll(sqlDclMap.values());
		return resultSQLDclList;
	}

	public synchronized void resultSetMetrics(String threadName, String statementGuid, int numberOfRows, long starttime, long endtime) {

		SQLDcl sqlDcl = sqlDclMap.get(statementGuid);
		if (sqlDcl != null) {
			sqlDcl.rowsRead = numberOfRows;
			sqlDcl.rowsReadTimeTaken = endtime - starttime;
		}
		sqlAnalyzerBll.updateSQLRowsReadStats(sqlDcl, numberOfRows, endtime - starttime);
	}

}
