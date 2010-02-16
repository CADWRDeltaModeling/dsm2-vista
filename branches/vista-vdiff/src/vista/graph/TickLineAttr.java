/*
    Copyright (C) 1996, 1997, 1998 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0beta
	by Nicky Sandhu
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA 95814
    (916)-653-7552
    nsandhu@water.ca.gov

    Send bug reports to nsandhu@water.ca.gov

    This program is licensed to you under the terms of the GNU General
    Public License, version 2, as published by the Free Software
    Foundation.

    You should have received a copy of the GNU General Public License
    along with this program; if not, contact Dr. Francis Chung, below,
    or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
    02139, USA.

    THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
    OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
    USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
    DAMAGE.

    For more information about VISTA, contact:

    Dr. Francis Chung
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA  95814
    916-653-5601
    chung@water.ca.gov

    or see our home page: http://wwwdelmod.water.ca.gov/

    Send bug reports to nsandhu@water.ca.gov or call (916)-653-7552

 */
package vista.graph;

import java.awt.Color;

/**
 * Attributes of TickLine
 * 
 * @see TickLine
 * @author Nicky Sandhu (DWR).
 * @version $Id: TickLineAttr.java,v 1.1 2003/10/02 20:49:11 redwood Exp $
 */
public class TickLineAttr extends GEAttr {
	/**
	 * Tick Line position in plot towards bottom
	 */
	public final static int BOTTOM = 1;
	/**
	 * Tick Line position in plot towards top
	 */
	public final static int TOP = BOTTOM + 1;
	/**
	 * Tick Line position in plot towards left
	 */
	public final static int LEFT = TOP + 1;
	/**
	 * Tick Line position in plot towards right
	 */
	public final static int RIGHT = LEFT + 1;
	/**
	 * Tick marks within the drawing area
	 */
	public final static int INSIDE = 10;
	/**
	 * Tick marks outside the drawing area
	 */
	public final static int OUTSIDE = INSIDE + 1;
	/**
	 * Tick marks inside & outside the drawing area
	 */
	public final static int BOTH = OUTSIDE + 1;
	/**
	 * Position of tick line in plot
	 */
	public int _position = BOTTOM;
	/**
	 * Location of tick marks within or outside bounds
	 */
	public int _tickLocation = OUTSIDE;
	/**
	 * length of major tick mark in percentage of the drawing area.
	 */
	public double _percentMajorTickLength = 0.8;
	/**
	 * length of major 2 tick mark in percentage of the drawing area.
	 */
	public double _percentMajor2TickLength = 0.0;
	/**
	 * length of minor tick mark in percentage of the drawing area.
	 */
	public double _percentMinorTickLength = 0.3;
	/**
	 * plot major ticks
	 */
	public boolean _plotMajorTicks = true;
	/**
	 * plot minor ticks
	 */
	public boolean _plotMinorTicks = true;
	/**
	 * plot tick line
	 */
	public boolean _plotLine = true;
	/**
	 * Thickness of lines
	 */
	public int _thickness = 1;
	/**
   *
   */
	public Color _color = Color.black;

	/**
	 * copies the fields into the given GEAttr object. Also copies in the
	 * TickLineAttr if the object is of that type.
	 */
	public void copyInto(GEAttr ga) {
		super.copyInto(ga);
		if (ga instanceof TickLineAttr) {
			TickLineAttr lea = (TickLineAttr) ga;
			lea._position = this._position;
			lea._tickLocation = this._tickLocation;
			lea._percentMajorTickLength = this._percentMajorTickLength;
			lea._percentMinorTickLength = this._percentMinorTickLength;
			lea._plotMajorTicks = this._plotMajorTicks;
			lea._plotMinorTicks = this._plotMinorTicks;
			lea._color = this._color;
		}
	}

}
