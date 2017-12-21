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

import java.util.List;

public class GeomUtils {
	/**
	 * For a given line represented as segments, find the index to the segment
	 * that contains the distance from the index 0
	 * 
	 * @param segments
	 * @param distance
	 * @return
	 */
	public static int findSegmentAtDistance(LatLng[] segments, double distance) {
		if (distance <= 0) {
			return 0;
		}
		int i = 0;
		double segmentTotalDistance = 0;
		for (i = 0; i < segments.length - 1; i++) {
			double segmentLength = segments[i + 1].distanceFrom(segments[i]);
			segmentTotalDistance += getLengthInFeet(segmentLength);
			if (segmentTotalDistance > distance) {
				break;
			}
		}
		return Math.min(i, segments.length - 2);
	}

	/**
	 * Finds a point at a distance from point 1 along the line between point 1
	 * and point 2
	 * 
	 * @param point1
	 * @param point2
	 * @param distance
	 * @return
	 */
	public static LatLng findPointAtDistance(LatLng point1, LatLng point2,
			double distance) {
		if (distance <= 0) {
			return point1;
		} else if (distance >= getLengthInFeet(point1.distanceFrom(point2))) {
			return point2;
		}
		double distanceBetween = getLengthInFeet(point1.distanceFrom(point2));
		double x1 = point1.getLatitude(), y1 = point1.getLongitude();
		double x2 = point2.getLatitude(), y2 = point2.getLongitude();
		double ratio = distance / distanceBetween;
		double x = x1 + (x2 - x1) * ratio;
		double y = y1 + (y2 - y1) * ratio;
		return LatLng.newInstance(x, y);
	}

	/**
	 * Gets the starting and ending points in an array of points of length 2
	 * that define a line with slope m and length and have a mid point of the
	 * given point0
	 * 
	 * @param m
	 * @param length
	 * @param point0
	 * @return
	 */
	public static LatLng[] getLineWithSlopeOfLengthAndCenteredOnPoint(double m,
			double length, LatLng point0) {
		LatLng[] points = new LatLng[2];
		double x0 = point0.getLatitude();
		double y0 = point0.getLongitude();
		double c = y0 - m * x0;
		LatLng pointx = LatLng.newInstance(x0 + 0.001, m * (x0 + 0.001) + c);
		double distanceFrom = getLengthInFeet(point0.distanceFrom(pointx));
		double ratio = 0.5 * length / distanceFrom;
		double xa = x0 + (pointx.getLatitude() - x0) * ratio;
		double ya = m * xa + c;
		double xb = x0 - (pointx.getLatitude() - x0) * ratio;
		double yb = m * xb + c;
		points[0] = LatLng.newInstance(xa, ya);
		points[1] = LatLng.newInstance(xb, yb);
		return points;
	}

	/**
	 * Distance upto this index but not including the distance of the segment
	 * begining with this index
	 * 
	 * @param segmentIndex
	 * @param channelOutlinePoints
	 * @return
	 */
	public static double findDistanceUptoSegment(int segmentIndex,
			LatLng[] channelOutlinePoints) {
		if (segmentIndex <= 0) {
			return 0;
		}
		double distance = 0;
		for (int i = 0; i < segmentIndex; i++) {
			distance += getLengthInFeet(channelOutlinePoints[i]
					.distanceFrom(channelOutlinePoints[i + 1]));
		}
		return distance;
	}

	public static double getSlopeBetweenPoints(LatLng point1, LatLng point2) {
		double rawScale = (point2.getLongitude() - point1.getLongitude())
				/ (point2.getLatitude() - point1.getLatitude());
		double scale = rawScale * getAspectRatio(point1, point2);
		return scale;
	}

	public static double getAspectRatio(LatLng point1, LatLng point2) {
		double latScale = LatLng.newInstance(point1.getLatitude() + 0.001,
				point1.getLongitude()).distanceFrom(point1);
		double lngScale = LatLng.newInstance(point1.getLatitude(),
				point1.getLongitude() + 0.001).distanceFrom(point1);
		return lngScale / latScale;
	}

	public static double getLengthInFeet(double length) {
		return Math.round(length * 3.2808399 * 100) / 100;
	}

	public static double getLengthInMeters(double lengthInFeet) {
		return Math.round(lengthInFeet * 0.3048);
	}

	public static void moveOriginAndProjectOntoLineAndConvertToFeet(
			List<? extends DataPoint> points, DataPoint origin,
			DataPoint endPoint) {
		double x0 = origin.x;
		double y0 = origin.y;
		double x2 = endPoint.x;
		double y2 = endPoint.y;
		double angle0 = CoordinateGeometryUtils.angle(x0, y0, x2, y2);
		for (DataPoint point : points) {
			double length = GeomUtils.getLengthInFeet(CoordinateGeometryUtils
					.distanceBetween(x0, y0, point.x, point.y));
			double angle = CoordinateGeometryUtils.angle(x0, y0, point.x,
					point.y)
					- angle0;
			point.x = length * Math.cos(angle);
			point.y = length * Math.sin(angle);
		}
	}

	public static double[] calculateUTMFromPointAtFeetDistanceAlongLine(
			double distance, DataPoint origin, DataPoint endPoint) {
		double x0 = origin.x;
		double y0 = origin.y;
		double x2 = endPoint.x;
		double y2 = endPoint.y;
		double totalLength = CoordinateGeometryUtils.distanceBetween(x0, y0,
				x2, y2);
		double lengthInMeters = GeomUtils.getLengthInMeters(distance);
		return Geometry.computePointOnLine(x0, y0, x2, y2, lengthInMeters
				/ totalLength);
	}

	public static double[] convertToUTM(double latitude, double longitude) {
		CoordinateConversion cc = new CoordinateConversion();
		String latLon2UTM = cc.latLon2UTM(latitude, longitude);
		String[] split = latLon2UTM.split("\\s");
		double x = Double.parseDouble(split[2]);
		double y = Double.parseDouble(split[3]);
		return new double[] { x, y };
	}

	public static double[] convertToLatLng(double utmx, double utmy) {
		CoordinateConversion cc = new CoordinateConversion();
		double[] utm2LatLon = cc.utm2LatLon("10 N " + utmx + " " + utmy);
		return utm2LatLon;
	}

}
