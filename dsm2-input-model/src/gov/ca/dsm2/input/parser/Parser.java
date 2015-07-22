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

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the text representation of tables into {@link Tables} containing the
 * same information but easily retrievable in an in memory structure.
 * <p>
 * This is a one-to-one conversion and {@link Tables} to see how to convert this
 * into a higher level structure that organizes the information in a closer
 * representation of the objects in the model
 * 
 * @see Tables
 * @author nsandhu
 * 
 */
public class Parser {
	private Pattern regex;

	/**
	 * Creates an empty parser
	 */
	public Parser() {
		String noQuoteString = "[^\\s\"']+";
		String quotedStringWithEscapedQuotes = "'([^\\\\']+|\\\\([btnfr\"'\\\\]|[0-3]?[0-7]{1,2}|u[0-9a-fA-F]{4}))*'|\"([^\\\\\"]+|\\\\([btnfr\"'\\\\]|[0-3]?[0-7]{1,2}|u[0-9a-fA-F]{4}))*\"";
		regex = Pattern.compile(noQuoteString+"|"+quotedStringWithEscapedQuotes);
	}

	/**
	 * Parses the file and returns the in memory representation as a @see
	 * {@link Tables}
	 * 
	 * @param file
	 *            path to the file
	 * @return Tables
	 * @throws IOException
	 */
	public Tables parseModel(String file) throws IOException {
		return parseModel(new FileInputStream(file));
	}

	/**
	 * Parses the input stream and returns the in memory representation as a @see
	 * {@link Tables}
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public Tables parseModel(InputStream inputStream) throws IOException {
		Tables tables = new Tables();
		parseAndAddToModel(tables, inputStream);
		return tables;
	}

	/**
	 * Use this if tables are to be loaded from multiple files.
	 * 
	 * Note: This does not merge tables named with the same name and is not
	 * supported. Use only to load distinct tables into the same model or handle
	 * that in a custom manner.
	 * 
	 * @param tables
	 * @param inputStream
	 * @throws IOException
	 */
	public void parseAndAddToModel(Tables tables, InputStream inputStream)
			throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
				inputStream));
		InputTable table = null;
		try {
			do {
				table = parseTable(reader);
				if (table == null) {
					break;
				}
				tables.addTable(table);
			} while (true);
		} catch (IOException e) {
			if (!(e instanceof EOFException)) {
				throw e;
			}
		} finally {
			reader.close();
		}
	}

	/**
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public InputTable parseTable(LineNumberReader reader) throws IOException {
		String line = nextLine(reader);
		InputTable table = new InputTable();
		table.setName(line);
		line = nextLine(reader);
		table.setHeaders(getFields(line));
		line = nextLine(reader);
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		while (!line.equals("END")) {
			values.add(getFields(line));
			line = nextLine(reader);
		}
		table.setValues(values);
		return table;
	}

	String nextLine(LineNumberReader reader) throws IOException {
		String line = null;
		do {
			line = reader.readLine();
			if (line != null) {
				line = line.trim();
			} else {
				throw new EOFException("No more lines");
			}
		} while (line.equals("") || line.startsWith("#"));
		line = stripComment(line);
		return line;
	}

	String stripComment(String text) {
		int index = text.indexOf("#");
		if (index < 0) {
			return text;
		} else {
			return text.substring(0, index);
		}
	}

	private final boolean KEEP_QUOTES = true;

	ArrayList<String> getFields(String text) {
		ArrayList<String> matchList = new ArrayList<String>();
		Matcher regexMatcher = regex.matcher(text);
		while (regexMatcher.find()) {
			if (KEEP_QUOTES) {
				matchList.add(regexMatcher.group());
			} else {
				if (regexMatcher.group(1) != null) {
					// Add double-quoted string without the quotes
					matchList.add(regexMatcher.group(1));
				} else if (regexMatcher.group(2) != null) {
					// Add single-quoted string without the quotes
					matchList.add(regexMatcher.group(2));
				} else {
					// Add unquoted word
					matchList.add(regexMatcher.group());
				}
			}
		}
		return matchList;
	}
}
