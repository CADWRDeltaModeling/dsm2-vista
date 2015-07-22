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

/**
 * A gate in the model {@link DSM2Model} contained in {@link Gates}. Each gate
 * is identified by its name {@link #getName()} and is defined by the object to
 * which its connected {@link #fromObject} and that objects identifier
 * {@link #fromIdentifier}. It further refines that connection to the
 * node/junction {@link #toNode} near which it exists
 * <p>
 * GIS information is represented by its location {@link #getLatitude()} and
 * {@link #getLongitude()}
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class Gate implements Serializable {
	private String name;
	private String fromObject;
	private String fromIdentifier;
	private String toNode;
	private double latitude;
	private double longitude;
	private ArrayList<GateDevice> gateDevices;
	private ArrayList<OperatingRule> gateOperations;

	public Gate() {
		gateDevices = new ArrayList<GateDevice>();
		gateOperations = new ArrayList<OperatingRule>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFromObject() {
		return fromObject;
	}

	public void setFromObject(String fromObject) {
		this.fromObject = fromObject;
	}

	public String getFromIdentifier() {
		return fromIdentifier;
	}

	public void setFromIdentifier(String fromIdentifier) {
		this.fromIdentifier = fromIdentifier;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void addGateDevice(GateDevice device) {
		gateDevices.add(device);
	}

	public void addGateOperation(OperatingRule gateOperation) {
		gateOperations.add(gateOperation);
	}

	public ArrayList<GateDevice> getGateDevices() {
		return gateDevices;
	}

	public ArrayList<OperatingRule> getGateOperations() {
		return gateOperations;
	}

}
