package gov.ca.dsm2.input.gis;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.model.Node;

import java.util.ArrayList;
import java.util.List;

public class GisUtil {
	
	public double[] getPointAtDistanceFromUpNode(String id, double distance, DSM2Model model){
		CoordinateConversion cc = new CoordinateConversion();

		Channel channel = model.getChannels().getChannel(id);
		
		List<double[]> latLngPoints = channel.getLatLngPoints();
		List<double[]> utmPoints = new ArrayList<double[]>();
		Node upNode = model.getNodes().getNode(channel.getUpNodeId());
		utmPoints.add(cc.latLng2UTM(upNode.getLatitude(), upNode.getLongitude()));
		for(double[] latLng: latLngPoints){
			utmPoints.add(cc.latLng2UTM(latLng[0], latLng[1]));
		}
		Node downNode = model.getNodes().getNode(channel.getDownNodeId());
		utmPoints.add(cc.latLng2UTM(downNode.getLatitude(), downNode.getLongitude()));
		
		int segmentIndex = findSegmentAtDistance(utmPoints, distance);
		double[] point1 = utmPoints.get(segmentIndex);
		double[] point2 = utmPoints.get(segmentIndex + 1);
		double segmentDistance = findDistanceUptoSegment(segmentIndex,
				utmPoints);
		double[] point0 = findPointAtDistance(point1, point2, distance
				- segmentDistance);
		double[] utm2LatLon = cc.utm2LatLon("10 N "+point0[0]+" "+point0[1]);
		return utm2LatLon;
	}
	
	public List<double[]> calculateXSectRelativeProfile(
			List<double[]> profilePoints, List<double[]> endPoints) {
		List<double[]> relativeProfile = new ArrayList<double[]>();
		double[] origin = endPoints.get(0);
		double[] endPoint = endPoints.get(1);
		double x0 = origin[0];
		double y0 = origin[1];
		double x2 = endPoint[0];
		double y2 = endPoint[1];
		double angle0 = CoordinateGeometryUtils.angle(x0, y0, x2, y2);
		for (double[] point : profilePoints) {
			double length = CoordinateGeometryUtils.distanceBetween(x0, y0,
					point[0], point[1]);
			double angle = CoordinateGeometryUtils.angle(x0, y0, point[0],
					point[1])
					- angle0;
			// length * Math.sin(angle) is ignored i.e. distance to cross
			// section line as its close to zero. maybe check that?
			double distToXSectLine = length * Math.sin(angle);
			if (Math.abs(distToXSectLine) > 5) {
				System.err.println("Distance to profile is more than 5 feet: "
						+ distToXSectLine);
			}
			relativeProfile.add(new double[] { length * Math.cos(angle),
					point[2] });
		}
		return relativeProfile;
	}

	public List<double[]> calculateXSectEndPoints(List<double[]> profilePoints) {
		List<double[]> endPoints = new ArrayList<double[]>();
		endPoints.add(profilePoints.get(0));
		endPoints.add(profilePoints.get(profilePoints.size() - 1));
		return endPoints;
	}

	public List<double[]> calculateEndPoints(
			List<double[]> channelOutlinePoints, double distance, double width) {
		int segmentIndex = findSegmentAtDistance(channelOutlinePoints, distance);
		double[] point1 = channelOutlinePoints.get(segmentIndex);
		double[] point2 = channelOutlinePoints.get(segmentIndex + 1);
		double segmentDistance = findDistanceUptoSegment(segmentIndex,
				channelOutlinePoints);
		double[] point0 = findPointAtDistance(point1, point2, distance
				- segmentDistance);
		double slope = getSlopeBetweenPoints(point1, point2);
		return getLineWithSlopeOfLengthAndCenteredOnPoint(-1 / slope, width,
				point0);
	}

	public int findSegmentAtDistance(List<double[]> segments, double distance) {
		if (distance <= 0) {
			return 0;
		}
		int i = 0;
		double segmentTotalDistance = 0;
		for (i = 0; i < segments.size() - 1; i++) {
			double segmentLength = getLength(segments.get(i + 1), segments
					.get(i));
			segmentTotalDistance += segmentLength;
			if (segmentTotalDistance > distance) {
				break;
			}
		}
		return Math.min(i, segments.size() - 2);
	}

	public List<double[]> getLineWithSlopeOfLengthAndCenteredOnPoint(double m,
			double length, double[] point0) {
		double x0 = point0[0];
		double y0 = point0[1];
		double c = y0 - m * x0;
		double[] pointx = new double[] { x0 + 0.001, m * (x0 + 0.001) + c };
		double distanceFrom = getLength(point0, pointx);
		double ratio = 0.5 * length / distanceFrom;
		double xa = x0 + (pointx[0] - x0) * ratio;
		double ya = m * xa + c;
		double xb = x0 - (pointx[0] - x0) * ratio;
		double yb = m * xb + c;
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[] { xa, ya });
		points.add(new double[] { xb, yb });
		return points;
	}

	public double getSlopeBetweenPoints(double[] point1, double[] point2) {
		double scale = (point2[1] - point1[1]) / (point2[0] - point1[0]);
		return scale;
	}

	public double[] findPointAtDistance(double[] point1, double[] point2,
			double distance) {
		if (distance <= 0) {
			return point1;
		} else if (distance >= getLength(point1, point2)) {
			return point2;
		}
		double distanceBetween = getLength(point1, point2);
		double x1 = point1[0], y1 = point1[1];
		double x2 = point2[0], y2 = point2[1];
		double ratio = distance / distanceBetween;
		double x = x1 + (x2 - x1) * ratio;
		double y = y1 + (y2 - y1) * ratio;
		return new double[] { x, y };
	}

	public double findDistanceUptoSegment(int segmentIndex,
			List<double[]> channelOutlinePoints) {
		if (segmentIndex <= 0) {
			return 0;
		}
		double distance = 0;
		for (int i = 0; i < segmentIndex; i++) {
			distance += getLength(channelOutlinePoints.get(i),
					(channelOutlinePoints.get(i + 1)));
		}
		return distance;
	}

	public double getLength(double[] ds, double[] ds2) {
		return Geometry.length(ds[0], ds[1], ds2[0], ds2[1]);
	}

	public int calculateLength(List<double[]> points) {
		double l = 0;
		int np = points.size();
		for (int i = 1; i < np; i++) {
			double[] p0 = points.get(i - 1);
			double[] p1 = points.get(i);
			l += Geometry.length(p0[0], p0[1], p1[0], p1[1]);
		}
		return (int) Math.round(l);
	}

}
