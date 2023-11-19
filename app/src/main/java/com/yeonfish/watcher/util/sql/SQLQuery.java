package com.yeonfish.watcher.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class SQLQuery {
	protected Connection conn = null;
	protected Statement stmt = null;
	protected ResultSet rs = null;
	protected Exception errTemp = null;
	
	public SQLQuery(String host, String port, String id, String pw, String database) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database, id, pw);
		} catch (Exception e) {
			errTemp = e;
		}
	}
	
	public boolean cStatus() {
		if (conn != null) {
			return true;
		}else {
			errTemp.printStackTrace();
			return false;
		}
	}
	
	public Connection getConn() {
		return conn;
	}
	
	public int insert(String table, String[][] get) throws Exception {
		
		String[] clean = getClean(get);
		
		String col = clean[0];
		String val = clean[1].replaceAll("`", "'");
		
		stmt = conn.createStatement();
		String sql = "INSERT INTO "+table+"("+col+") VALUES("+val+")";
		int rows = stmt.executeUpdate(sql);
		return rows;
	}
	
	public int update(String table, String[][] get, int index) throws Exception {
		
		if (get[0].length != get[1].length) throw new Exception("Error! The number of columns does not match the number of values.");
		
		
		String temp = "";
		for(int i=0;i<get[0].length;i++) {
			if (temp == "") {
				temp = get[0][i]+"='"+get[1][i]+"'"; 
			}else {
				temp += ", "+get[0][i]+"='"+get[1][i]+"'";
			}
		}
		
		stmt = conn.createStatement();
		String sql = "UPDATE "+table+" SET "+temp+" WHERE "+get[0][index]+"='"+get[1][index]+"'";
		int rows = stmt.executeUpdate(sql);
		return rows;
	}
	
	public int delete(String table, String condition) throws Exception {
		String sql = "DELETE FROM "+table+" WHERE "+condition;
		stmt = conn.createStatement();
		int rows = stmt.executeUpdate(sql);
		return rows;
	}
	
	public int update(String sql) throws Exception {
		stmt = conn.createStatement();
		int rows = stmt.executeUpdate(sql);
		return rows;
	}
	
	public SQLResults query(String sql) throws Exception {
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		int column_count = rsmd.getColumnCount();
		rs.last();
		int row_count = rs.getRow();
		rs.beforeFirst();
		String[][] result = new String[row_count+1][column_count];
		for(int i=0;i<column_count;i++) {
			result[0][i] = rsmd.getColumnName(i+1);
		}
		int j = 1;
		while(rs.next()) {
			for(int i=0;i<column_count;i++) {
				result[j][i] = rs.getString(i+1);
			}
			j++;
		}
		SQLResults re = new SQLResults(result, column_count);
		return re;
	}
	
	protected String[] getClean(String[][] raw) {
		String[] vals = new String[raw.length];
		int i = 0;
		for(String[] raw2:raw) {
			for(String c:raw2) {
				if(vals[i] == null) {
					vals[i] = "`"+c+"`";
				}else {
					vals[i] += ", `"+c+"`";
				}
			}
			i++;
		}
		return vals;
	}
}