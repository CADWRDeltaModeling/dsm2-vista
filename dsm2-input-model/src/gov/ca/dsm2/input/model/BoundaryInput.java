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
package gov.ca.dsm2.input.model;

import java.io.Serializable;

/**
 * The boundary input to the model. This generally represented by a file and
 * path that contains the time series providing this information
 * 
 * @author psandhu
 * 
 */
@SuppressWarnings("serial")
public class BoundaryInput implements Serializable {
	/**
	 * Name of boundary input
	 */
	public String name;
	/**
	 * The id of the node at which the input is attached
	 */
	public String nodeId;
	/**
	 * The sign (+ve/-ve) that the input needs to be multiplied by before use
	 */
	public int sign;
	/**
	 * what kind of fillin if any should be used when values may be missing in
	 * the time series, e.g. last value
	 */
	public String fillIn;
	/**
	 * file name in which this input resides
	 */
	public String file;
	/**
	 * a reference to the data within that file (multiple inputs maybe in a
	 * file)
	 */
	public String path;

	/**
	 * The types recognized are sourcesink, flow, stage, ec
	 */
	public String type;

	public BoundaryInput() {
		sign = 1;
	}

}
