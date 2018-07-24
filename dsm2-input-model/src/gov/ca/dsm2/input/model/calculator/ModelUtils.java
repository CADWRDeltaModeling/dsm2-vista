package gov.ca.dsm2.input.model.calculator;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.Channels;
import gov.ca.dsm2.input.model.Node;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionLayer;
import gov.ca.dsm2.input.model.XSectionProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelUtils {
	/*
	 * Calculates the profile from the xsection characteristics, the flow line
	 * of the channel and using the relative distance along channel length to
	 * calculate the position of the xsection line end points
	 */
	public static XSectionProfile calculateProfileFrom(XSection xSection, boolean centerProfile){
		XSectionProfile profile = new XSectionProfile();
		profile.setChannelId(Integer.parseInt(xSection.getChannelId()));
		profile.setDistance(xSection.getDistance());
		List<double[]> endPoints = new ArrayList<double[]>();
		profile.setEndPoints(endPoints);
		//
		List<double[]> profilePoints = new ArrayList<double[]>();
		profile.setProfilePoints(profilePoints);
		double maxWidth = centerProfile ? 0 : getMaxTopWidth(xSection);
		for (XSectionLayer layer : xSection.getLayers()) {
			double w = layer.getTopWidth();
			double[] point1 = new double[2];
			double[] point2 = new double[2];
			point1[0] = maxWidth / 2 - w / 2;
			point1[1] = layer.getElevation();
			if (w > 0) {
				point2[0] = maxWidth / 2 + w / 2;
				point2[1] = layer.getElevation();
			}
			profilePoints.add(0, point1);
			if (w > 0) {
				profilePoints.add(profilePoints.size(), point2);
			}
		}
		return profile;
	}
	/**
	 * Calculates the profile from the xsection characteristics, the flow line
	 * of the channel and using the relative distance along channel length to
	 * calculate the position of the xsection line end points
	 * 
	 * @param xSection
	 * @param channel
	 * @param upNode
	 * @param downNode
	 * @return
	 */
	public static XSectionProfile calculateProfileFrom(XSection xSection) {
		return calculateProfileFrom(xSection, false);
	}

	/**
	 * Calculates the maximum top width in this xsection FIXME: needs to move to
	 * the model class XSection
	 * 
	 * @param xSection
	 * @return the top width
	 */
	public static double getMaxTopWidth(XSection xSection) {
		double width = Double.MIN_VALUE;
		for (XSectionLayer layer : xSection.getLayers()) {
			width = Math.max(width, layer.getTopWidth());
		}
		return width;
	}

	public static double getTopWidthAtDepth(XSection xsection, double depth) {
		ArrayList<XSectionLayer> layers = xsection.getLayers();
		// assumes sorted layers with index 0 being the bottom
		double bottomElevation = layers.get(0).getElevation();
		return bottomElevation + depth;
	}

	public static double getMaxDepth(XSection xsection) {
		ArrayList<XSectionLayer> layers = xsection.getLayers();
		double minElevation = layers.get(0).getElevation();
		double maxElevation = layers.get(layers.size() - 1).getElevation();
		return maxElevation - minElevation;
	}

	public static double getTopWidthAtElevation(XSection xsection,
			double elevation) {
		ArrayList<XSectionLayer> layers = xsection.getLayers();
		double previousElevation = 0;
		double previousTopWidth = 0;
		for (XSectionLayer xSectionLayer : layers) {
			if (elevation < xSectionLayer.getElevation()) {
				return interpolateLinearly(elevation,
						xSectionLayer.getTopWidth(),
						xSectionLayer.getElevation(), previousElevation,
						previousTopWidth);
			}
			previousElevation = xSectionLayer.getElevation();
			previousTopWidth = xSectionLayer.getTopWidth();
		}
		return 0;
	}

	public static double interpolateLinearly(double elevation,
			double thisTopWidth, double thisElevation,
			double previousElevation, double previousTopWidth) {
		return (elevation - previousElevation)
				* (thisTopWidth - previousTopWidth)
				/ (thisElevation - previousElevation) + previousTopWidth;
	}

	public static double getLengthInFeet(double length) {
		return Math.round(length * 3.2808399 * 100) / 100;
	}

	/**
	 * Returns a list of comma separated channel ids for the given node. with
	 * down channels followed by upchannels
	 * 
	 * @param node
	 * @return
	 */
	public static String getChannelsConnectedTo(Channels channels, Node node) {
		String upChannels = channels.getUpChannels(node.getId());
		String downChannels = channels.getDownChannels(node.getId());
		if ((upChannels == null) && (downChannels == null)) {
			return null;
		}
		if (upChannels == null) {
			return downChannels;
		}
		if (downChannels == null) {
			return upChannels;
		}
		return downChannels + "," + upChannels;
	}

	/**
	 * Get channels that have the given nodes as their end points
	 * 
	 * @param channel
	 * @param nodes
	 * @param xSection
	 * @param oldLength
	 */
	public static ArrayList<String> getChannelsWithNodes(Node node1,
			Node node2, Channels channels) {
		ArrayList<String> list = new ArrayList<String>();
		String channels1 = getChannelsConnectedTo(channels, node1);
		if (channels1 == null) {
			return null;
		}
		String[] list1 = channels1.split(",");
		String channels2 = getChannelsConnectedTo(channels, node2);
		if (channels2 == null) {
			return null;
		}
		String[] list2 = channels2.split(",");
		HashMap<String, String> commonList = new HashMap<String, String>();
		for (String element : list1) {
			commonList.put(element, element);
		}
		for (String element : list2) {
			if (commonList.containsKey(element)) {
				list.add(element);
			}
		}
		return list;
	}

	public static Channel getChannelForXSection(XSection xSection,
			Channels channels) {
		String channelId = xSection.getChannelId();
		return channels.getChannel(channelId);
	}

	/**
	 * Generates a list of xsections arranged from upnode to down of the
	 * channel. <br/>
	 * 
	 * The xsections returned are rectangular 1000 ft channels from -10 ft
	 * bottom elevation to 10 ft elevation layer (only 2 layers).<br/>
	 * 
	 * Each channel has a xsection at upnode (0.05 normalized distance) and
	 * downnode (0.95 normalized distance) and atleast one xsection in the
	 * middle. The strategy is to have xsections in the middle (other than the
	 * two at each end ) which are less than 5000 ft apart and are equally
	 * spaced. This is based on the DSM2 computation distance and is
	 * configurable.
	 * 
	 * @param channel
	 * @param upNode
	 * @param downNode
	 * @return
	 */
	public static List<XSection> generateCrossSections(Channel channel,
			Node upNode, Node downNode, double minDistance, double maxWidth) {
		double length = channel.getLength();
		int numberInterior = (int) Math.floor(length / minDistance) + 1;
		double xsectionSpacing = length / (numberInterior + 1);
		ArrayList<XSection> xsections = new ArrayList<XSection>();
		xsections.add(createXSection(channel.getId(), 0.05, maxWidth));
		double distance = 0.0;
		for (int i = 0; i < numberInterior; i++) {
			distance += xsectionSpacing;
			double distanceRatio = Math.round(distance / length * 1000) / 1000.0;
			xsections.add(createXSection(channel.getId(), distanceRatio,
					maxWidth));
		}
		xsections.add(createXSection(channel.getId(), 0.95, maxWidth));
		return xsections;
	}

	public static XSection createXSection(String channelId,
			double normalizedDistance, double maxWidth) {
		// FIXME: add rectangular xsection of 1000 width and -10 to 10 ft
		// elevations. Make this better by adding xsection based on actual
		// width and profile of the
		// dem
		XSection xsection = new XSection();
		xsection.setChannelId(channelId);
		XSectionLayer layer1 = new XSectionLayer();
		layer1.setArea(0);
		layer1.setElevation(-10);
		layer1.setTopWidth(maxWidth);
		layer1.setWettedPerimeter(maxWidth);
		XSectionLayer layer2 = new XSectionLayer();
		layer2.setArea(20 * maxWidth);
		layer2.setElevation(10);
		layer2.setTopWidth(maxWidth);
		layer2.setWettedPerimeter(maxWidth + 40);
		xsection.addLayer(layer1);
		xsection.addLayer(layer2);
		xsection.setDistance(normalizedDistance);
		return xsection;
	}

	public static XSectionProfile getOrCalculateXSectionalProfile(
			XSection xsection) {
		XSectionProfile profileFrom = xsection.getProfile();
		if (profileFrom == null) {
			profileFrom = ModelUtils.calculateProfileFrom(xsection);
			xsection.setProfile(profileFrom);
		}
		return profileFrom;
	}

}
