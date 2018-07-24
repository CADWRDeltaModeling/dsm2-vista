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
package gov.ca.modeling.maps.elevation.client.model;

import java.util.ArrayList;
import java.util.List;

public class CoordinateGeometryUtils {
	/**
	 * Calculates distance between the two points (x1,y1) and (x2,y2)
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double distanceBetween(double x1, double y1, double x2,
			double y2) {
		double delx = x2 - x1;
		double dely = y2 - y1;
		return Math.sqrt(delx * delx + dely * dely);
	}

	/**
	 * Calculates angle formed by line from (x1,y1) to (x2,y2)
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double angle(double x1, double y1, double x2, double y2) {
		double dely = y2 - y1;
		double delx = x2 - x1;
		if ((delx == 0.0) && (dely == 0.0)) {
			return 0;
		}
		return Math.atan(dely / delx);
	}

	/**
	 * Calculate the lengths of projection formed by line between given point
	 * (x,y) and (x1,y1) onto the line formed by (x1,y1) and (x2,y2) as well as
	 * the projection prependicular to the latter line.
	 * 
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return two values, projection along line (x1,y1) to (x2,y2) and
	 *         projection perpendicular to it.
	 */
	public static double[] projectionOfPointOntoLine(double x, double y,
			double x1, double y1, double x2, double y2) {
		double d = distanceBetween(x1, y1, x, y);
		double a = angle(x1, y1, x, y);
		double al = angle(x1, y1, x2, y2);
		double angle = a - al;
		double lineProjection = Math.abs(d * Math.cos(angle));
		double perpendicularProjection = Math.abs(d * Math.sin(angle));
		double[] projection = { lineProjection, perpendicularProjection };
		return projection;
	}

	public static double[] getIntersectionOfLineAndSegments(double x1,
			double y1, double x2, double y2, List<double[]> segmentPoints) {
		
		return null;
	}

	/**
	 * Calculates the points along the path from x1,y1 to x2,y2 where a grid of
	 * the given size intersects it.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param gridSize
	 * @return
	 */
	public static List<DataPoint> getIntersectionOfLineAndGrid(double x1,
			double y1, double x2, double y2, int gridSize) {
		List<DataPoint> points = new ArrayList<DataPoint>();
		double delx = x2 - x1;
		double dely = y2 - y1;
		if (Math.abs(dely) > Math.abs(delx)) {
			double x = x1;
			double y = y1;
			double slope = delx / dely;
			double intercept = x1 - slope * y1;
			double step = Math.signum(dely) * gridSize;
			double nsteps = Math.floor(Math.abs(dely) / gridSize);
			points.add(createPoint(x, y));
			for (int i = 0; i < nsteps; i++) {
				y += step;
				x = slope * y + intercept;
				points.add(createPoint(x, y));
			}
			points.add(createPoint(x2, y2));
		} else {
			double x = x1;
			double y = y1;
			double slope = dely / delx;
			double intercept = y1 - slope * x1;
			double step = Math.signum(delx) * gridSize;
			double nsteps = Math.floor(Math.abs(delx) / gridSize);
			points.add(createPoint(x, y));
			for (int i = 0; i < nsteps; i++) {
				x += step;
				y = slope * x + intercept;
				points.add(createPoint(x, y));
			}
			points.add(createPoint(x2, y2));
		}
		return points;
	}

	private static DataPoint createPoint(double x, double y) {
		DataPoint point = new DataPoint();
		point.x = x;
		point.y = y;
		return point;
	}

	public static int roundDown(double value, int gridSize) {
		return (int) (Math.floor(value / gridSize) * gridSize);
	}
}
