package gov.ca.dsm2.input.model.test;

import gov.ca.dsm2.input.model.XSectionLayer;
import gov.ca.dsm2.input.model.XSectionProfile;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class XSectionProfileTest extends TestCase {

	public void testRectangularXSection() {
		XSectionProfile profile = new XSectionProfile();
		profile.setChannelId(1);
		profile.setDistance(0.25);
		profile.setEndPoints(Arrays.asList(new double[] { 5, 5 }, new double[] {
				10, 5 }));
		profile.setId(275);
		profile.setProfilePoints(Arrays.asList(new double[] { 0, 0 },
				new double[] { 0, -10 }, new double[] { 15, -10 },
				new double[] { 15, 0 }));
		XSectionLayer l_0 = profile.calculateLayer(0);
		XSectionLayer l_5 = profile.calculateLayer(-5);
		XSectionLayer l_10 = profile.calculateLayer(-10);
		assertApproxEquals(0, l_10.getArea());
		assertApproxEquals(75, l_5.getArea());
		assertApproxEquals(150, l_0.getArea());
		//
		assertApproxEquals(15, l_10.getTopWidth());
		assertApproxEquals(15, l_5.getTopWidth());
		assertApproxEquals(15, l_0.getTopWidth());
		//
		assertApproxEquals(15, l_10.getWettedPerimeter());
		assertApproxEquals(25, l_5.getWettedPerimeter());
		assertApproxEquals(35, l_0.getWettedPerimeter());
		//
		double[] calculateElevations = profile.calculateElevations();
		assertApproxEquals(profile.getMinimumElevation(),
				calculateElevations[0]);
		assertApproxEquals(profile.getMaximumElevation(),
				calculateElevations[calculateElevations.length-1]);
		//
		List<XSectionLayer> layers = profile.calculateLayers();
		assertNotNull(layers);
	}

	public static void assertApproxEquals(double expected, double actual) {
		assertTrue(Math.abs(expected - actual) < 1e-6);
	}
}
