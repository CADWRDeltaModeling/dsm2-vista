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
package gov.ca.dsm2.input.model.diff;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.DSM2Model;

/**
 * A structure to represent to change in a model.
 * 
 * This can be generically represented as a tuple of (Model[Base], Model[New])
 * where the tuple is present for all elements that are present in both models
 * but are different in their intrinsic properties.
 * 
 * @author nsandhu
 * 
 */
public class DSM2ModelChange {
	private  DSM2Model modelBase;
	private  DSM2Model modelChanged;

	public DSM2ModelChange() {
		modelBase = new DSM2Model();
		modelChanged = new DSM2Model();
	}

	public void addChannelChange(Channel channelBase, Channel channelChanged) {
	}
}
