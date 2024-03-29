/**
 *   Copyright (C) 2009, 2010 
 *    Nicky Sandhu
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
 *    GNU General Public License for more details.

 *    You should have received a copy of the GNU General Public License
 *    along with DSM2 Grid Map.  If not, see <http://www.gnu.org/licenses>.
 */
package gov.ca.dsm2.input.gis;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BathymetryDataPoint extends DataPoint implements Serializable {
	public int year;
	public String agency;

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(x).append(",").append(y).append(",").append(z);
		builder.append(year).append(",").append(agency);
		return builder.toString();
	}
}
