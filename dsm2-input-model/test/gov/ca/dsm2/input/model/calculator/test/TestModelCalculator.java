package gov.ca.dsm2.input.model.calculator.test;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionLayer;
import gov.ca.dsm2.input.model.calculator.ModelCalculator;

import java.util.ArrayList;

import junit.framework.TestCase;

public class TestModelCalculator extends TestCase {

	private Channel channel;

	protected void setUp() {
		channel = new Channel();
		channel.setId("425");
		channel.setDispersion(0.04);
		channel.setMannings(0.1);
		channel.setDownNodeId("5");
		channel.setUpNodeId("33");
		channel.setLength(2500);

		XSection upSection = new XSection();
		XSectionLayer layer0 = new XSectionLayer();
		layer0.setElevation(-5);
		layer0.setArea(0);
		layer0.setTopWidth(0);
		layer0.setWettedPerimeter(0);
		XSectionLayer layer1 = new XSectionLayer();
		layer1.setElevation(-2);
		layer1.setArea(10);
		layer1.setTopWidth(12);
		layer1.setWettedPerimeter(15);
		upSection.addLayer(layer0);
		upSection.addLayer(layer1);
		upSection.setChannelId(channel.getId());
		upSection.setDistance(0.25);
		channel.addXSection(upSection);

		XSection downSection = new XSection();
		layer0 = new XSectionLayer();
		layer0.setElevation(-7);
		layer0.setArea(0);
		layer0.setTopWidth(0);
		layer0.setWettedPerimeter(0);
		layer1 = new XSectionLayer();
		layer1.setElevation(-3);
		layer1.setArea(10);
		layer1.setTopWidth(12);
		layer1.setWettedPerimeter(15);
		downSection.addLayer(layer0);
		downSection.addLayer(layer1);
		downSection.setChannelId(channel.getId());
		downSection.setDistance(0.75);
		channel.addXSection(downSection);
	}

	public void testInterpolate() {
		ModelCalculator calculator = new ModelCalculator();
		ArrayList<XSection> xsections = channel.getXsections();
		ArrayList<XSection> interpolatedXSections = calculator
				.calculateZInterpolatedXSections(channel);
		assertNotNull(interpolatedXSections);
		assertEquals(xsections.size(), interpolatedXSections.size());
		XSection xSection = xsections.get(0);
		XSection ixSection = interpolatedXSections.get(0);
		assertEquals(xSection.getChannelId(), ixSection.getChannelId());
		assertEquals(xSection.getDistance(), ixSection.getDistance());
	}
}
