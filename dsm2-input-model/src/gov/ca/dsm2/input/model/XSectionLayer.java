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
 * A layer in the {@link XSection} which defines the elevation
 * {@link #getElevation()}, the top width {@link #getTopWidth()}, the wetted
 * perimeter {@link #getWettedPerimeter()}, the area {@link #getArea()}
 * 
 * @author nsandhu
 * 
 */
@SuppressWarnings("serial")
public class XSectionLayer implements Serializable {
	private double elevation;
	private double area;
	private double topWidth;
	private double wettedPerimeter;

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getTopWidth() {
		return topWidth;
	}

	public void setTopWidth(double topWidth) {
		this.topWidth = topWidth;
	}

	public double getWettedPerimeter() {
		return wettedPerimeter;
	}

	public void setWettedPerimeter(double wettedPerimeter) {
		this.wettedPerimeter = wettedPerimeter;
	}
}
