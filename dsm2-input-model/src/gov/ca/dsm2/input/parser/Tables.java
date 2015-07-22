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
package gov.ca.dsm2.input.parser;

import gov.ca.dsm2.input.model.BoundaryInput;
import gov.ca.dsm2.input.model.BoundaryInputs;
import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.ChannelOutput;
import gov.ca.dsm2.input.model.Channels;
import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.model.Gate;
import gov.ca.dsm2.input.model.GateDevice;
import gov.ca.dsm2.input.model.GatePipeDevice;
import gov.ca.dsm2.input.model.GateWeirDevice;
import gov.ca.dsm2.input.model.Gates;
import gov.ca.dsm2.input.model.Node;
import gov.ca.dsm2.input.model.Nodes;
import gov.ca.dsm2.input.model.OperatingRule;
import gov.ca.dsm2.input.model.Outputs;
import gov.ca.dsm2.input.model.Reservoir;
import gov.ca.dsm2.input.model.ReservoirConnection;
import gov.ca.dsm2.input.model.ReservoirOutput;
import gov.ca.dsm2.input.model.Reservoirs;
import gov.ca.dsm2.input.model.Transfer;
import gov.ca.dsm2.input.model.Transfers;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionLayer;
import gov.ca.dsm2.input.model.XSectionProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * The in memory representation of all the tables.
 * 
 * It is also converter from tables to the model {@link #toDSM2Model()} and
 * vice-versa {@link #fromDSM2Model(DSM2Model)}.
 * <p>
 * Each table {@link InputTable} in the input is represented here
 * {@link #getTables()} and can be retrieved using the name of the table
 * {@link #getTableNamed(String)}.
 * 
 * @see InputTable
 * @see DSM2Model
 * @author nsandhu
 * 
 */
public class Tables {
	private ArrayList<InputTable> tables;
	private HashMap<String, InputTable> tableMap;

	/**
	 * Creates an initial empty tables list
	 */
	public Tables() {
		tables = new ArrayList<InputTable>();
		tableMap = new HashMap<String, InputTable>();
	}

	/**
	 * adds a table to this list.
	 * <p>
	 * Note: Does not support adding the same name table twice to this list. If
	 * you need to do that use {@link #replaceTable(InputTable)} instead
	 * 
	 * @param table
	 */
	public void addTable(InputTable table) {
		tables.add(table);
		tableMap.put(table.getName(), table);
	}

	/**
	 * Replaces the similar named table. If it does not exist, it simply
	 * defaults to an add {@link #addTable(InputTable)} operation
	 * 
	 * @param table
	 */
	public void replaceTable(InputTable table) {
		InputTable existingTable = tableMap.get(table.getName());
		if (existingTable != null) {
			int existingIndexOf = tables.indexOf(existingTable);
			tables.add(existingIndexOf, table);
			tables.remove(existingTable);
			tableMap.remove(table.getName());
			tableMap.put(table.getName(), table);
		} else {
			addTable(table);
		}
	}

	/**
	 * Retrieves the reference to the list of all tables.
	 * 
	 * @return
	 */
	public ArrayList<InputTable> getTables() {
		return tables;
	}

	/**
	 * Retrieves the table named
	 * 
	 * @param name
	 * @return
	 */
	public InputTable getTableNamed(String name) {
		return tableMap.get(name);
	}

	/**
	 * The method to use if you want to convert from the raw table
	 * representation to a more object oriented structure.
	 * 
	 * If you want to be more fine grained in controlling how to convert to the
	 * model other methods are available {@link #toChannels()},
	 * {@link #toNodes()},{@link #toReservoirs()}, {@link #toGates()},
	 * {@link #toBoundaryInputs()}, {@link #toOutputs()}
	 * 
	 * @see DSM2Model
	 * @return
	 */
	public DSM2Model toDSM2Model() {
		DSM2Model model = new DSM2Model();
		model.setChannels(toChannels());
		model.setNodes(toNodes());
		model.setReservoirs(toReservoirs());
		model.setGates(toGates());
		model.setInputs(toBoundaryInputs());
		model.setOutputs(toOutputs());
		model.setTransfers(toTransfers());
		return model;
	}

	/**
	 * The key method to use to convert from model back to table representation
	 * of the input. This method uses the {@link #replaceTable(InputTable)}
	 * extensively to replace the appropriate named tables with information from
	 * the model. This means that tables that are not represented in the model
	 * are not affected
	 * <p>
	 * Note: currently even though inputs and outputs are parsed from these
	 * tables, they are not replaced from the model. This will be fixed soon
	 * 
	 * @param model
	 */
	public void fromDSM2Model(DSM2Model model) {
		List<InputTable> tables = fromChannels(model.getChannels());
		for (InputTable table : tables) {
			replaceTable(table);
		}
		List<InputTable> nodeTables = fromNodes(model.getNodes());
		for (InputTable table : nodeTables) {
			replaceTable(table);
		}
		List<InputTable> reservoirTables = fromReservoirs(model.getReservoirs());
		for (InputTable table : reservoirTables) {
			replaceTable(table);
		}
		List<InputTable> gateTables = fromGates(model.getGates());
		for (InputTable table : gateTables) {
			replaceTable(table);
		}

		// FIXME: fromInputs(model.getInputs());
		// FIXME: fromOutputs(model.getOutputs());
	}

	/**
	 * Finer grained conversion from model element. Use
	 * {@link #fromDSM2Model(DSM2Model)} instead unless you really need this
	 * level of access
	 * 
	 * @param channels
	 * @return
	 */
	public List<InputTable> fromChannels(Channels channels) {
		ArrayList<InputTable> list = new ArrayList<InputTable>();
		InputTable channelTable = new InputTable();
		channelTable.setName("CHANNEL");
		channelTable.setHeaders(Arrays.asList(new String[] { "CHAN_NO",
				"LENGTH", "MANNING", "DISPERSION", "UPNODE", "DOWNNODE" }));
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> xvalues = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> xpvalues = new ArrayList<ArrayList<String>>();
		for (Channel channel : channels.getChannels()) {
			ArrayList<String> rowValues = new ArrayList<String>();
			rowValues.add(channel.getId());
			rowValues.add(channel.getLength() + "");
			rowValues.add(channel.getMannings() + "");
			rowValues.add(channel.getDispersion() + "");
			rowValues.add(channel.getUpNodeId());
			rowValues.add(channel.getDownNodeId());
			values.add(rowValues);
			for (XSection xsection : channel.getXsections()) {
				for (XSectionLayer xsectLayer : xsection.getLayers()) {
					ArrayList<String> xrowValues = new ArrayList<String>();
					xrowValues.add(channel.getId());
					xrowValues.add(xsection.getDistance() + "");
					xrowValues.add(xsectLayer.getElevation() + "");
					xrowValues.add(xsectLayer.getArea() + "");
					xrowValues.add(xsectLayer.getTopWidth() + "");
					xrowValues.add(xsectLayer.getWettedPerimeter() + "");
					xvalues.add(xrowValues);
				}
				if (xsection.getProfile() != null) {
					XSectionProfile profile = xsection.getProfile();
					ArrayList<String> pvalues = new ArrayList<String>();
					pvalues.add(profile.getId() + "");
					pvalues.add(channel.getId() + "");
					pvalues.add(profile.getDistance() + "");
					pvalues.add(TableUtil.fromLatLngPoints(profile
							.getEndPoints()));
					pvalues.add(TableUtil.fromProfilePoints(profile
							.getProfilePoints()));
					xpvalues.add(pvalues);
				}
			}
		}
		channelTable.setValues(values);
		list.add(channelTable);
		InputTable xsectionTable = new InputTable();
		xsectionTable.setName("XSECT_LAYER");
		xsectionTable.setHeaders(Arrays.asList(new String[] { "CHAN_NO",
				"DIST", "ELEV", "AREA", "WIDTH", "WET_PERIM" }));
		list.add(xsectionTable);
		xsectionTable.setValues(xvalues);
		//
		InputTable gisTable = new InputTable();
		gisTable.setName("CHANNEL_GIS");
		gisTable.setHeaders(Arrays.asList(new String[] { "ID",
				"INTERIOR_LAT_LNG" }));
		values = new ArrayList<ArrayList<String>>();
		for (Channel channel : channels.getChannels()) {
			ArrayList<String> rowValues = new ArrayList<String>();
			rowValues.add(channel.getId());
			rowValues
					.add(TableUtil.fromLatLngPoints(channel.getLatLngPoints()));
			values.add(rowValues);
		}
		gisTable.setValues(values);
		list.add(gisTable);
		//
		InputTable xsectionProfileTable = new InputTable();
		xsectionProfileTable.setName("XSECTION_GIS");
		xsectionProfileTable.setHeaders(Arrays.asList(new String[] { "ID",
				"CHAN_NO", "DIST", "LATLNG_ENDPOINTS", "PROFILE_POINTS" }));
		xsectionProfileTable.setValues(xpvalues);
		list.add(xsectionProfileTable);
		//
		return list;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public Channels toChannels() {
		Channels channels = new Channels();
		InputTable channelTable = getTableNamed("CHANNEL");
		if (channelTable == null) {
			return channels;
		}
		int nchannels = channelTable.getValues().size();
		for (int i = 0; i < nchannels; i++) {
			try {
				Channel channel = new Channel();
				channel.setId(channelTable.getValue(i, "CHAN_NO"));
				channel.setLength((int) Double.parseDouble(channelTable
						.getValue(i, "LENGTH")));
				channel.setMannings(Double.parseDouble(channelTable.getValue(i,
						"MANNING")));
				channel.setDispersion(Double.parseDouble(channelTable.getValue(
						i, "DISPERSION")));
				channel.setUpNodeId(channelTable.getValue(i, "UPNODE"));
				channel.setDownNodeId(channelTable.getValue(i, "DOWNNODE"));
				channels.addChannel(channel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		InputTable xsectionTable = getTableNamed("XSECT_LAYER");
		if (xsectionTable != null) {
			int nxsects = xsectionTable.getValues().size();
			// FIXME: assumes all layers for a xsections are grouped together
			boolean newLayer = true;
			String currentChannelDist = "";
			XSection currentXSection = null;
			for (int i = 0; i < nxsects; i++) {
				try {
					if (!newLayer) {
						String channelDist = xsectionTable.getValue(i,
								"CHAN_NO")
								+ "_" + xsectionTable.getValue(i, "DIST");
						if (!channelDist.equals(currentChannelDist)) {
							newLayer = true;
						}
					}
					if (newLayer) {
						Channel channel = channels.getChannel(xsectionTable
								.getValue(i, "CHAN_NO"));
						XSection xsection = new XSection();
						xsection.setChannelId(channel.getId());
						xsection.setDistance(Double.parseDouble(xsectionTable
								.getValue(i, "DIST")));
						currentChannelDist = xsectionTable.getValue(i,
								"CHAN_NO")
								+ "_" + xsectionTable.getValue(i, "DIST");
						currentXSection = xsection;
						channel.addXSection(xsection);
						newLayer = false;
					}
					XSectionLayer layer = new XSectionLayer();
					layer.setElevation(Double.parseDouble(xsectionTable
							.getValue(i, "ELEV")));
					layer.setArea(Double.parseDouble(xsectionTable.getValue(i,
							"AREA")));
					layer.setTopWidth(Double.parseDouble(xsectionTable
							.getValue(i, "WIDTH")));
					layer.setWettedPerimeter(Double.parseDouble(xsectionTable
							.getValue(i, "WET_PERIM")));
					currentXSection.addLayer(layer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		InputTable gisTable = getTableNamed("CHANNEL_GIS");
		if (gisTable != null) {
			try {
				int nvals = gisTable.getValues().size();
				for (int i = 0; i < nvals; i++) {
					String id = gisTable.getValue(i, "ID");
					Channel channel = channels.getChannel(id);
					if (channel != null) {
						channel.setLatLngPoints(TableUtil
								.toLatLngPoints(gisTable.getValue(i,
										"INTERIOR_LAT_LNG")));
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		InputTable xsectGisTable = getTableNamed("XSECTION_GIS");
		if (xsectGisTable != null) {
			try {
				int nvals = xsectGisTable.getValues().size();
				for (int i = 0; i < nvals; i++) {
					String id = xsectGisTable.getValue(i, "ID");
					String channelId = xsectGisTable.getValue(i, "CHAN_NO");
					Channel channel = channels.getChannel(channelId);
					if (channel != null) {
						XSectionProfile xsProfile = new XSectionProfile();
						xsProfile.setId(Integer.parseInt(id));
						xsProfile.setChannelId(Integer.parseInt(channelId));
						double dist = Double.parseDouble(xsectGisTable
								.getValue(i, "DIST"));
						XSection xSectionAt = channel.getXSectionAt(dist);
						if (xSectionAt == null) {
							System.err
									.println("No XSection in channel: "
											+ channel.getId()
											+ " at distance: " + dist);
							continue;
						}
						xSectionAt.setProfile(xsProfile);
						xsProfile.setDistance(dist);
						xsProfile.setEndPoints(TableUtil
								.toLatLngPoints(xsectGisTable.getValue(i,
										"LATLNG_ENDPOINTS")));
						xsProfile.setProfilePoints(TableUtil
								.toProfilePoints(xsectGisTable.getValue(i,
										"PROFILE_POINTS")));
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return channels;
	}

	/**
	 * Finer grained conversion from model element. Use
	 * {@link #fromDSM2Model(DSM2Model)} instead unless you really need this
	 * level of access
	 * 
	 * @param nodes
	 * @return
	 */
	public List<InputTable> fromNodes(Nodes nodes) {
		ArrayList<InputTable> tables = new ArrayList<InputTable>();
		InputTable nodeTable = new InputTable();
		nodeTable.setName("NODE_GIS");
		nodeTable.setHeaders(Arrays.asList(new String[] { "ID", "LAT_LNG" }));
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		List<Node> nodeList = new ArrayList<Node>();
		nodeList.addAll(nodes.getNodes());
		Collections.sort(nodeList);
		for (Node node : nodeList) {
			try {
				ArrayList<String> rowValues = new ArrayList<String>();
				rowValues.add(node.getId());
				rowValues.add(TableUtil.fromLatLng(node.getLatitude(), node
						.getLongitude()));
				values.add(rowValues);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		nodeTable.setValues(values);
		tables.add(nodeTable);
		return tables;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public Nodes toNodes() {
		Nodes nodes = new Nodes();
		InputTable nodeTable = getTableNamed("NODE_GIS");
		if (nodeTable == null) {
			// derive from channels.inp as this table doesn't exist.
			nodeTable = new InputTable();
			nodeTable.setName("NODE_GIS");
			nodeTable.setHeaders(Arrays
					.asList(new String[] { "ID", "LAT_LNG" }));
			Channels channels = toChannels();
			HashMap<String, String> nodeIdMap = new HashMap<String, String>();
			ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
			for (Channel channel : channels.getChannels()) {
				String nodeId = channel.getUpNodeId();
				if (!nodeIdMap.containsKey(nodeId)) {
					nodeIdMap.put(nodeId, nodeId);
					ArrayList<String> rowValue = new ArrayList<String>();
					rowValue.add(nodeId);
					int idValue = Integer.parseInt(nodeId);
					double lat = 37.67 + 0.0001 * idValue;
					double lng = -121.45 + 0.0001 * idValue;
					rowValue.add("(" + lat + "," + lng + ")");
					values.add(rowValue);
				}
			}
			nodeTable.setValues(values);
		}
		int nnodes = nodeTable.getValues().size();
		for (int i = 0; i < nnodes; i++) {
			Node node = new Node();
			node.setId(nodeTable.getValue(i, "ID"));
			String latLng = nodeTable.getValue(i, "LAT_LNG");
			node.setLatitude(TableUtil.toLatitude(latLng));
			node.setLongitude(TableUtil.toLongitude(latLng));
			nodes.addNode(node);
		}
		InputTable channelTable = getTableNamed("CHANNEL");
		if (channelTable == null) {
			return nodes;
		}
		int nchannels = channelTable.getValues().size();
		for (int i = 0; i < nchannels; i++) {
			checkAndAddNode(nodes, channelTable.getValue(i, "UPNODE"));
			checkAndAddNode(nodes, channelTable.getValue(i, "DOWNNODE"));
		}

		return nodes;
	}

	private void checkAndAddNode(Nodes nodes, String nodeId) {
		Node node = nodes.getNode(nodeId);
		if (node == null) {
			node = new Node();
			node.setId(nodeId);
			node.setLatitude(38.5);
			node.setLongitude(-121.5);
			nodes.addNode(node);
		}
	}

	/**
	 * Finer grained conversion from model element. Use
	 * {@link #fromDSM2Model(DSM2Model)} instead unless you really need this
	 * level of access
	 * 
	 * @param gates
	 * @return
	 */
	public List<InputTable> fromGates(Gates gates) {
		ArrayList<InputTable> list = new ArrayList<InputTable>();
		InputTable gateTable = new InputTable();
		gateTable.setName("GATE");
		gateTable.setHeaders(Arrays.asList(new String[] { "NAME", "FROM_OBJ",
				"FROM_IDENTIFIER", "TO_NODE" }));

		InputTable gateWeirDeviceTable = new InputTable();
		gateWeirDeviceTable.setName("GATE_WEIR_DEVICE");
		gateWeirDeviceTable.setHeaders(Arrays.asList(new String[] {
				"GATE_NAME", "DEVICE", "NDUPLICATE", "WIDTH", "ELEV", "HEIGHT",
				"CF_FROM_NODE", "CF_TO_NODE", "DEFAULT_OP" }));
		
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> gateWeirValues = new ArrayList<ArrayList<String>>();
		for (Gate gate : gates.getGates()) {
			try {
				ArrayList<String> rowValues = new ArrayList<String>();
				rowValues.add(gate.getName());
				rowValues.add(gate.getFromObject());
				rowValues.add(gate.getFromIdentifier());
				rowValues.add(gate.getToNode());
				values.add(rowValues);
				ArrayList<GateDevice> gateDevices = gate.getGateDevices();
				for(GateDevice device: gateDevices){
					if (device instanceof GateWeirDevice){
						ArrayList<String> deviceRowValues = new ArrayList<String>();
						deviceRowValues.add(gate.getName());
						deviceRowValues.add(device.device);
						deviceRowValues.add(device.numberOfDuplicates+"");
						deviceRowValues.add(((GateWeirDevice) device).width+"");
						deviceRowValues.add(device.elevation+"");
						deviceRowValues.add(((GateWeirDevice) device).height+"");
						deviceRowValues.add(device.coefficientFromNode+"");
						deviceRowValues.add(device.coefficientToNode+"");
						deviceRowValues.add(device.defaultOperation);
						gateWeirValues.add(deviceRowValues);
					}
					//FIXME: for pipe devices implement here
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gateTable.setValues(values);
		gateWeirDeviceTable.setValues(gateWeirValues);
		list.add(gateTable);
		list.add(gateWeirDeviceTable);
		//
		//
		InputTable gisTable = new InputTable();
		gisTable.setName("GATE_GIS");
		gisTable.setHeaders(Arrays.asList(new String[] { "ID", "LAT_LNG" }));
		values = new ArrayList<ArrayList<String>>();
		for (Gate gate : gates.getGates()) {
			try {
				ArrayList<String> rowValues = new ArrayList<String>();
				rowValues.add(gate.getName());
				rowValues.add(TableUtil.fromLatLng(gate.getLatitude(), gate
						.getLongitude()));
				values.add(rowValues);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gisTable.setValues(values);
		list.add(gisTable);
		return list;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public Gates toGates() {
		Gates gates = new Gates();
		InputTable gateTable = getTableNamed("GATE");
		if (gateTable == null) {
			return gates;
		}
		int ngates = gateTable.getValues().size();
		for (int i = 0; i < ngates; i++) {
			try {
				Gate gate = new Gate();
				gate.setName(gateTable.getValue(i, "NAME"));
				gate.setFromObject(gateTable.getValue(i, "FROM_OBJ"));
				gate
						.setFromIdentifier(gateTable.getValue(i,
								"FROM_IDENTIFIER"));
				gate.setToNode(gateTable.getValue(i, "TO_NODE"));
				gates.addGate(gate);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		InputTable gateWeirTable = getTableNamed("GATE_WEIR_DEVICE");
		if (gateWeirTable != null) {
			int ndevices = gateWeirTable.getValues().size();
			for (int i = 0; i < ndevices; i++) {
				GateWeirDevice device = new GateWeirDevice();
				device.gateName = gateWeirTable.getValue(i, "GATE_NAME");
				device.device = gateWeirTable.getValue(i, "DEVICE");
				device.numberOfDuplicates = Integer.parseInt(gateWeirTable
						.getValue(i, "NDUPLICATE"));
				device.width = Double.parseDouble(gateWeirTable.getValue(i,
						"WIDTH"));
				device.elevation = Double.parseDouble(gateWeirTable.getValue(i,
						"ELEV"));
				device.height = Double.parseDouble(gateWeirTable.getValue(i,
						"HEIGHT"));
				device.coefficientFromNode = Double.parseDouble(gateWeirTable
						.getValue(i, "CF_FROM_NODE"));
				device.coefficientToNode = Double.parseDouble(gateWeirTable
						.getValue(i, "CF_TO_NODE"));
				device.defaultOperation = gateWeirTable.getValue(i,
						"DEFAULT_OP");
				Gate gate = gates.getGate(device.gateName);
				if (gate != null) {
					gate.addGateDevice(device);
				}
			}
		}
		InputTable gatePipeTable = getTableNamed("GATE_PIPE_DEVICE");
		if (gatePipeTable != null) {
			int ndevices = gatePipeTable.getValues().size();
			for (int i = 0; i < ndevices; i++) {
				GatePipeDevice device = new GatePipeDevice();
				device.gateName = gatePipeTable.getValue(i, "GATE_NAME");
				device.device = gatePipeTable.getValue(i, "DEVICE");
				device.numberOfDuplicates = Integer.parseInt(gatePipeTable
						.getValue(i, "NDUPLICATE"));
				device.radius = Double.parseDouble(gatePipeTable.getValue(i,
						"RADIUS"));
				device.elevation = Double.parseDouble(gatePipeTable.getValue(i,
						"ELEV"));
				device.coefficientFromNode = Double.parseDouble(gatePipeTable
						.getValue(i, "CF_FROM_NODE"));
				device.coefficientToNode = Double.parseDouble(gatePipeTable
						.getValue(i, "CF_TO_NODE"));
				device.defaultOperation = gatePipeTable.getValue(i,
						"DEFAULT_OP");
				Gate gate = gates.getGate(device.gateName);
				if (gate != null) {
					gate.addGateDevice(device);
				}
			}

		}
		InputTable operatingRuleTable = getTableNamed("OPERATING_RULE");
		if (operatingRuleTable != null) {
			int nrules = operatingRuleTable.getValues().size();
			for (int i = 0; i < nrules; i++) {
				OperatingRule opRule = new OperatingRule();
				opRule.name = operatingRuleTable.getValue(i, "NAME");
				opRule.action = operatingRuleTable.getValue(i, "ACTION");
				opRule.trigger = operatingRuleTable.getValue(i, "TRIGGER");
				Gate gate = gates.getGate(opRule.getGateName());
				if (gate != null) {
					gate.addGateOperation(opRule);
				}
			}
		}
		InputTable gisTable = getTableNamed("GATE_GIS");
		if (gisTable != null) {
			int nnodes = gisTable.getValues().size();
			for (int i = 0; i < nnodes; i++) {
				try {
					String id = gisTable.getValue(i, "ID");
					Gate gate = gates.getGate(id);
					if (gate != null) {
						String latLng = gisTable.getValue(i, "LAT_LNG");
						gate.setLatitude(TableUtil.toLatitude(latLng));
						gate.setLongitude(TableUtil.toLongitude(latLng));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return gates;
	}

	/**
	 * Finer grained conversion from model element. Use
	 * {@link #fromDSM2Model(DSM2Model)} instead unless you really need this
	 * level of access
	 * 
	 * @param reservoirs
	 * @return
	 */
	public List<InputTable> fromReservoirs(Reservoirs reservoirs) {
		ArrayList<InputTable> list = new ArrayList<InputTable>();
		InputTable reservoirTable = new InputTable();
		reservoirTable.setName("RESERVOIR");
		reservoirTable.setHeaders(Arrays.asList(new String[] { "NAME", "AREA",
				"BOT_ELEV" }));
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		for (Reservoir reservoir : reservoirs.getReservoirs()) {
			try {
				ArrayList<String> rowValues = new ArrayList<String>();
				rowValues.add(reservoir.getName());
				rowValues.add(reservoir.getArea() + "");
				rowValues.add(reservoir.getBottomElevation() + "");
				values.add(rowValues);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		reservoirTable.setValues(values);
		list.add(reservoirTable);
		//
		InputTable reservoirConnectionTable = new InputTable();
		reservoirConnectionTable.setName("RESERVOIR_CONNECTION");
		reservoirConnectionTable.setHeaders(Arrays.asList(new String[] {
				"RES_NAME", "NODE", "COEF_IN", "COEF_OUT" }));
		values = new ArrayList<ArrayList<String>>();
		for (Reservoir reservoir : reservoirs.getReservoirs()) {
			for (ReservoirConnection reservoirConnection : reservoir
					.getReservoirConnections()) {
				try {
					ArrayList<String> rowValues = new ArrayList<String>();
					rowValues.add(reservoirConnection.reservoirName);
					rowValues.add(reservoirConnection.nodeId);
					rowValues.add(reservoirConnection.coefficientIn + "");
					rowValues.add(reservoirConnection.coefficientOut + "");
					values.add(rowValues);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		reservoirConnectionTable.setValues(values);
		list.add(reservoirConnectionTable);
		//
		InputTable gisTable = new InputTable();
		gisTable.setName("RESERVOIR_GIS");
		gisTable.setHeaders(Arrays.asList(new String[] { "ID", "LAT_LNG",
				"INTERIOR_LAT_LNG" }));
		values = new ArrayList<ArrayList<String>>();
		for (Reservoir reservoir : reservoirs.getReservoirs()) {
			try {
				ArrayList<String> rowValues = new ArrayList<String>();
				rowValues.add(reservoir.getName());
				rowValues.add(TableUtil.fromLatLng(reservoir.getLatitude(),
						reservoir.getLongitude()));
				rowValues.add(TableUtil.fromLatLngPoints(reservoir
						.getLatLngPoints()));
				values.add(rowValues);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gisTable.setValues(values);
		list.add(gisTable);
		return list;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public Reservoirs toReservoirs() {
		Reservoirs reservoirs = new Reservoirs();
		InputTable reservoirTable = getTableNamed("RESERVOIR");
		if (reservoirTable == null) {
			return reservoirs;
		}
		int nreservoirs = reservoirTable.getValues().size();
		for (int i = 0; i < nreservoirs; i++) {
			try {
				Reservoir reservoir = new Reservoir();
				reservoir.setName(reservoirTable.getValue(i, "NAME"));
				reservoir.setArea(Double.parseDouble(reservoirTable.getValue(i,
						"AREA")));
				reservoir.setBottomElevation(Double.parseDouble(reservoirTable
						.getValue(i, "BOT_ELEV")));
				reservoirs.addReservoir(reservoir);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		InputTable reservoirConnectionTable = getTableNamed("RESERVOIR_CONNECTION");
		if (reservoirConnectionTable == null) {
			return reservoirs;
		}
		int nconnections = reservoirConnectionTable.getValues().size();
		for (int i = 0; i < nconnections; i++) {
			try {
				String reservoirName = reservoirConnectionTable.getValue(i,
						"RES_NAME");
				ReservoirConnection connection = new ReservoirConnection();
				connection.reservoirName = reservoirName;
				connection.nodeId = reservoirConnectionTable
						.getValue(i, "NODE");
				connection.coefficientIn = Double
						.parseDouble(reservoirConnectionTable.getValue(i,
								"COEF_IN"));
				connection.coefficientOut = Double
						.parseDouble(reservoirConnectionTable.getValue(i,
								"COEF_OUT"));
				Reservoir reservoir = reservoirs.getReservoir(reservoirName);
				if (reservoir == null) {
					System.err
							.println("Connection for unknown reservoir named: "
									+ reservoirName);
				}
				reservoir.addReservoirConnection(connection);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		InputTable gisTable = getTableNamed("RESERVOIR_GIS");
		if (gisTable != null) {
			int nvals = gisTable.getValues().size();
			for (int i = 0; i < nvals; i++) {
				try {
					String id = gisTable.getValue(i, "ID");
					Reservoir reservoir = reservoirs.getReservoir(id);
					if (reservoir != null) {
						String latLng = gisTable.getValue(i, "LAT_LNG");
						reservoir.setLatitude(TableUtil.toLatitude(latLng));
						reservoir.setLongitude(TableUtil.toLongitude(latLng));
						reservoir.setLatLngPoints(TableUtil
								.toLatLngPoints(gisTable.getValue(i,
										"INTERIOR_LAT_LNG")));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}
		return reservoirs;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public BoundaryInputs toBoundaryInputs() {
		BoundaryInputs binputs = new BoundaryInputs();
		InputTable stageTable = getTableNamed("BOUNDARY_STAGE");
		if (stageTable != null) {
			int nstages = stageTable.getValues().size();
			for (int i = 0; i < nstages; i++) {
				try {
					BoundaryInput input = new BoundaryInput();
					input.name = stageTable.getValue(i, "NAME");
					input.nodeId = stageTable.getValue(i, "NODE");
					input.fillIn = stageTable.getValue(i, "FILLIN");
					input.file = stageTable.getValue(i, "FILE");
					input.path = stageTable.getValue(i, "PATH");
					input.type = "stage";
					binputs.addStage(input);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		InputTable flowTable = getTableNamed("BOUNDARY_FLOW");
		if (flowTable != null) {
			int nstages = flowTable.getValues().size();
			for (int i = 0; i < nstages; i++) {
				try {
					BoundaryInput input = new BoundaryInput();
					input.name = flowTable.getValue(i, "NAME");
					input.nodeId = flowTable.getValue(i, "NODE");
					input.sign = Integer
							.parseInt(flowTable.getValue(i, "SIGN"));
					input.fillIn = flowTable.getValue(i, "FILLIN");
					input.file = flowTable.getValue(i, "FILE");
					input.path = flowTable.getValue(i, "PATH");
					input.type = "flow";
					binputs.addFlow(input);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		InputTable sourceFlowTable = getTableNamed("SOURCE_FLOW");
		if (sourceFlowTable != null) {
			int nstages = sourceFlowTable.getValues().size();
			for (int i = 0; i < nstages; i++) {
				try {
					BoundaryInput input = new BoundaryInput();
					input.name = sourceFlowTable.getValue(i, "NAME");
					input.nodeId = sourceFlowTable.getValue(i, "NODE");
					input.sign = Integer.parseInt(sourceFlowTable.getValue(i,
							"SIGN"));
					input.fillIn = sourceFlowTable.getValue(i, "FILLIN");
					input.file = sourceFlowTable.getValue(i, "FILE");
					input.path = sourceFlowTable.getValue(i, "PATH");
					input.type = "sourcesink";
					binputs.addSourceFlow(input);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return binputs;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public Outputs toOutputs() {
		Outputs outputs = new Outputs();
		InputTable outputTable = getTableNamed("OUTPUT_CHANNEL");
		if (outputTable == null) {
			return outputs;
		}
		int noutputs = outputTable.getValues().size();
		for (int i = 0; i < noutputs; i++) {
			try {
				ChannelOutput output = new ChannelOutput();
				output.name = outputTable.getValue(i, "NAME");
				output.channelId = outputTable.getValue(i, "CHAN_NO");
				output.distance = outputTable.getValue(i, "DISTANCE");
				output.variable = outputTable.getValue(i, "VARIABLE");
				output.interval = outputTable.getValue(i, "INTERVAL");
				output.period_op = outputTable.getValue(i, "PERIOD_OP");
				output.file = outputTable.getValue(i, "FILE");
				outputs.addOutput(output);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		outputTable = getTableNamed("OUTPUT_RESERVOIR");
		if (outputTable == null) {
			return outputs;
		}
		noutputs = outputTable.getValues().size();
		for (int i = 0; i < noutputs; i++) {
			try {
				ReservoirOutput output = new ReservoirOutput();
				output.name = outputTable.getValue(i, "NAME");
				output.reservoirId = outputTable.getValue(i, "RES_NAME");
				output.nodeId = outputTable.getValue(i, "NODE");
				output.variable = outputTable.getValue(i, "VARIABLE");
				output.interval = outputTable.getValue(i, "INTERVAL");
				output.period_op = outputTable.getValue(i, "PERIOD_OP");
				output.file = outputTable.getValue(i, "FILE");
				outputs.addReservoirOutput(output);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return outputs;
	}

	/**
	 * Finer grained conversion to model elements. Use {@link #toDSM2Model()}
	 * instead unless you really need this level of access
	 * 
	 * @return
	 */
	public Transfers toTransfers() {
		Transfers transfers = new Transfers();
		InputTable transferTable = getTableNamed("TRANSFER");
		if (transferTable != null) {
			int ntransfers = transferTable.getValues().size();
			for (int i = 0; i < ntransfers; i++) {
				Transfer transfer = new Transfer();
				transfer.name = transferTable.getValue(i, "NAME");
				transfer.fromObject = transferTable.getValue(i, "FROM_OBJ");
				transfer.fromIdentifier = transferTable.getValue(i,
						"FROM_IDENTIFIER");
				transfer.toObject = transferTable.getValue(i, "TO_OBJ");
				transfer.toIdentifier = transferTable.getValue(i,
						"TO_IDENTIFIER");
				transfers.addTransfer(transfer);
			}
		}
		return transfers;
	}

	public HashMap<String, String> toExtIntMap(InputTable table) {
		HashMap<String, String> map = new HashMap<String, String>();
		int count = table.getValues().size();
		for (int i = 0; i < count; i++) {
			String intVal = table.getValue(i, "INT");
			String extVal = table.getValue(i, "EXT");
			map.put(intVal, extVal);
		}
		return map;
	}
}
