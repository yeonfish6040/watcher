package com.yeonfish.watcher.util.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLResults {
	protected String[][] data;
	protected int column_count;
	protected int table_nm;
	SQLResults(String[][] data, int column_count) {
		this.data = data;
		this.column_count = column_count;
	}
	
	public boolean isEmpty() {
		if(getRowCount() == 1) {
			return false;
		}else {
			return true;
		}
	}
	
	public String[][] get() {
		return data;
	}
	
	public List<List<String>> getList() {
		List<List<String>> rows = new ArrayList<List<String>>();
		for(String[] e: data) {
			rows.add(Arrays.asList(e));
		}
		return rows;
	}
	
	public int getRowCount() {
		return data.length;
	}
	
	public int getColCount() {
		return column_count;
	}
	
	public String[] split(String spChar) {
		String[] result = new String[data.length];
		int i = 0;
		for(String[] e:data) {
			String str = "";
			for(String e2:e) {
				if (str == "") str += e2;
				else str += spChar+e2;
			}
			result[i] = str;
			i++;
		}
		return result;
	}
	
	public String[][] findCol(int col, String text, Boolean egnoreCase) {
		List<String[]> finded = new ArrayList<String[]>();
		if (egnoreCase) {
			for(String[] s:data) {
				if(s[col].equalsIgnoreCase(text))
					finded.add(s);
			}
		}else {
			for(String[] s:data) {
				if(s[col].equals(text))
					finded.add(s);
			}
		}
		return finded.toArray(new String[getColCount()][getRowCount()]);
	}

	public String getJSON() {
		StringBuffer JSONResult = new StringBuffer();
		JSONResult.append("[");
		for (int i=1;i<data.length;i++) {
			JSONResult.append("{");
			for (int j=0;j<data[0].length;j++) {
				JSONResult.append(data[0][j]+":"+data[i][j]);
				if (data[0].length-1 != j) {
					JSONResult.append(",");
				}
			}
			JSONResult.append("}");
			if (data.length-1 != i) {
				JSONResult.append(",");
			}
		}
		JSONResult.append("]");

		return JSONResult.toString();
	}
	
	public int getTableNo() {
		return table_nm;
	}
}