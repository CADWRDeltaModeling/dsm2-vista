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
 * A container for {@link Reservoir} objects {@link #getReservoirs()}. There is
 * also ability to retrieve them from their name {@link #getReservoir(String)}.
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class Reservoirs implements Serializable {
	private ArrayList<Reservoir> reservoirs;
	private HashMap<String, Reservoir> reservoirIdMap;
	private HashMap<String, ArrayList<Reservoir>> reservoirNodeMap;

	public Reservoirs() {
		reservoirs = new ArrayList<Reservoir>();
		reservoirIdMap = new HashMap<String, Reservoir>();
		reservoirNodeMap = new HashMap<String, ArrayList<Reservoir>>();
	}

	public void addReservoir(Reservoir reservoir) {
		reservoirs.add(reservoir);
		reservoirIdMap.put(reservoir.getName(), reservoir);
		List<ReservoirConnection> reservoirConnections = reservoir
				.getReservoirConnections();
		for (ReservoirConnection c : reservoirConnections) {
			String nodeId = c.nodeId;
			if (reservoirNodeMap.containsKey(nodeId)) {
				ArrayList<Reservoir> reservoirs = reservoirNodeMap.get(nodeId);
				reservoirs.add(reservoir);
			} else {
				ArrayList<Reservoir> reservoirs = new ArrayList<Reservoir>();
				reservoirs.add(reservoir);
				reservoirNodeMap.put(nodeId, reservoirs);
			}
		}
	}

	public Reservoir getReservoir(String reservoirId) {
		return reservoirIdMap.get(reservoirId);
	}

	public void removeReservoir(Reservoir reservoir) {
		reservoirs.remove(reservoir);
		reservoirIdMap.remove(reservoir.getName());
		List<ReservoirConnection> reservoirConnections = reservoir
				.getReservoirConnections();
		for (ReservoirConnection c : reservoirConnections) {
			String nodeId = c.nodeId;
			ArrayList<Reservoir> reservoirs = reservoirNodeMap.get(nodeId);
			if (reservoirs != null) {
				reservoirs.remove(reservoir);
				if (reservoirs.size() == 0) {
					reservoirNodeMap.remove(nodeId);
				}
			}
		}
	}

	public void updateNodeId(String newValue, String previousValue) {
		ArrayList<Reservoir> reservoirs = reservoirNodeMap.get(previousValue);
		if (reservoirs == null) {
			return;
		}
		for (Reservoir reservoir : reservoirs) {
			for (ReservoirConnection c : reservoir.getReservoirConnections()) {
				if (c.nodeId.equals(previousValue)) {
					c.nodeId = newValue;
				}
			}
		}
		reservoirNodeMap.remove(previousValue);
		reservoirNodeMap.put(newValue, reservoirs);
	}

	public List<Reservoir> getReservoirs() {
		return reservoirs;
	}

}
