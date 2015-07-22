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
import java.util.HashMap;
import java.util.List;

/**
 * A container for all the {@link Gate} objects with ability to retrieve them
 * all {@link #getGates()}. Retrieval by name/id {@link #getGate(String)} is
 * also available.
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class Gates implements Serializable {
	private ArrayList<Gate> gates;
	private HashMap<String, Gate> gatesMap;
	private HashMap<String, ArrayList<Gate>> gatesNodeMap;

	public Gates() {
		gates = new ArrayList<Gate>();
		gatesMap = new HashMap<String, Gate>();
		gatesNodeMap = new HashMap<String, ArrayList<Gate>>();
	}

	public void addGate(Gate gate) {
		gates.add(gate);
		gatesMap.put(gate.getName(), gate);
		if (gatesNodeMap.containsKey(gate.getToNode())) {
			ArrayList<Gate> gates = gatesNodeMap.get(gate.getToNode());
			gates.add(gate);
		} else {
			ArrayList<Gate> gatesOnNode = new ArrayList<Gate>();
			gatesOnNode.add(gate);
			gatesNodeMap.put(gate.getToNode(), gatesOnNode);
		}

	}

	public void removeGate(String gateId) {
		Gate gate = gatesMap.get(gateId);
		if (gate == null) {
			return;
		}
		gatesMap.remove(gateId);
		gates.remove(gate);
		ArrayList<Gate> gatesOnNode = gatesNodeMap.get(gate.getToNode());
		if (gatesOnNode != null) {
			gatesOnNode.remove(gate);
			if (gatesOnNode.size() == 0) {
				gatesNodeMap.remove(gate.getToNode());
			}
		}
	}

	public void updateNodeId(String newValue, String previousValue) {
		ArrayList<Gate> gatesOnNode = gatesNodeMap.get(previousValue);
		if (gatesOnNode == null) {
			return;
		}
		for (Gate g : gatesOnNode) {
			g.setToNode(newValue);
		}
		gatesNodeMap.remove(previousValue);
		gatesNodeMap.put(newValue, gatesOnNode);
	}

	public List<Gate> getGates() {
		return gates;
	}

	public Gate getGate(String name) {
		return gatesMap.get(name);
	}

	public String buildGISTable() {
		StringBuilder buf = new StringBuilder();
		buf.append("GATE_GIS\n");
		buf.append("ID\tLAT_LNG\n");
		for (Gate gate : gates) {
			buf.append(gate.getName()).append("\t");
			buf.append("(").append(gate.getLatitude());
			buf.append(",").append(gate.getLongitude()).append(")");
			buf.append("\n");
		}
		buf.append("END\n");
		return buf.toString();
	}

}
