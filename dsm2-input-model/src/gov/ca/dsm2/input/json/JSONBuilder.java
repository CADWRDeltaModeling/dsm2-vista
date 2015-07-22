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
package gov.ca.dsm2.input.json;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.Channels;
import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.model.Gates;
import gov.ca.dsm2.input.model.Node;
import gov.ca.dsm2.input.model.Nodes;
import gov.ca.dsm2.input.model.Reservoir;
import gov.ca.dsm2.input.model.ReservoirConnection;
import gov.ca.dsm2.input.model.Reservoirs;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionLayer;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class JSONBuilder {

	public JSONBuilder() {
	}

	public String toJSON(DSM2Model model) {
		JSONObject jmodel = new JSONObject();
		jmodel.put("channels", toJSON(model.getChannels()));
		jmodel.put("nodes", toJSON(model.getNodes()));
		jmodel.put("gates", toJSON(model.getGates()));
		jmodel.put("reservoirs", toJSON(model.getReservoirs()));
		return model.toString();
	}

	private JSONArray toJSON(Reservoirs reservoirs) {
		JSONArray jreservoirs = new JSONArray();
		List<Reservoir> list = reservoirs.getReservoirs();
		for (Reservoir reservoir : list) {
			jreservoirs.add(toJSON(reservoir));
		}
		return jreservoirs;
	}

	private JSONObject toJSON(Reservoir reservoir) {
		JSONObject jreservoir = new JSONObject();
		jreservoir.put("name", reservoir.getName());
		jreservoir.put("area", reservoir.getArea());
		jreservoir.put("bottomElevation", reservoir.getBottomElevation());
		jreservoir.put("connections", toJSON(reservoir
				.getReservoirConnections()));
		return jreservoir;
	}

	private JSONArray toJSON(List<ReservoirConnection> reservoirConnections) {
		JSONArray jconnections = new JSONArray();
		for (ReservoirConnection reservoirConnection : reservoirConnections) {
			jconnections.add(toJSON(reservoirConnection));
		}
		return jconnections;
	}

	private JSONObject toJSON(ReservoirConnection reservoirConnection) {
		JSONObject jconnection = new JSONObject();
		jconnection.put("nodeId", reservoirConnection.nodeId);
		jconnection.put("coeffIn", reservoirConnection.coefficientIn);
		jconnection.put("coeffOut", reservoirConnection.coefficientOut);
		return jconnection;
	}

	private JSONArray toJSON(Gates gates) {
		JSONArray jgates = new JSONArray();
		return jgates;
	}

	private JSONArray toJSON(Nodes nodes) {
		JSONArray jnodes = new JSONArray();
		List<Node> nodeList = nodes.getNodes();
		for (Node node : nodeList) {
			jnodes.add(toJSON(node));
		}
		return jnodes;
	}

	private JSONObject toJSON(Node node) {
		JSONObject jnode = new JSONObject();
		jnode.put("id", node.getId());
		jnode.put("lat", node.getLatitude());
		jnode.put("lng", node.getLongitude());
		return jnode;
	}

	private JSONArray toJSON(Channels channels) {
		JSONArray jchannels = new JSONArray();

		List<Channel> channelList = channels.getChannels();
		for (Channel channel : channelList) {
			JSONObject jchannel = toJSON(channel);
			jchannels.add(jchannel);
		}
		return jchannels;
	}

	private JSONObject toJSON(Channel channel) {
		JSONObject jchannel = new JSONObject();
		jchannel.put("id", channel.getId());
		jchannel.put("length", channel.getLength());
		jchannel.put("manning", channel.getMannings());
		jchannel.put("dispersion", channel.getDispersion());
		jchannel.put("upnode", channel.getUpNodeId());
		jchannel.put("downnode", channel.getDownNodeId());
		jchannel.put("xsections", toJSON(channel.getXsections()));
		return jchannel;
	}

	private JSONArray toJSON(ArrayList<XSection> xsections) {
		JSONArray jxsections = new JSONArray();
		for (XSection xsect : xsections) {
			jxsections.add(toJSON(xsect));
		}
		return jxsections;
	}

	private JSONObject toJSON(XSection xsect) {
		JSONObject jxsect = new JSONObject();
		jxsect.put("channel_id", xsect.getChannelId());
		jxsect.put("dist", xsect.getDistance());
		jxsect.put("layers", toJSONLayer(xsect.getLayers()));
		return jxsect;
	}

	private JSONArray toJSONLayer(ArrayList<XSectionLayer> layers) {
		JSONArray jlayers = new JSONArray();
		for (XSectionLayer layer : layers) {
			jlayers.add(toJSON(layer));
		}
		return jlayers;
	}

	private JSONObject toJSON(XSectionLayer layer) {
		JSONObject jlayer = new JSONObject();
		jlayer.put("elev", layer.getElevation());
		jlayer.put("area", layer.getArea());
		jlayer.put("width", layer.getTopWidth());
		jlayer.put("wet_perim", layer.getWettedPerimeter());
		return jlayer;
	}

}
