package gov.ca.dsm2.input.gis;

import java.io.Serializable;

/**
 * Represents a point in space. Used in storing a cross section profile as a set
 * of distance (x) and elevations (z)
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class DataPoint implements Serializable {
	public double x;
	public double y;
	public double z;

	public DataPoint() {

	}
}
