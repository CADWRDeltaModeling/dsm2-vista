package gov.ca.dsm2.input.csdp;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.ca.dsm2.input.gis.CoordinateGeometryUtils;
import gov.ca.dsm2.input.gis.GeomUtils;
import gov.ca.dsm2.input.gis.Geometry;
import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.Channels;
import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.model.Node;
import gov.ca.dsm2.input.model.Nodes;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionProfile;
import gov.ca.dsm2.input.model.calculator.ModelUtils;

/**
 * Reads in the file in .cdn format
 * 
 * @author psandhu
 * 
 */
public class CDNReader {
	private String file;

	public CDNReader(String cdnFile) {
		file = cdnFile;
	}

	public void readAndUpdateModel(DSM2Model dsm2Model) throws IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(file));
		String line = null;
		int numberOfChannels = 0;
		Channel c = null;
		XSection xsect = null;
		boolean readingChannelOutline = false;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(";")) {
				continue;
			}
			if (line.contains("*nl*")) {
				continue;
			}
			line = line.trim();
			String[] fields = line.split("\\s");
			if (line.startsWith("Version")) {
				numberOfChannels = Integer.parseInt(fields[1]);
			} else {
				if (fields[0].equals("\"\"")) { // xsection
					int numberPoints = Integer.parseInt(fields[1]);
					if (readingChannelOutline) {
						readingChannelOutline = false;
					}
					XSectionProfile xsProfile = new XSectionProfile();
					List<double[]> profilePoints = new ArrayList<double[]>();
					for (int i = 0; i < numberPoints; i++) {
						line = reader.readLine();
						String[] split = line.split(",");
						profilePoints.add(new double[] {
								Double.parseDouble(split[0]),
								Double.parseDouble(split[1]),
								Double.parseDouble(split[2]) });
					}
					line = reader.readLine();
					line = line.trim();
					String[] split = line.split("\\s");
					line = reader.readLine();// discard one more line following
					// xsect width info
					if (profilePoints.size() == 0) {
						continue;
					}
					double distanceAlongCenterline = Double
							.parseDouble(split[0]);
					if (c == null) {
						continue;
					}
					double dist = distanceAlongCenterline / c.getLength();
					List<double[]> endPoints = calculateXSectEndPoints(profilePoints);
					double xsectLineLength = calculateLength(endPoints);
					profilePoints = calculateXSectRelativeProfile(
							profilePoints, endPoints);
					xsProfile.setProfilePoints(profilePoints);
					xsProfile.setChannelId(Integer.parseInt(c.getId()));
					xsect = c.getXSectionAt(dist);
					boolean newXSection = false;
					if (xsect == null) {
						xsect = ModelUtils.createXSection(c.getId(), dist,
								xsectLineLength);
						c.addXSection(xsect);
						newXSection = true;
					}
					xsect.setProfile(xsProfile);
					xsProfile.setDistance(dist);
					xsProfile.setEndPoints(endPoints);
					if (newXSection) {
						xsect.setLayers(xsProfile.calculateLayers());
					}
				} else if (fields[0].startsWith("\"")) {// channel outline
					String chId = fields[0].replace("\"", "");
					c = dsm2Model.getChannels().getChannel(chId);
					int nPoints = Integer.parseInt(fields[1]);
					List<double[]> points = new ArrayList<double[]>();
					for (int i = 0; i < nPoints; i++) {
						line = reader.readLine();
						String[] split = line.split(",");
						points.add(new double[] { Double.parseDouble(split[0]),
								Double.parseDouble(split[1]) });
					}
					line = reader.readLine(); // number of xsections expected
					if (c == null) {
						continue;
					}
					c.setLatLngPoints(points);
					int oldLength = c.getLength();
					c.setLength(calculateLength(points));
					System.out.println("Channel: " + c.getId() + " length: "
							+ oldLength + " -> " + c.getLength());
				} else if ((fields.length == 1) && fields[0].equals("2")) {
					c = null;
				} else if (readingChannelOutline) {
				}
			}
		}
		reader.close();
		// for each node, find connecting channels points
		// calculate node position as average of derived pos on channel
		// centerline
		// trim end points from centerline to indicate only interior points of
		// channels
		Channels channels = dsm2Model.getChannels();
		HashMap<String, List<double[]>> nodeLatLngs = new HashMap<String, List<double[]>>();
		for (Node n : dsm2Model.getNodes().getNodes()) {
			ArrayList<String> channelsWithNodes = ModelUtils
					.getChannelsWithNodes(n, n, channels);
			if ((channelsWithNodes == null) || (channelsWithNodes.size() == 0)) {
				continue;
			}
			for (String cid : channelsWithNodes) {
				Channel channel = channels.getChannel(cid);
				if ((channel.getLatLngPoints() == null)
						|| (channel.getLatLngPoints().size() == 0)) {
					System.out.println("No outline information for channel: "
							+ channel.getId());
					continue;
				}
				String uId = channel.getUpNodeId();
				String dId = channel.getDownNodeId();
				List<double[]> list = nodeLatLngs.get(n.getId());
				if (list == null) {
					nodeLatLngs.put(n.getId(), new ArrayList<double[]>());
				}
				if (n.getId().equals(uId)) {
					nodeLatLngs.get(n.getId()).add(
							channel.getLatLngPoints().get(0));
				}
				if (n.getId().equals(dId)) {
					nodeLatLngs.get(n.getId()).add(
							channel.getLatLngPoints().get(
									channel.getLatLngPoints().size() - 1));
				}
			}
		}
		Nodes nodes = dsm2Model.getNodes();
		for (String nId : nodeLatLngs.keySet()) {
			List<double[]> latLngs = nodeLatLngs.get(nId);
			if ((latLngs == null) || (latLngs.size() == 0)) {
				continue;
			}
			double[] avg = new double[2];
			for (double[] p : latLngs) {
				avg[0] += p[0];
				avg[1] += p[1];
			}
			avg[0] = avg[0] / latLngs.size();
			avg[1] = avg[1] / latLngs.size();
			Node node = nodes.getNode(nId);
			double[] latLng = GeomUtils.convertToLatLng(avg[0] * 0.3048,
					avg[1] * 0.3048);
			node.setLatitude(latLng[0]);
			node.setLongitude(latLng[1]);
		}

		for (Channel channel : dsm2Model.getChannels().getChannels()) {
			List<double[]> latLngPoints = channel.getLatLngPoints();
			if ((latLngPoints == null) || (latLngPoints.size() < 2)) {
				if (latLngPoints.size() == 1) {
					System.out.println("Channel : " + channel.getId()
							+ " has no interior points");
				}
				continue;
			}
			latLngPoints.remove(0);
			latLngPoints.remove(latLngPoints.size() - 1);
			List<double[]> interiorPoints = new ArrayList<double[]>();
			for (double[] point : latLngPoints) {
				double[] latLng = GeomUtils.convertToLatLng(point[0] * 0.3048,
						point[1] * 0.3048);
				interiorPoints.add(latLng);
			}
			channel.setLatLngPoints(interiorPoints);
			for (XSection xsection : channel.getXsections()) {
				XSectionProfile profile = xsection.getProfile();
				if (profile == null) {
					continue;
				}
				List<double[]> endPoints = new ArrayList<double[]>();
				for (double[] point : profile.getEndPoints()) {
					endPoints.add(GeomUtils.convertToLatLng(point[0] * 0.3048,
							point[1] * 0.3048));
				}
				profile.setEndPoints(endPoints);
			}
		}

	}

	private List<double[]> calculateXSectRelativeProfile(
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

	private List<double[]> calculateXSectEndPoints(List<double[]> profilePoints) {
		List<double[]> endPoints = new ArrayList<double[]>();
		endPoints.add(profilePoints.get(0));
		endPoints.add(profilePoints.get(profilePoints.size() - 1));
		return endPoints;
	}

	private List<double[]> calculateEndPoints(
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

	private int findSegmentAtDistance(List<double[]> segments, double distance) {
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

	private List<double[]> getLineWithSlopeOfLengthAndCenteredOnPoint(double m,
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

	private double getSlopeBetweenPoints(double[] point1, double[] point2) {
		double scale = (point2[1] - point1[1]) / (point2[0] - point1[0]);
		return scale;
	}

	private double[] findPointAtDistance(double[] point1, double[] point2,
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

	private double findDistanceUptoSegment(int segmentIndex,
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

	private double getLength(double[] ds, double[] ds2) {
		return Geometry.length(ds[0], ds[1], ds2[0], ds2[1]);
	}

	private int calculateLength(List<double[]> points) {
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
