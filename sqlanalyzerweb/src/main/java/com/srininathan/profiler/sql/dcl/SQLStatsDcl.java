package com.srininathan.profiler.sql.dcl;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SQLStatsDcl implements Serializable {

	public SqlType getType() {

		return type;
	}

	public int getCount() {

		return count;
	}

	public long getTimeTaken() {

		return timeTaken;
	}

	public SQLStatsDcl( SqlType type ) {

		this.type = type;
	}

	public void addCount() {

		count++;
	}

	public void addTimeTaken( long time ) {

		timeTaken = timeTaken + time;
	}

	public void addRowsRead( int rows ) {

		rowsRead = rowsRead + rows;
	}
	
	public void addRowsReadTimeTaken( long time ) {
		rowsReadTimeTaken = rowsReadTimeTaken + time;
	}

	public long getRowsRead() {

		return rowsRead;
	}

	public SqlType type;
	public int count;
	public long timeTaken;
	public int rowsRead;
	public long rowsReadTimeTaken;
}
