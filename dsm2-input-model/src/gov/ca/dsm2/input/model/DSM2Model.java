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

/**
 * Represents the model as channels {@link Channels}, nodes {@link Nodes},
 * reservoirs {@link Reservoirs}, gates {@link Gates}, outputs {@link Outputs}
 * and inputs {@link BoundaryInputs} as top level elements.
 * <p>
 * These top level elements that contain the associated sub information. For
 * example channels contain channel elements that in turn contain xsections and
 * so on
 * 
 * @author psandhu
 * 
 */
@SuppressWarnings("serial")
public class DSM2Model implements Serializable {
	private Channels channels;
	private Nodes nodes;
	private Reservoirs reservoirs;
	private Gates gates;
	private Outputs outputs;
	private BoundaryInputs inputs;
	private Transfers transfers;

	/**
	 * 
	 */
	public DSM2Model() {
		channels = new Channels();
		nodes = new Nodes();
		reservoirs = new Reservoirs();
		gates = new Gates();
	}

	public Reservoirs getReservoirs() {
		return reservoirs;
	}

	public void setReservoirs(Reservoirs reservoirs) {
		this.reservoirs = reservoirs;
	}

	public Gates getGates() {
		return gates;
	}

	public void setGates(Gates gates) {
		this.gates = gates;
	}

	public void setChannels(Channels channels) {
		this.channels = channels;
	}

	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	public Channels getChannels() {
		return channels;
	}

	public Nodes getNodes() {
		return nodes;
	}

	public void setInputs(BoundaryInputs inputs) {
		this.inputs = inputs;
	}

	public BoundaryInputs getInputs() {
		return inputs;
	}

	public void setOutputs(Outputs outputs) {
		this.outputs = outputs;
	}

	public Outputs getOutputs() {
		return outputs;
	}

	public Transfers getTransfers() {
		return transfers;
	}

	public void setTransfers(Transfers transfers) {
		this.transfers = transfers;
	}

	/**
	 * This is to assist in finding objects that are specified by type and
	 * identifier. E.g. in TRANSFER table the type can be one of node, channel,
	 * reservoir and the identifier the appropriate id for that type
	 * 
	 * @return null or the object if found
	 * @param type
	 * @param id
	 * @return
	 */
	public Object getObjectFromTypeAndIdentifier(String type, String id) {
		Object found = null;
		if ("node".equals(type)) {
			found = getNodes().getNode(id);
		} else if ("channel".equals(type)) {
			found = getChannels().getChannel(id);
		} else if ("reservoir".equals(type)) {
			found = getReservoirs().getReservoir(id);
		} else if ("gate".equals(type)) {
			found = getGates().getGate(id);
		} else if ("transfer".equals(type)) {
			found = getTransfers().getTransfer(id);
		}
		return found;
	}
}
