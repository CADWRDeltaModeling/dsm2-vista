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
package gov.ca.dsm2.input.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the xsection of a {@link Channel} in {@link XSectionLayer} layers.
 * Contains the distance {@link #getDistance()} at which this xsection is
 * present and the channel id {@link #getChannelId()} to which it belongs
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class XSection implements Serializable {
	private String channelId;
	private double distance;
	private ArrayList<XSectionLayer> layers;
	private XSectionProfile profile;

	public XSection() {
		layers = new ArrayList<XSectionLayer>();
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public ArrayList<XSectionLayer> getLayers() {
		return layers;
	}

	public void addLayer(XSectionLayer layer) {
		layers.add(layer);
	}
	
	public void setLayers(List<XSectionLayer> layers){
		this.layers.clear();
		this.layers.addAll(layers);
	}

	public void setProfile(XSectionProfile xsProfile) {
		profile = xsProfile;
	}

	public XSectionProfile getProfile() {
		return profile;
	}
}
