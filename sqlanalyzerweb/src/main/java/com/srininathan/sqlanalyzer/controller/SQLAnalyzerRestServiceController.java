package com.srininathan.sqlanalyzer.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.srininathan.profiler.rmi.AgentDataCollector;
import com.srininathan.profiler.sql.bll.BasicFormatterImpl;
import com.srininathan.profiler.sql.bll.SQLAnalyzerBll;
import com.srininathan.sqlanalyzer.dcl.SQLAnalyzerResultDcl;

@RestController
public class SQLAnalyzerRestServiceController {

	@Autowired
	private AgentDataCollector agentDataCollector;

	@Autowired
	private SQLAnalyzerBll sqlAnalyzerBll;

	private Map<DeferredResult<SQLAnalyzerResultDcl>, StatsDetail> deferredCallMap = new HashMap<>();

	private long lastUpdateTime = System.nanoTime();
	private static final long ajaxTimeOut = 60000L;

	@RequestMapping(value = "/isAnalyzerRunning", produces = "application/json", method = RequestMethod.GET)
	public Map<String, Object> isAnalyzerRunning() {

		boolean listening = false;
		int portNumber = 0;

		listening = agentDataCollector.isProfilingOn();
		if (listening == true) {
			portNumber = agentDataCollector.getPortNumber();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("port", portNumber);
		resultMap.put("listening", listening);
		return resultMap;
	}

	@RequestMapping(value = "/startAnalyzer", produces = "application/json", method = RequestMethod.POST)
	public Map<String, Object> startAnalyzer(@RequestBody int port) {

		String error = null;
		boolean success = true;
		try {
			agentDataCollector.start(port);
		} catch (Exception e) {
			success = false;
			error = e.getMessage();
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("port", port);
		resultMap.put("listening", success);
		resultMap.put("error", error);
		return resultMap;
	}

	@RequestMapping(value = "/dashboardHomeStats", produces = "application/json", method = RequestMethod.GET)
	public DeferredResult<SQLAnalyzerResultDcl> dashboardHomeStats(@RequestParam String lastUpdateTime) {

		StatsDetail statsDetail = new StatsDetail();
		statsDetail.propertyName = "dashboardStats";
		return getStats(statsDetail, lastUpdateTime);

	}

	@RequestMapping(value = "/detailedTableStats", produces = "application/json", method = RequestMethod.GET)
	public DeferredResult<SQLAnalyzerResultDcl> getDetailedTableStats(@RequestParam String lastUpdateTime) {

		StatsDetail statsDetail = new StatsDetail();
		statsDetail.propertyName = "detailedTableStats";
		return getStats(statsDetail, lastUpdateTime);
	}

	@RequestMapping(value = "/ps/table/{tablename}", produces = "application/json", method = RequestMethod.GET)
	public DeferredResult<SQLAnalyzerResultDcl> getPreparedStatementByTable(@PathVariable(value = "tablename") String tablename, @RequestParam String lastUpdateTime) {

		StatsDetail statsDetail = new StatsDetail();
		statsDetail.propertyName = "preparedStatementStats";
		statsDetail.parameters = new Object[1];
		statsDetail.parameters[0] = tablename;
		return getStats(statsDetail, lastUpdateTime);

	}

	@RequestMapping(value = "/sql/format", produces = "application/json", method = RequestMethod.POST)
	public Map<String, String> formatSQL(@RequestBody String sql) {

		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("formattedSQL", new BasicFormatterImpl().format(sql));
		return resultMap;
	}

	private DeferredResult<SQLAnalyzerResultDcl> getStats(StatsDetail statsDetail, String clientLastUpdateTime) {

		long clientUpdateTime = Long.parseLong(clientLastUpdateTime);

		if (lastUpdateTime > clientUpdateTime) {
			DeferredResult<SQLAnalyzerResultDcl> deferredResult = new DeferredResult<SQLAnalyzerResultDcl>();
			SQLAnalyzerResultDcl resultDcl = new SQLAnalyzerResultDcl();
			resultDcl.newUpdates = true;
			resultDcl.lastUpdateTime = Long.toString(lastUpdateTime);
			try {
				String methodName = "get" + statsDetail.propertyName.substring(0, 1).toUpperCase() + statsDetail.propertyName.substring(1);
				Object result = MethodUtils.invokeMethod(sqlAnalyzerBll, methodName, statsDetail.parameters);
				FieldUtils.writeField(resultDcl, statsDetail.propertyName, result);
			} catch (Exception e) {
				e.printStackTrace();
			}
			deferredResult.setResult(resultDcl);
			return deferredResult;
		}

		SQLAnalyzerResultDcl resultDcl = new SQLAnalyzerResultDcl();
		resultDcl.lastUpdateTime = Long.toString(lastUpdateTime);
		final DeferredResult<SQLAnalyzerResultDcl> deferredResult = new DeferredResult<SQLAnalyzerResultDcl>(ajaxTimeOut, resultDcl);
		deferredResult.onCompletion(new Runnable() {
			@Override
			public void run() {
				deferredCallMap.remove(deferredResult);
			}
		});

		synchronized (deferredCallMap) {
			// Add the deferred result to the map.
			if (deferredResult.isSetOrExpired() == false)
				deferredCallMap.put(deferredResult, statsDetail);
		}
		return deferredResult;
	}

	public synchronized void pushResults() {

		lastUpdateTime = System.nanoTime();

		synchronized (deferredCallMap) {
			if (!deferredCallMap.isEmpty()) {
				for (Entry<DeferredResult<SQLAnalyzerResultDcl>, StatsDetail> entry : deferredCallMap.entrySet()) {
					DeferredResult<SQLAnalyzerResultDcl> deferredResult = entry.getKey();
					StatsDetail statsDetail = entry.getValue();
					if (deferredResult.isSetOrExpired() == false) {
						SQLAnalyzerResultDcl resultDcl = new SQLAnalyzerResultDcl();
						resultDcl.lastUpdateTime = Long.toString(lastUpdateTime);
						resultDcl.newUpdates = true;
						try {
							String methodName = "get" + statsDetail.propertyName.substring(0, 1).toUpperCase() + statsDetail.propertyName.substring(1);
							Object result = MethodUtils.invokeMethod(sqlAnalyzerBll, methodName, statsDetail.parameters);
							FieldUtils.writeField(resultDcl, statsDetail.propertyName, result);
						} catch (Exception e) {
							e.printStackTrace();
						}
						deferredResult.setResult(resultDcl);
					}
				}
				deferredCallMap.clear();
			}
		}

	}

	private class StatsDetail {

		public String propertyName;
		public Object[] parameters;
	}

}
