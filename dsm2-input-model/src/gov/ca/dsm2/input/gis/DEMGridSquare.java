package gov.ca.dsm2.input.gis;

import java.io.Serializable;
import java.util.List;

/**
 * A class representing a square piece of a digtial elevation model. This
 * particular one is specifically a 10 x 10 grid representing points which are
 * in the x,y direction similar to ASCII Grid format with a grid size of 10m
 * assumed. The elevation points themselves are returned in feet but are stored
 * internally in tenths of a foot
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class DEMGridSquare implements Serializable {
	public static final int NODATA = -9999;
	private int x;
	private int y;
	private int[] elevations; // represents a grid 10x10 with size of 10m

	protected DEMGridSquare() {

	}

	public DEMGridSquare(int x, int y, int[] elevations) {
		this.x = x;
		this.y = y;
		this.elevations = elevations;
	}

	/**
	 * Elevations are in tenths of a foot
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getElevationAt(double x, double y) {
		if (elevations == null) {
			return NODATA;
		}
		int xo = offset(x, this.x);
		int yo = offset(y, this.y);
		return elevations[xo + yo * 10];
	}

	public List<DataPoint> getElevationsAlongLine(double x1, double y1,
			double x2, double y2) {
		return null;
	}

	/**
	 * Number of 10 m increments this value is from the specified origin
	 */
	private int offset(double value, int origin) {
		return (int) Math.floor((value - origin) / 10);
	}

	public double getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int[] getElevations() {
		return elevations;
	}

	public void setElevations(int[] elevations) {
		this.elevations = elevations;
	}
}
