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
 * In DSM2 a channel is an entity representing a stream with a in stream length
 * {@link #getLength()}, roughness coefficient (mannings n)
 * {@link #getMannings()}, dispersion factor {@link #getDispersion()} for water
 * quality constituents
 * 
 * It is defined by an id {@link #getId()} and its two end nodes, up node
 * {@link #getUpNodeId()} and down node {@link #getDownNodeId()}. It also has a
 * number of xsections {@link #getXsections()} and contains the gis
 * {@link #getLatLngPoints()} information to demarcate the internal points
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class Channel implements Serializable {
	private String id;
	private int length;
	private double mannings;
	private double dispersion;
	private String upNodeId;
	private String downNodeId;
	private ArrayList<XSection> xsections;
	private ArrayList<double[]> latLngPoints;

	public Channel() {
		xsections = new ArrayList<XSection>();
		latLngPoints = new ArrayList<double[]>();
	}

	public String getId() {
		return id;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public double getMannings() {
		return mannings;
	}

	public void setMannings(double mannings) {
		this.mannings = mannings;
	}

	public double getDispersion() {
		return dispersion;
	}

	public void setDispersion(double dispersion) {
		this.dispersion = dispersion;
	}

	public String getUpNodeId() {
		return upNodeId;
	}

	public void setUpNodeId(String upNodeId) {
		this.upNodeId = upNodeId;
	}

	public String getDownNodeId() {
		return downNodeId;
	}

	public void setDownNodeId(String downNodeId) {
		this.downNodeId = downNodeId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<XSection> getXsections() {
		return xsections;
	}

	public void addXSection(XSection xsection) {
		xsections.add(xsection);
	}

	public void setLatLngPoints(List<double[]> points) {
		latLngPoints.clear();
		latLngPoints.addAll(points);
	}

	public List<double[]> getLatLngPoints() {
		return latLngPoints;
	}

	public XSection getXSectionAt(double dist) {
		XSection x = null;
		for (XSection xs : getXsections()) {
			double distance = xs.getDistance();
			if (Math.abs(dist - distance) <= 0.0011) {
				x = xs;
				break;
			}
		}
		return x;
	}

}
