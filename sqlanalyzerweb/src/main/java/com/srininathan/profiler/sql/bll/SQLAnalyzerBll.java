package com.srininathan.profiler.sql.bll;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.srininathan.profiler.sql.dcl.SQLDcl;
import com.srininathan.profiler.sql.dcl.SQLStatsDcl;
import com.srininathan.profiler.sql.dcl.SqlType;
import com.srininathan.profiler.sql.dcl.StatementAnalysisDcl;
import com.srininathan.sqlanalyzer.controller.SQLAnalyzerRestServiceController;
import com.srininathan.sqlanalyzer.dcl.DashboardStatsDcl;
import com.srininathan.sqlanalyzer.dcl.DetailTableStatsDcl;
import com.srininathan.sqlanalyzer.dcl.PreparedStatementStatsDcl;
import com.srininathan.sqlanalyzer.dcl.SQLTypeStatsDcl;
import com.srininathan.sqlanalyzer.dcl.TableSummaryStatsDcl;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

@Component("sqlAnalyzerBll")
@Scope("singleton")
public class SQLAnalyzerBll implements Runnable {

	private Map<String, SQLStatsDcl> sqlTypeStatMap = new HashMap<String, SQLStatsDcl>();
	private Map<String, Map<SqlType, SQLStatsDcl>> tableSqlStatsDclMap = new HashMap<String, Map<SqlType, SQLStatsDcl>>();
	private Map<StatementAnalysisDcl, SQLStatsDcl> hotSpotStatsMap = new HashMap<StatementAnalysisDcl, SQLStatsDcl>();
	private Map<String, StatementAnalysisDcl> statementAnalysisMap = new HashMap<String, StatementAnalysisDcl>();
	private List<String> workList = null;
	private CCJSqlParserManager pm = new CCJSqlParserManager();

	@Autowired
	private SQLCollectorBll sqlCollectorBll;

	@Autowired
	private TablesNamesFinder tablesNamesFinder;

	@Autowired
	private SQLAnalyzerRestServiceController clientController;

	public void setWorkList(List<String> workList) {

		this.workList = workList;
	}

	public List<String> getWorkList() {

		return workList;
	}

