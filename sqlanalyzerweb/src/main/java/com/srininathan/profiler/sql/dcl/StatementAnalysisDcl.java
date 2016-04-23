package com.srininathan.profiler.sql.dcl;

import java.util.ArrayList;
import java.util.List;


public class StatementAnalysisDcl {
	
	public String uniqueIdentifier;
	public SqlType sqlType;
	public String sql;
	public String formattedSQL;
	public List<String> statementIdentifierList = new ArrayList<String>();
	public List<String> tableList = new ArrayList<String>();
	
	
}
