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
import java.util.List;

/**
 * Utility methods which don't really belong to the structures
 * 
 * @author nsandhu
 * 
 */
public class TableUtil {
	/**
	 * Builds the text representation (&lt;latitude&gt;,&lt;longitude&gt;) used
	 * to store location information
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static String fromLatLng(double latitude, double longitude) {
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(latitude).append(",").append(longitude)
				.append(")");
		return builder.toString();
	}

	/**
	 * Builds the text representation location|location|....|location of a list
	 * of points defining a path Also see {@link #fromLatLng(double, double)}
	 * 
	 * @param latlngPoints
	 * @return
	 */
	public static String fromLatLngPoints(List<double[]> latlngPoints) {
		StringBuilder builder = new StringBuilder();
		for (double[] ds : latlngPoints) {
			builder.append("(").append(ds[0]).append(",").append(ds[1]).append(
					")");
			builder.append("|");
		}
		if (builder.length() >= 1) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	/**
	 * Method to parse the longitude of a location in the format
	 * (&lt;latitude&gt;,&lt;longitude&gt;)
	 * 
	 * @param latLng
	 * @return
	 */
	public static double toLongitude(String latLng) {
		String longitude = latLng.substring(latLng.indexOf(",") + 1, latLng
				.indexOf(")"));
		return Double.parseDouble(longitude);
	}

	/**
	 * Method to parse the latitude of a location in the format
	 * (&lt;latitude&gt;,&lt;longitude&gt;)
	 * 
	 * @param latLng
	 * @return
	 */
	public static double toLatitude(String latLng) {
		String latitude = latLng.substring(latLng.indexOf("(") + 1, latLng
				.indexOf(","));
		return Double.parseDouble(latitude);
	}

	/**
	 * a method to parse the list of points in the format defined
	 * {@link #fromLatLngPoints(List)}
	 * 
	 * @param value
	 * @return
	 */
	public static List<double[]> toLatLngPoints(String value) {
		ArrayList<double[]> interiorPoints = new ArrayList<double[]>();
		if (value != null) {
			String[] fieldLatLngs = value.split("\\|");
			for (String fieldLatLng : fieldLatLngs) {
				double[] latLngPoint = new double[2];
				latLngPoint[0] = toLatitude(fieldLatLng);
				latLngPoint[1] = toLongitude(fieldLatLng);
				interiorPoints.add(latLngPoint);
			}
		}
		return interiorPoints;
	}

	public static List<double[]> toProfilePoints(String value) {
		ArrayList<double[]> interiorPoints = new ArrayList<double[]>();
		if (value != null) {
			String[] fieldLatLngs = value.split("\\|");
			for (String fieldLatLng : fieldLatLngs) {
				double[] xypoint = new double[2];
				String s = fieldLatLng.substring(fieldLatLng.indexOf("(") + 1,
						fieldLatLng.indexOf(")"));
				String[] fields = s.split(",");
				xypoint[0] = Double.parseDouble(fields[0]);
				xypoint[1] = Double.parseDouble(fields[1]);
				interiorPoints.add(xypoint);
			}
		}
		return interiorPoints;
	}

	public static String fromProfilePoints(List<double[]> pointsList) {
		StringBuffer buf = new StringBuffer();
		for (double[] point : pointsList) {
			buf.append("(");
			buf.append(point[0]).append(",").append(point[1]);
			buf.append(")");
			buf.append("|");
		}
		return buf.toString();
	}
}
