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
 * Represents a reservoir in the model {@link DSM2Model} and contained by
 * {@link Reservoirs}. Its identified by a name {@link #getName()} and defined
 * by an area {@link #getArea()}, a bottom elevation
 * {@link #getBottomElevation()} and its connections
 * {@link #getReservoirConnections()}
 * <p>
 * GIS information is represented by the general location {@link #getLatitude()}
 * and {@link #getLongitude()} and its outline by {@link #getLatLngPoints()}
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class Reservoir implements Serializable {
	private String name;
	private double area;
	private double bottomElevation;
	private  ArrayList<ReservoirConnection> reservoirConnections;
	private  ArrayList<double[]> latLngPoints;
	private double latitude;
	private double longitude;

	public Reservoir() {
		reservoirConnections = new ArrayList<ReservoirConnection>();
		latLngPoints = new ArrayList<double[]>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getBottomElevation() {
		return bottomElevation;
	}

	public void setBottomElevation(double bottomElevation) {
		this.bottomElevation = bottomElevation;
	}

	public void addReservoirConnection(ReservoirConnection connection) {
		reservoirConnections.add(connection);
	}

	public List<ReservoirConnection> getReservoirConnections() {
		return reservoirConnections;
	}

	public void setLatLngPoints(List<double[]> points) {
		latLngPoints.clear();
		latLngPoints.addAll(points);
	}

	public List<double[]> getLatLngPoints() {
		return latLngPoints;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
