/**
 *    Copyright (C) 2009, 2010 
 *    State of California,
 *    Department of Water Resources.
 *    This file is part of DSM2 Grid Map
 *    The DSM2 Grid Map is free software: 
 *    you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    DSM2 Grid Map is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details. [http://www.gnu.org/licenses]
 *    
 *    @author Nicky Sandhu
 *    
 */
package gov.ca.dsm2.input.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This represents an input table structure. Each table is assumed to have a
 * name and a list of header names. This is followed by rows of values where
 * each row has a value for each header.
 * 
 * @author psandhu
 * 
 */
public class InputTable {
	private String name;
	private ArrayList<String> headers;
	private  HashMap<String, Integer> headerIndexMap;
	private ArrayList<ArrayList<String>> values;

	/**
	 * Initializes a no name table with no headers, no values
	 */
	public InputTable() {
		name = "";
		headers = new ArrayList<String>();
		headerIndexMap = new HashMap<String, Integer>();
		values = new ArrayList<ArrayList<String>>();
	}

	/**
	 * Retrieves the value at row (indexed from 0.. length-1) and under named
	 * header
	 * 
	 * @param index
	 *            of row [0 indexed]
	 * @param headerName
	 *            name of header
	 * @return the string value in that cell
	 */
	public String getValue(int index, String headerName) {
		Integer headerIndex = headerIndexMap.get(headerName);
		if (headerIndex == null) {
			throw new IllegalArgumentException("No header with name: "
					+ headerName + " in table: " + name);
		}
		if (headerIndex.intValue() >= values.get(index).size()) {
			return null;
		}
		return values.get(index).get(headerIndex.intValue());
	}

	/**
	 * Name of this table
	 * 
	 * @return name of table
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the name of this table
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 */
	public int getNumberOfRows(){
		return values != null ? values.size() : 0;
	}
	/**
	 * Gets the array of headers in order
	 * 
	 * @return
	 */
	public ArrayList<String> getHeaders() {
		return headers;
	}

	/**
	 * Sets header by copying from this list and also getting ready to receive
	 * values that will be indexed by header name
	 * 
	 * @see #getValue(int, String)
	 * @param list
	 */
	public void setHeaders(List<String> list) {
		headers = new ArrayList<String>();
		headers.addAll(list);
		headerIndexMap.clear();
		int index = 0;
		for (String header : list) {
			headerIndexMap.put(header, new Integer(index));
			index++;
		}
	}

	/**
	 * Returns the actual list used to store values. Be careful as manipulating
	 * this list changes this tables values for other current users
	 * 
	 * @return reference to the value array
	 */
	public ArrayList<ArrayList<String>> getValues() {
		return values;
	}

	/**
	 * Changes the reference held internally to the array passed in. Be careful
	 * as this affects other current users
	 * 
	 * @param values
	 */
	public void setValues(ArrayList<ArrayList<String>> values) {
		this.values = values;
	}

	/**
	 * Builds a string representation of this table (very similar to the
	 * original parsed in except for perhaps whitespace differences). This can
	 * be used in conjunction with a print that honors "\n" for end of line and
	 * prints the table to the desired output
	 * 
	 * @return
	 */
	public String toStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName()).append("\n");
		writeRow(builder, getHeaders());
		builder.append("\n");
		for (ArrayList<String> valueRow : getValues()) {
			writeRow(builder, valueRow);
			builder.append("\n");
		}
		builder.append("END\n");
		return builder.toString();
	}

	private void writeRow(StringBuilder builder, ArrayList<String> valueRow) {
		for (String value : valueRow) {
			builder.append(value).append("\t");
		}
		builder.deleteCharAt(builder.length() - 1);
	}

	/**
	 * shows just the name of the table. For a complete string representation of
	 * the input @see {@link #toStringRepresentation()}
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Input Table: ").append(getName());
		return builder.toString();
	}
}
