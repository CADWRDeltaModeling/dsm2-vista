package gov.ca.dsm2.input.model.calculator;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionLayer;
import gov.ca.dsm2.input.model.XSectionProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * A higher level calculator for DSM2 Model. It could be used to calculate
 * volume of channels, average bottom elevation of reservoirs
 * 
 * @author nsandhu
 * 
 */
public class ModelCalculator {

	public double calculateVolumeAtElevation(Channel channel, double elevation) {
		ArrayList<XSection> interpolatedXSections = calculateZInterpolatedXSections(channel);
		double volume = 0.0;

		return volume;
	}

	public ArrayList<XSection> calculateZInterpolatedXSections(Channel channel) {
		// contains the rounded values of elevation unique to the second decimal
		// place
		HashMap<Long, XSectionLayer> mapRoundedElevationToXSectionLayer = new HashMap<Long, XSectionLayer>();
		// first pass to collect distinct elevations
		for (XSection xsection : channel.getXsections()) {
			ArrayList<XSectionLayer> layers = xsection.getLayers();
			for (XSectionLayer layer : layers) {
				double elevation = layer.getElevation();
				long roundedElevation = Math.round(elevation * 100);
				mapRoundedElevationToXSectionLayer.put(roundedElevation, layer);
			}
		}
		// second pass to add layers to xsections that don't have the distinct
		// elevations
		ArrayList<XSection> xsections = new ArrayList<XSection>();
		for (XSection xsection : channel.getXsections()) {
			XSection interpolatedXSection = new XSection();
			xsections.add(interpolatedXSection);
			interpolatedXSection.setChannelId(channel.getId());
			interpolatedXSection.setDistance(xsection.getDistance());
			for (Long roundedElevation : mapRoundedElevationToXSectionLayer
					.keySet()) {
				double elevation = ((double) roundedElevation) / 100.0;
				XSectionLayer layer = getInterpolatedLayer(elevation, xsection);
				interpolatedXSection.addLayer(layer);
			}
			sortLayers(interpolatedXSection);
		}
		return xsections;
	}

	/**
	 * sorts the layers in increasing elevation order (in place)
	 * 
	 * @param interpolatedXSection
	 */
	private void sortLayers(XSection interpolatedXSection) {
		Collections.sort(interpolatedXSection.getLayers(),
				new Comparator<XSectionLayer>() {
					public int compare(XSectionLayer layer1,
							XSectionLayer layer2) {
						return (int) Math.signum(layer1.getElevation()
								- layer2.getElevation());
					}
				});
	}

	public XSectionLayer getInterpolatedLayer(double elevation,
			XSection xsection) {
		XSectionLayer layer = new XSectionLayer();
		layer.setElevation(elevation);
		// KEY ASSUMPTION: layers are arranged in order from min elevation to
		// max
		ArrayList<XSectionLayer> layers = xsection.getLayers();
		int i = 0;
		for (i = 0; i < layers.size(); i++) {
			if (elevation < layers.get(i).getElevation()) {
				break;
			}
		}
		if (i == 0) {
			layer.setArea(layers.get(0).getArea());
			layer.setTopWidth(layers.get(0).getTopWidth());
			layer.setWettedPerimeter(layers.get(0).getWettedPerimeter());
		} else if (i == layers.size()) {
			// TODO: use levee slope from model instead??
			layer.setArea(layers.get(i - 1).getArea());
			layer.setTopWidth(layers.get(i - 1).getTopWidth());
			layer.setWettedPerimeter(layers.get(i - 1).getWettedPerimeter());
		} else {
			XSectionLayer layer2 = layers.get(i);
			XSectionLayer layer1 = layers.get(i - 1);
			double elevationSlope = (elevation - layer1.getElevation())
					/ (layer2.getElevation() - layer1.getElevation());
			layer.setArea(layer1.getArea()
					+ (layer2.getArea() - layer1.getArea()) * elevationSlope);
			layer.setTopWidth(layer1.getTopWidth()
					+ (layer2.getTopWidth() - layer1.getTopWidth())
					* elevationSlope);
			layer.setWettedPerimeter(layer1.getWettedPerimeter()
					+ (layer2.getWettedPerimeter() - layer1
							.getWettedPerimeter()) * elevationSlope);
		}
		return layer;
	}
	
	public double getDistanceFromUpNode(Channel channel, XSectionProfile profile){
		//FIXME: implement this
		return 0;
	}


	/**
	 * Simplifies the xsection profile to get fewer points that still represent the shape.
	 * Calculates the simplified xsection based on epsilon (tolerance) and modifies the profile stored in the xsection 
	 * @param xs
	 */
	public void simplifyXSectionProfile(XSectionProfile xs, double epsilon){
		List<double[]> simplifiedPoints = RamerDouglasPeucker.properRDP(xs.getProfilePoints(), epsilon);
		xs.setProfilePoints(simplifiedPoints);
	}
}
