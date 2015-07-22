package gov.ca.dsm2.input.model.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 *** Ramer Douglas Peucker

 The Ramer-Douglas–Peucker algorithm is an algorithm for reducing the number of points in a curve that is approximated by a series of points. 
 It does so by "thinking" of a line between the first and last point in a set of points that form the curve. 
 It checks which point in between is farthest away from this line. 
 If the point (and as follows, all other in-between points) is closer than a given distance 'epsilon', it removes all these in-between points. 
 If on the other hand this 'outlier point' is farther away from our imaginary line than epsilon, the curve is split in two parts. 
 The function is recursively called on both resulting curves, and the two reduced forms of the curve are put back together.

 1) From the first point up to and including the outlier
 2) The outlier and the remaining points.


 *** Bad implementations on the web
 On the web I found many Ramer Douglas Peucker implementations, but most of the top results on google contained bugs. 
 Even the original example on Wikipedia was BAD! 
 The bugs were ranging from bad calculation of the perpendicular distance of a point to a line (often they contained a devide by zero error for vertical lines), 
 to discarding points that should not be removed at all. 
 To see this in action, just try running the algorithm on it's own result with the same epsilon, 
 many implementations will keep on reducing more and more points until there is no spline left. 
 A correct implementation of RDP will remove *all* points that it can remove given a certain epsilon in the first run.

 I hope that by looking at this source code for my Ramer Douglas Peucker implementation you will be able to get a correct reduction of your dataset.

 @licence Feel free to use it as you please, a mention of my name is always nice.

 Marius Karthaus
 http://www.LowVoice.nl

 * 
 */
public class RamerDouglasPeucker {

	public static List<double[]> properRDP(List<double[]> points, double epsilon) {

		double[] firstPoint = points.get(0);
		double[] lastPoint = points.get(points.size() - 1);
		if (points.size() < 3) {
			return points;
		}
		int index = -1;
		double dist = 0;
		for (int i = 1; i < points.size() - 1; i++) {
			double cDist = findPerpendicularDistance(points.get(i), firstPoint,
					lastPoint);
			if (cDist > dist) {
				dist = cDist;
				index = i;
			}
		}
		if (dist > epsilon) {
			// iterate
			List<double[]> l1 = slice(points, 0, index + 1);
			List<double[]> l2 = slice(points, index);
			List<double[]> r1 = properRDP(l1, epsilon);
			List<double[]> r2 = properRDP(l2, epsilon);
			// concat r2 to r1 minus the end/startpoint that will be the same
			List<double[]> rs = concat(slice(r1, 0, r1.size() - 1), r2);
			return rs;
		} else {
			ArrayList<double[]> list = new ArrayList<double[]>();
			list.add(firstPoint);
			list.add(lastPoint);
			return list;
		}
	}

	public static List<double[]> slice(List<double[]> list, int startIndex,
			int endIndex) {
		ArrayList<double[]> newList = new ArrayList<double[]>();
		for (int i = startIndex; i < endIndex; i++) {
			newList.add(list.get(i));
		}
		return newList;
	}

	public static List<double[]> slice(List<double[]> list, int startIndex) {
		return slice(list, startIndex, list.size());
	}

	public static List<double[]> concat(List<double[]> list1,
			List<double[]> list2) {
		ArrayList<double[]> newList = new ArrayList<double[]>(list1);
		newList.addAll(list2);
		return newList;
	}

	public static double findPerpendicularDistance(double[] p, double[] p1,
			double[] p2) {
		// if start and end point are on the same x the distance is the
		// difference in X.
		double result;
		double slope;
		double intercept;
		if (p1[0] == p2[0]) {
			result = Math.abs(p[0] - p1[0]);
		} else {
			slope = (p2[1] - p1[1]) / (p2[0] - p1[0]);
			intercept = p1[1] - (slope * p1[0]);
			result = Math.abs(slope * p[0] - p[1] + intercept)
					/ Math.sqrt(Math.pow(slope, 2) + 1);
		}

		return result;
	}
}
