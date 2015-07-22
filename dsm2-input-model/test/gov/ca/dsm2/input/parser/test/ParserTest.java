package gov.ca.dsm2.input.parser.test;

import gov.ca.dsm2.input.model.BoundaryInput;
import gov.ca.dsm2.input.model.BoundaryInputs;
import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.ChannelOutput;
import gov.ca.dsm2.input.model.Channels;
import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.model.Gate;
import gov.ca.dsm2.input.model.Gates;
import gov.ca.dsm2.input.model.Nodes;
import gov.ca.dsm2.input.model.Outputs;
import gov.ca.dsm2.input.model.Reservoir;
import gov.ca.dsm2.input.model.ReservoirOutput;
import gov.ca.dsm2.input.model.Reservoirs;
import gov.ca.dsm2.input.model.Transfer;
import gov.ca.dsm2.input.model.Transfers;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionProfile;
import gov.ca.dsm2.input.parser.InputTable;
import gov.ca.dsm2.input.parser.Parser;
import gov.ca.dsm2.input.parser.Tables;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ParserTest extends TestCase {

	public void testSingleTable() throws IOException {
		Parser parser = new Parser();
		Tables model = parser.parseModel("test/single_table.inp");
		ArrayList<InputTable> tables = model.getTables();
		assertEquals(1, tables.size());
		InputTable envvarTable = tables.get(0);
		assertEquals("ENVVAR", envvarTable.getName());
		assertEquals("NAME", envvarTable.getHeaders().get(0));
		assertEquals("VALUE", envvarTable.getHeaders().get(1));
		assertEquals("BNDRYINPUT", envvarTable.getValue(0, "NAME"));
		assertEquals("15MIN", envvarTable.getValue(1, "VALUE"));
		assertEquals("STUDYDIR", envvarTable.getValue(2, "NAME"));
		assertEquals(".", envvarTable.getValue(2, "VALUE"));
	}

	public void testMultipleTables() throws IOException {
		Parser parser = new Parser();
		Tables tables = parser.parseModel("test/hydro_echo.inp");
		ArrayList<InputTable> tableArray = tables.getTables();
		assertEquals(26, tableArray.size());
		//
		Channels channels = tables.toChannels();
		assertNotNull(channels);
		assertNotNull(channels.getChannel("1"));
		// 
		Nodes nodes = tables.toNodes();
		assertNotNull(nodes);
		assertNotNull(nodes.getNode("17"));
		// parser.parseAndAddToModel(model, new
		// FileInputStream("test/node.inp"));
		parser.parseAndAddToModel(tables, new FileInputStream("test/gis.inp"));
		nodes = tables.toNodes();
		assertNotNull(nodes);
		assertNotNull(nodes.getNode("17"));
		//
		Gates gates = tables.toGates();
		assertNotNull(gates);
		Gate gate = gates.getGate("grant_line_barrier");
		assertNotNull(gate);
		assertEquals("grant_line_barrier", gate.getName());
		assertEquals("channel", gate.getFromObject());
		assertEquals("206", gate.getFromIdentifier());
		assertEquals("172", gate.getToNode());
		Gate gate2 = gates.getGate("7_mile@3_mile");
		assertNotNull(gate2);
		//
		Reservoirs reservoirs = tables.toReservoirs();
		assertNotNull(reservoirs);
		Reservoir reservoir = reservoirs.getReservoir("clifton_court");
		assertNotNull(reservoir);
		assertEquals("clifton_court", reservoir.getName());
		assertEquals(Double.parseDouble("-10.10"), reservoir
				.getBottomElevation());
		//
		BoundaryInputs boundaryInputs = tables.toBoundaryInputs();
		boundaryInputs.getFlowInputs();
		boundaryInputs.getSourceFlowInputs();
		List<BoundaryInput> stageInputs = boundaryInputs.getStageInputs();
		assertNotNull(stageInputs);
		assertEquals(1, stageInputs.size());
		assertEquals(7, boundaryInputs.getFlowInputs().size());
		BoundaryInput stageBoundary = stageInputs.get(0);
		assertEquals("mtz", stageBoundary.name);
		assertEquals("361", stageBoundary.nodeId);
		//
		Outputs outputs = tables.toOutputs();
		List<ChannelOutput> channelOutputs = outputs.getChannelOutputs();
		assertEquals(124, channelOutputs.size());
		List<ReservoirOutput> reservoirOutputs = outputs.getReservoirOutputs();
		assertEquals(2, reservoirOutputs.size());
		//
		Transfers transfers = tables.toTransfers();
		List<Transfer> transfersList = transfers.getTransfers();
		//
		//
		InputTable channelsTable = tables.getTableNamed("CHANNEL");
		String strTable = channelsTable.toStringRepresentation();
		System.out.println(strTable);
		//
		DSM2Model dsm2Model = tables.toDSM2Model();
		tables.fromDSM2Model(dsm2Model);
		channelsTable = tables.getTableNamed("CHANNEL");
		strTable = channelsTable.toStringRepresentation();
		//
		Channel channel26 = dsm2Model.getChannels().getChannel("26");
		XSection xSectionAt = channel26.getXSectionAt(0.139);
		XSectionProfile profile = xSectionAt.getProfile();
		assertNotNull(profile);
		assertEquals(0.139, profile.getDistance());
		//
		Parser parser2 = new Parser();
		ByteArrayInputStream bais = new ByteArrayInputStream(strTable
				.getBytes());
		bais.close();
		Tables tables2 = parser2.parseModel(bais);
		DSM2Model model2 = tables2.toDSM2Model();
		Channels channels2 = model2.getChannels();

	}
}