	@Override
	public void run() {

		int index = 0;
		while (true) {
			while (workList.size() > index) {
				SQLDcl sqlDcl = sqlCollectorBll.getSQLDcl(workList.get(0));
				StatementAnalysisDcl statementAnalysisDcl = parseSQL(sqlDcl);
				if (statementAnalysisDcl != null) {
					updateSQLTypeStatistics(sqlDcl, statementAnalysisDcl);
					updateTableStatistics(sqlDcl, statementAnalysisDcl);
					updateHotSpotStatistics(sqlDcl, statementAnalysisDcl);
					synchronized (workList) {
						workList.remove(index);
					}
					clientController.pushResults();
				} else {
					synchronized (workList) {
						workList.remove(index);
					}
				}
			}
			try {
				synchronized (workList) {
					workList.wait();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private StatementAnalysisDcl parseSQL(SQLDcl sqlDcl) {

		String uniqueId = DigestUtils.md5Hex(sqlDcl.sql);

		StatementAnalysisDcl statementAnalysisDcl = null;
		if (!statementAnalysisMap.containsKey(uniqueId)) {
			statementAnalysisDcl = new StatementAnalysisDcl();
			statementAnalysisDcl.uniqueIdentifier = uniqueId;
			statementAnalysisDcl.sqlType = SqlType.UNKNOWN;
			statementAnalysisDcl.sql = sqlDcl.sql;
			statementAnalysisDcl.formattedSQL = new BasicFormatterImpl().format(sqlDcl.sql);
			net.sf.jsqlparser.statement.Statement statement = null;
			try {
				statement = pm.parse(new StringReader(sqlDcl.sql));
			} catch (JSQLParserException e) {

			}
			if (statement != null) {
				if (statement instanceof Select) {
					statementAnalysisDcl.sqlType = SqlType.SELECT;
					statementAnalysisDcl.tableList.addAll(tablesNamesFinder.getTableList((Select) statement));
				} else if (statement instanceof Insert) {
					statementAnalysisDcl.sqlType = SqlType.INSERT;
					statementAnalysisDcl.tableList.add(((Insert) statement).getTable().getName().toLowerCase());
				} else if (statement instanceof Update) {
					statementAnalysisDcl.sqlType = SqlType.UPDATE;
					statementAnalysisDcl.tableList.add(((Update) statement).getTable().getName().toLowerCase());
				} else if (statement instanceof Delete) {
					statementAnalysisDcl.sqlType = SqlType.DELETE;
					statementAnalysisDcl.tableList.add(((Delete) statement).getTable().getName().toLowerCase());
				}
			}
			statementAnalysisMap.put(uniqueId, statementAnalysisDcl);
		} else {
			statementAnalysisDcl = statementAnalysisMap.get(uniqueId);
		}
		statementAnalysisDcl.statementIdentifierList.add(sqlDcl.statementIdentifier);
		return statementAnalysisDcl;
	}

	private void updateSQLTypeStatistics(SQLDcl sqlDcl, StatementAnalysisDcl statementAnalysisDcl) {

		if (!sqlTypeStatMap.containsKey(statementAnalysisDcl.sqlType.name())) {
			sqlTypeStatMap.put(statementAnalysisDcl.sqlType.name(), new SQLStatsDcl(statementAnalysisDcl.sqlType));
		}
		sqlTypeStatMap.get(statementAnalysisDcl.sqlType.name()).addCount();
		sqlTypeStatMap.get(statementAnalysisDcl.sqlType.name()).addTimeTaken(sqlDcl.endTime - sqlDcl.startTime);
	}

	private void updateHotSpotStatistics(SQLDcl sqlDcl, StatementAnalysisDcl statementAnalysisDcl) {

		if (!hotSpotStatsMap.containsKey(statementAnalysisDcl)) {
			hotSpotStatsMap.put(statementAnalysisDcl, new SQLStatsDcl(statementAnalysisDcl.sqlType));
		}
		hotSpotStatsMap.get(statementAnalysisDcl).addCount();
		hotSpotStatsMap.get(statementAnalysisDcl).addTimeTaken(sqlDcl.endTime - sqlDcl.startTime);
	}

	private void updateTableStatistics(SQLDcl sqlDcl, StatementAnalysisDcl statementAnalysisDcl) {

		SqlType sqlType = statementAnalysisDcl.sqlType;
		if (statementAnalysisDcl.tableList.size() > 0) {
			for (String tableName : statementAnalysisDcl.tableList) {
				if (tableName != null) {
					tableName = tableName.toLowerCase();
					if (!tableSqlStatsDclMap.containsKey(tableName)) {
						tableSqlStatsDclMap.put(tableName, new HashMap<SqlType, SQLStatsDcl>());
					}
					Map<SqlType, SQLStatsDcl> map = tableSqlStatsDclMap.get(tableName);
					if (!map.containsKey(sqlType)) {
						map.put(sqlType, new SQLStatsDcl(sqlType));
					}
					SQLStatsDcl sqlStatsDcl = map.get(sqlType);
					sqlStatsDcl.addCount();
					sqlStatsDcl.addTimeTaken(sqlDcl.endTime - sqlDcl.startTime);
				}
			}
		}
	}

	public Collection<SQLStatsDcl> getSqlTypeStats() {

		return sqlTypeStatMap.values();
	}

	public Map<String, SQLStatsDcl> getSQLStatsDclMap() {

		return sqlTypeStatMap;
	}

	public Map<StatementAnalysisDcl, SQLStatsDcl> getHotSpotStatsMap() {

		return hotSpotStatsMap;
	}

	public void updateSQLRowsReadStats(SQLDcl sqlDcl, int rowsRead, long rowsReadTimeTaken) {

		// Add the table stats
		StatementAnalysisDcl statementAnalysisDcl = parseSQL(sqlDcl);
		if (statementAnalysisDcl != null) {
			for (String tableName : statementAnalysisDcl.tableList) {
				tableName = tableName.toLowerCase();
				if (tableSqlStatsDclMap.containsKey(tableName)) {
					SQLStatsDcl sqlStatsDcl = tableSqlStatsDclMap.get(tableName).get(SqlType.SELECT);
					if (sqlStatsDcl != null) {
						sqlStatsDcl.addRowsRead(rowsRead);
						sqlStatsDcl.addRowsReadTimeTaken(rowsReadTimeTaken);
					}
				}
			}
		}

		if (hotSpotStatsMap.containsKey(statementAnalysisDcl)) {
			hotSpotStatsMap.get(statementAnalysisDcl).addRowsRead(rowsRead);
			hotSpotStatsMap.get(statementAnalysisDcl).addRowsReadTimeTaken(sqlDcl.rowsReadTimeTaken);
		}

		// Add the sql stats
		sqlTypeStatMap.get(SqlType.SELECT.name()).addRowsRead(rowsRead);
		sqlTypeStatMap.get(SqlType.SELECT.name()).addRowsReadTimeTaken(rowsReadTimeTaken);

		clientController.pushResults();
	}

	public Map<String, Map<SqlType, SQLStatsDcl>> getTableSqlStatsDclMap() {

		return tableSqlStatsDclMap;
	}

	public DashboardStatsDcl getDashboardStats() {
		DashboardStatsDcl statsDcl = new DashboardStatsDcl();
		statsDcl.sqlTypeStatsArray = new ArrayList<>();
		initializeDashboardHomeSQLStats(statsDcl);
		statsDcl.tableSummaryStatsArray = new ArrayList<>();
		initializeDashboardTableSummaryStats(statsDcl);
		return statsDcl;
	}

	public ArrayList<PreparedStatementStatsDcl> getPreparedStatementStats(String tableName) {

		ArrayList<PreparedStatementStatsDcl> resultList = new ArrayList<>();

		for (Entry<StatementAnalysisDcl, SQLStatsDcl> entry : hotSpotStatsMap.entrySet()) {
			StatementAnalysisDcl statementAnalysisDcl = entry.getKey();
			SQLStatsDcl sqlStatsDcl = entry.getValue();
			if (statementAnalysisDcl.tableList != null && statementAnalysisDcl.tableList.contains(tableName)) {
				PreparedStatementStatsDcl preparedStatementStatsDcl = new PreparedStatementStatsDcl();
				preparedStatementStatsDcl.uniqueIdentifier = statementAnalysisDcl.uniqueIdentifier;
				preparedStatementStatsDcl.sqlType = statementAnalysisDcl.sqlType.name();
				preparedStatementStatsDcl.sql = statementAnalysisDcl.sql;
				preparedStatementStatsDcl.formattedSQL = statementAnalysisDcl.formattedSQL;
				preparedStatementStatsDcl.tableNames = statementAnalysisDcl.tableList;
				preparedStatementStatsDcl.count = sqlStatsDcl.count;
				preparedStatementStatsDcl.timetaken = sqlStatsDcl.timeTaken;
				preparedStatementStatsDcl.rowsRead = sqlStatsDcl.rowsRead;
				preparedStatementStatsDcl.rowsReadTimeTaken = sqlStatsDcl.rowsReadTimeTaken;
				resultList.add(preparedStatementStatsDcl);
			}
		}

		return resultList;
	}

	public ArrayList<DetailTableStatsDcl> getDetailedTableStats() {
		ArrayList<DetailTableStatsDcl> resultList = new ArrayList<DetailTableStatsDcl>();

		for (Entry<String, Map<SqlType, SQLStatsDcl>> entry : tableSqlStatsDclMap.entrySet()) {
			DetailTableStatsDcl tableStatsDcl = new DetailTableStatsDcl();
			tableStatsDcl.tableName = entry.getKey();
			if (entry.getValue().containsKey(SqlType.SELECT)) {
				tableStatsDcl.selectcount = entry.getValue().get(SqlType.SELECT).count;
				tableStatsDcl.selecttimetaken = entry.getValue().get(SqlType.SELECT).timeTaken;
				tableStatsDcl.rowsReadCount = entry.getValue().get(SqlType.SELECT).rowsRead;
				tableStatsDcl.rowsReadTimeTaken = entry.getValue().get(SqlType.SELECT).rowsReadTimeTaken;
				tableStatsDcl.totalcount += tableStatsDcl.selectcount;
				tableStatsDcl.totaltimetaken += tableStatsDcl.selecttimetaken;
			}
			if (entry.getValue().containsKey(SqlType.INSERT)) {
				tableStatsDcl.insertcount = entry.getValue().get(SqlType.INSERT).count;
				tableStatsDcl.inserttimetaken = entry.getValue().get(SqlType.INSERT).timeTaken;
				tableStatsDcl.totalcount += tableStatsDcl.insertcount;
				tableStatsDcl.totaltimetaken += tableStatsDcl.inserttimetaken;
			}
			if (entry.getValue().containsKey(SqlType.UPDATE)) {
				tableStatsDcl.updatecount = entry.getValue().get(SqlType.UPDATE).count;
				tableStatsDcl.updatetimetaken = entry.getValue().get(SqlType.UPDATE).timeTaken;
				tableStatsDcl.totalcount += tableStatsDcl.updatecount;
				tableStatsDcl.totaltimetaken += tableStatsDcl.updatetimetaken;
			}
			if (entry.getValue().containsKey(SqlType.DELETE)) {
				tableStatsDcl.deletecount = entry.getValue().get(SqlType.DELETE).count;
				tableStatsDcl.deletetimetaken = entry.getValue().get(SqlType.DELETE).timeTaken;
				tableStatsDcl.totalcount += tableStatsDcl.deletecount;
				tableStatsDcl.totaltimetaken += tableStatsDcl.deletetimetaken;
			}
			resultList.add(tableStatsDcl);
		}

		return resultList;
	}

	private void initializeDashboardTableSummaryStats(DashboardStatsDcl statsDcl) {

		for (Entry<String, Map<SqlType, SQLStatsDcl>> entry : tableSqlStatsDclMap.entrySet()) {
			TableSummaryStatsDcl tableStatsDcl = new TableSummaryStatsDcl();
			tableStatsDcl.tableName = entry.getKey();
			tableStatsDcl.count = 0;
			tableStatsDcl.timetaken = 0L;
			for (SQLStatsDcl sqlStatsDcl : entry.getValue().values()) {
				tableStatsDcl.count += sqlStatsDcl.count;
				tableStatsDcl.timetaken += sqlStatsDcl.timeTaken;
			}
			statsDcl.tableSummaryStatsArray.add(tableStatsDcl);
		}
	}

	private void initializeDashboardHomeSQLStats(DashboardStatsDcl statsDcl) {

		String[] typeArray = new String[] { SqlType.SELECT.name(), SqlType.INSERT.name(), SqlType.UPDATE.name(), SqlType.DELETE.name(), SqlType.UNKNOWN.name() };
		SQLTypeStatsDcl totalSQLTypeStats = new SQLTypeStatsDcl();
		totalSQLTypeStats.type = "Total";
		totalSQLTypeStats.count = 0;
		totalSQLTypeStats.timetaken = new Long(0);
		SQLTypeStatsDcl rowsReadSQLTypeStats = new SQLTypeStatsDcl();
		rowsReadSQLTypeStats.type = "Rows Read";
		rowsReadSQLTypeStats.count = 0;
		rowsReadSQLTypeStats.timetaken = new Long(0);

		for (String type : typeArray) {
			SQLTypeStatsDcl sqlTypeStats = new SQLTypeStatsDcl();
			sqlTypeStats.type = type;
			fillSQLTypeStats(sqlTypeStats, sqlTypeStatMap, totalSQLTypeStats, rowsReadSQLTypeStats);
			statsDcl.sqlTypeStatsArray.add(sqlTypeStats);
		}

		{
			SQLTypeStatsDcl sqlTypeStats = new SQLTypeStatsDcl();
			statsDcl.sqlTypeStatsArray.add(sqlTypeStats);
		}

		{
			statsDcl.sqlTypeStatsArray.add(totalSQLTypeStats);
			statsDcl.sqlTypeStatsArray.add(rowsReadSQLTypeStats);
		}

	}

	private void fillSQLTypeStats(SQLTypeStatsDcl sqlTypeStats, Map<String, SQLStatsDcl> sqlStatsDclMap, SQLTypeStatsDcl totalSQLTypeStats, SQLTypeStatsDcl rowsReadSQLTypeStats) {

		if (sqlStatsDclMap.containsKey(sqlTypeStats.type) == false) {
			sqlTypeStats.count = 0;
			sqlTypeStats.timetaken = new Long(0);
		} else {
			sqlTypeStats.count = sqlStatsDclMap.get(sqlTypeStats.type).count;
			sqlTypeStats.timetaken = sqlStatsDclMap.get(sqlTypeStats.type).timeTaken;
			totalSQLTypeStats.count = totalSQLTypeStats.count + sqlTypeStats.count;
			totalSQLTypeStats.timetaken = totalSQLTypeStats.timetaken + sqlTypeStats.timetaken;
			if (SqlType.SELECT.name().equals(sqlTypeStats.type)) {
				rowsReadSQLTypeStats.count = sqlStatsDclMap.get(sqlTypeStats.type).rowsRead;
				rowsReadSQLTypeStats.timetaken = sqlStatsDclMap.get(sqlTypeStats.type).rowsReadTimeTaken;
			}
		}

	}

}
