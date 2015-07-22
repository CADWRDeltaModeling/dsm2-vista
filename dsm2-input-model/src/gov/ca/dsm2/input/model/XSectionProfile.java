package gov.ca.dsm2.input.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the GIS information from which the XSection and XSectionLayer(s) are
 * derived.
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class XSectionProfile implements Serializable {
	private static final int MAX_LAYERS = 10;
	private int id;
	private int channelId;
	private double distance;
	/*
	 * End points are the (x,y) coordinates of the beginning and end points
	 * These coordinates are assumed on a flat projection surface such as UTM
	 */
	private List<double[]> endPoints;
	/*
	 * Profile points are (x,z) coordinates with x being the distance projected 
	 * along the line joining the end points and z being the elevation of these points
	 */
	private List<double[]> profilePoints;

	public XSectionProfile() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public List<double[]> getEndPoints() {
		return endPoints;
	}

	public void setEndPoints(List<double[]> endPoints) {
		this.endPoints = endPoints;
	}

	public List<double[]> getProfilePoints() {
		return profilePoints;
	}

	public void setProfilePoints(List<double[]> profilePoints) {
		this.profilePoints = profilePoints;
	}

	// ---------- calculation methods ------------//
	public List<XSectionLayer> calculateLayers() {
		ArrayList<XSectionLayer> layers = new ArrayList<XSectionLayer>();
		//
		double[] elevations = calculateElevations();
		for (double elevation : elevations) {
			layers.add(calculateLayer(elevation));
		}
		return layers;
	}

	public double getMinimumElevation() {
		double depth = Double.MAX_VALUE;
		for (double[] point : profilePoints) {
			depth = Math.min(depth, point[1]);
		}
		return depth;
	}

	public double getMaximumElevation() {
		double depth = Double.MIN_VALUE;
		for (double[] point : profilePoints) {
			depth = Math.max(depth, point[1]);
		}
		return depth;
	}

	public XSectionLayer calculateLayer(double elevation) {
		double mine = getMinimumElevation();
		double maxe = getMaximumElevation();
		if (elevation > maxe) {
			throw new RuntimeException("Elevation: " + elevation
					+ " is greater than maximum elevation: " + maxe);
		}
		if (elevation < mine) {
			throw new RuntimeException("Elevation: " + elevation
					+ " is less than minimum elevation: " + mine);
		}
		XSectionLayer layer = new XSectionLayer();
		layer.setElevation(elevation);
		double[] previousPoint = null;
		boolean insideChannel = false;
		double area = 0;
		double topWidth = 0;
		double wettedPerimeter = 0;
		for (double[] point : profilePoints) {
			double y = point[1];
			if (!insideChannel
					&& ((previousPoint != null) && (previousPoint[1] >= point[1]))) {
				insideChannel = true;
			}
			if ((y <= elevation) && insideChannel) {
				if (previousPoint[1] > elevation) {
					previousPoint = createPointOnLineAt(elevation,
							previousPoint, point);
				}
				double xp = point[0];
				double xp_1 = previousPoint[0];
				double yp = elevation - point[1];
				double yp_1 = elevation - previousPoint[1];
				double w = xp - xp_1;
				double h = yp - yp_1;
				topWidth += w;
				area += (yp_1 + yp) / 2.0 * w;
				wettedPerimeter += Math.sqrt(h * h + w * w);
			} else if ((y > elevation) && insideChannel) {
				if (previousPoint[1] < elevation) {
					double[] intersectionPoint = createPointOnLineAt(elevation,
							previousPoint, point);
					double xp = intersectionPoint[0];
					double xp_1 = previousPoint[0];
					double yp = elevation - intersectionPoint[1];
					double yp_1 = elevation - previousPoint[1];
					double w = xp - xp_1;
					double h = yp - yp_1;
					topWidth += w;
					area += (yp_1 + yp) / 2.0 * w;
					wettedPerimeter += Math.sqrt(h * h + w * w);

				}
			}
			previousPoint = point;
		}
		layer.setArea(area);
		layer.setTopWidth(topWidth);
		layer.setWettedPerimeter(wettedPerimeter);
		return layer;
	}

	private double[] createPointOnLineAt(double elevation,
			double[] previousPoint, double[] point) {
		double xp = point[0];
		double yp = point[1];
		double xp_1 = previousPoint[0];
		double yp_1 = previousPoint[1];
		if (xp - xp_1 < 1e-8) {
			return new double[] { xp, elevation };
		}
		double m = (yp - yp_1) / (xp - xp_1);
		double c = yp - m * xp;
		return new double[] { (elevation - c) / m, elevation };
	}

	/**
	 * Calculate and return the elevations that are important in defining the
	 * cross section from a DSM2 prespective.
	 * <p>
	 * TODO: Improve this by calculating derivative of slope and finding out
	 * inflexion points where slope changes faster than a certain threshold.
	 * <ul>
	 * <li>calculate rate of change of slope (second difference).</li>
	 * <li>identify elevations where there are changes above a certain threshold
	 * in rate of change of slope</li>
	 * <li>for elevations spaced closer than 2 feet... combine into one such
	 * elevation</li>
	 * </ul>
	 * 
	 * @return
	 */
	public double[] calculateElevations() {
		double minElevation = getMinimumElevation();
		double maxElevation = getMaximumElevation();
		double stepSize = (maxElevation - minElevation) / MAX_LAYERS;
		if (stepSize < 2) {
			stepSize = 2;
		}
		int nlayers = (int) Math.ceil((maxElevation - minElevation) / stepSize) + 1;
		double[] elevations = new double[nlayers];
		for (int i = 0; i < nlayers; i++) {
			elevations[i] = Math.min(minElevation + i * stepSize, maxElevation);
		}
		return elevations;
	}
}
