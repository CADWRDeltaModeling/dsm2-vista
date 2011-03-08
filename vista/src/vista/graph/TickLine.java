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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Properties;

/**
 * This implements a line with major ticks and minor ticks. The line drawn is
 * one one of the edges of the bounds depending upon the elements position. The
 * tick marks may lie outside the bounds and so clipping has to be off if some
 * of the tick marks need to be visible.
 * 
 * @author Nicky Sandhu
 * @version $Id: TickLine.java,v 1.1 2003/10/02 20:49:10 redwood Exp $
 */
public class TickLine extends GraphicElement {
	/**
	 * debuggin'
	 */
	public static final boolean DEBUG = false;

	/**
	 * constructor
	 */
	public TickLine(TickData[] ticks) {
		this(new TickLineAttr(), ticks);
	}

	/**
	 * constructor
	 */
	public TickLine(TickLineAttr attributes, TickData[] ticks) {
		super(attributes);
		if (ticks != null) {
			_ticks = new TickData[ticks.length];
			System.arraycopy(ticks, 0, _ticks, 0, _ticks.length);
		}
		setTickData(_ticks);
	}

	/**
	 * implements the draw method
	 */
	public void Draw() {
		/*
		Rectangle bounds = getBounds();
		getGraphics().setColor(Color.GREEN);
		getGraphics().drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		*/
		Rectangle area = getInsetedBounds();
		setTickDimensions(area);
		Graphics gc = getGraphics();
		TickLineAttr attr = (TickLineAttr) getAttributes();
		if (DEBUG)
			System.out.println(area);

		int x = 0;
		int y = 0;
		int incX = 0;
		int incY = 0;
		int decX = 0;
		int decY = 0;

		switch (attr._position) {

		case TickLineAttr.BOTTOM:
			x = area.x;
			y = area.y;
			incX = 0;
			decX = 0;
			if (attr._tickLocation == TickLineAttr.INSIDE) {
				incY = (int) (area.height);
				decY = 0;
			} else if (attr._tickLocation == TickLineAttr.OUTSIDE) {
				incY = 0;
				decY = (int) (-area.height);
			} else if (attr._tickLocation == TickLineAttr.BOTH) {
				incY = (int) (area.height);
				decY = (int) (-area.height);
			}
			break;

		case TickLineAttr.TOP:
			x = area.x;
			y = area.y + area.height;
			incX = 0;
			decX = 0;
			if (attr._tickLocation == TickLineAttr.INSIDE) {
				incY = (int) (-area.height);
				decY = 0;
			} else if (attr._tickLocation == TickLineAttr.OUTSIDE) {
				incY = 0;
				decY = (int) (area.height);
			} else if (attr._tickLocation == TickLineAttr.BOTH) {
				incY = (int) (-area.height);
				decY = (int) (area.height);
			}
			break;

		case TickLineAttr.LEFT:
			x = area.x + area.width;
			y = area.y + area.height;
			if (attr._tickLocation == TickLineAttr.INSIDE) {
				incX = (int) (-area.width);
				decX = 0;
			} else if (attr._tickLocation == TickLineAttr.OUTSIDE) {
				decX = (int) (area.width);
				incX = 0;
			} else if (attr._tickLocation == TickLineAttr.BOTH) {
				incX = (int) (-area.width);
				decX = (int) (area.width);
			}
			incY = 0;
			decY = 0;
			break;

		case TickLineAttr.RIGHT:
			x = area.x;
			y = area.y + area.height;
			if (attr._tickLocation == TickLineAttr.INSIDE) {
				incX = (int) (area.width);
				decX = 0;
			} else if (attr._tickLocation == TickLineAttr.OUTSIDE) {
				decX = (int) (-area.width);
				incX = 0;
			} else if (attr._tickLocation == TickLineAttr.BOTH) {
				incX = (int) (area.width);
				decX = (int) (-area.width);
			}
			incY = 0;
			decY = 0;
			break;
		}

		int startId = 0;
		int endId = _ticks.length;
		if (!attr._plotMajorTicks)
			startId = 1;
		if (!attr._plotMinorTicks)
			endId = 1;

		if (DEBUG)
			System.out.println("Starting Tick Id " + startId);
		if (DEBUG)
			System.out.println("Ending Tick Id " + endId);

		for (int id = startId; id < endId; id++) {

			TickData tick = _ticks[id];
			double percentLength = getPercentLength(id);
			int incXL = (int) (percentLength * incX);
			int incYL = (int) (percentLength * incY);
			int decXL = (int) (percentLength * decX);
			int decYL = (int) (percentLength * decY);
			int fromX = x;
			int fromY = y;

			if (DEBUG) {
				System.out.println("x=" + x + " y=" + y);
				System.out.println("fromX=" + fromX + " fromY=" + fromY);
				System.out.println("incX=" + incX + " incY=" + incY);
				System.out.println("decX=" + decX + " decY=" + decY);
				System.out.println(attr._position);
			}
			if (DEBUG)
				System.out.println(tick.getScale());
			if (attr._position == TickLineAttr.BOTTOM
					|| attr._position == TickLineAttr.TOP) {
				for (int i = 0; i < tick.getNumber(); i++) {
					fromX = tick.getUCValue(i);
					if (DEBUG)
						System.out.println("For " + i + "th tick fromX="
								+ fromX + " fromY=" + fromY);
					gc.drawLine(fromX, fromY + decYL, fromX, fromY + incYL);
				}
			} else {
				for (int i = 0; i < tick.getNumber(); i++) {
					fromY = tick.getUCValue(i);
					if (DEBUG) {
						System.out.println("For " + i + "th tick fromX="
								+ fromX + " fromY=" + fromY);
						System.out.println(tick.getUCValue(i) + " : "
								+ tick.getDCValue(i));
					}
					gc.drawLine(fromX + decXL, fromY, fromX + incXL, fromY);
				}
			}
		}

		if (attr._plotLine) {
			if (attr._position == TickLineAttr.BOTTOM
					|| attr._position == TickLineAttr.TOP) {
				gc.drawLine(x, y, x + area.width, y);
			} else {
				gc.drawLine(x, y, x, y - area.height);
			}
		}

	}

	/**
   *
   */
	public void setPosition(int position) {
		TickLineAttr attr = (TickLineAttr) getAttributes();
		attr._position = position;
		if (attr._position == TickLineAttr.LEFT
				|| attr._position == TickLineAttr.RIGHT) {
			attr._orientation = TickLineAttr.VERTICAL;
		} else {
			attr._orientation = TickLineAttr.HORIZONTAL;
		}
	}

	/**
	 * sets size of tick
	 */
	public void setSize(Dimension d) {
		super.setSize(d);
		Rectangle area = getDrawBounds();
		setTickDimensions(area);
	}

	/**
	 * sets the scales on the tick data.
	 */
	public void setTickDimensions(Rectangle r) {
		if (DEBUG)
			System.out.println("Setting Tick dimensions " + r);
		TickLineAttr attr = (TickLineAttr) getAttributes();
		for (int id = 0; id < _ticks.length; id++) {
			TickData tick = _ticks[id];
			if (attr._position == TickLineAttr.BOTTOM
					|| attr._position == TickLineAttr.TOP) {
				tick.setUCRange(r.x, r.x + r.width);
			} else {
				tick.setUCRange(r.y + r.height, r.y);
			}
		}
	}

	/**
	 * implements preferred size
	 */
	public Dimension getPreferredSize() {
		Dimension d = getMinimumSize();
		d.width = 2 * d.width;
		d.height = 2 * d.height;
		return d;
	}

	/**
	 * implement minimum size
	 */
	public Dimension getMinimumSize() {
		TickLineAttr attr = (TickLineAttr) getAttributes();
		int n = 0;
		for (int id = 0; id < _ticks.length; id++) {
			n += _ticks[id].getNumber();
		}
		int minHeight = 0, minWidth = 0;
		Insets insets = getInsets();
		if (attr._position == TickLineAttr.BOTTOM
				|| attr._position == TickLineAttr.TOP) {
			minHeight = 4 + insets.top + insets.bottom;
			minWidth = 2 * n + insets.left + insets.right;
		} else {
			minHeight = 2 * n + insets.top + insets.bottom;
			minWidth = 4 + insets.left + insets.right;
		}
		return new Dimension(minWidth, minHeight);
	}

	/**
   *
   */
	public int getPosition() {
		return ((TickLineAttr) getAttributes())._position;
	}

	/**
   *
   */
	public int getTickLocation() {
		return ((TickLineAttr) getAttributes())._tickLocation;
	}

	/**
	 * returns the scale object of the tick line pixel coordinates to the data
	 * coordinates.
	 */
	public Scale getScale() {
		return _ticks[0].getScale();
	}

	/**
	 * sets the data coordinate range and thus setting the scale in the TickData
	 * objects
	 */
	public void setDCRange(double min, double max) {
		for (int i = 0; i < _ticks.length; i++) {
			_ticks[i].setDCRange(min, max);
		}
	}

	/**
	 * keeps the reference to the given TickData object array.
	 */
	public void setTickData(TickData[] ticks) {
		if (ticks == null)
			return;
		_ticks = ticks;
		if (_ticks.length == 2) {
			MAJOR2_TICK_INDEX = -1;
			MINOR_TICK_INDEX = 1;
		} else {
			MAJOR2_TICK_INDEX = 1;
			MINOR_TICK_INDEX = 2;
		}
		setTickDimensions(this.getBounds());
	}

	/**
   *
   */
	public TickData[] getTickData() {
		return _ticks;
	}

	/**
   *
   */
	private double getPercentLength(int i) {
		if (i == MAJOR_TICK_INDEX)
			return ((TickLineAttr) getAttributes())._percentMajorTickLength;
		else if (i == MAJOR2_TICK_INDEX)
			return ((TickLineAttr) getAttributes())._percentMajor2TickLength;
		else
			return ((TickLineAttr) getAttributes())._percentMinorTickLength;
	}

	/**
	 * sets bounds and the user coordinate range for the ticks
	 */
	public void setBounds(Rectangle r) {
		super.setBounds(r);
		setSize(new Dimension(r.width, r.height));
	}

	/**
	 * Returns its properties in a Properties object. These are saved on disk.
	 * 
	 * @param prefixTag
	 *            A tag to assign the context for these properties e.g. if these
	 *            properties belong to Axis class then prefixTag will be "Axis."
	 */
	public void toProperties(Properties p, String prefixTag) {
		String localTag = getPrefixTag(prefixTag);
		if (p == null)
			return;
		TickLineAttr attr = (TickLineAttr) getAttributes();

		p.put(localTag + "percentMajorTickLength", new Double(
				attr._percentMajorTickLength).toString());
		p.put(localTag + "percentMinorTickLength", new Double(
				attr._percentMinorTickLength).toString());

		// p.put(localTag + "thickness", new Integer(attr._thickness).toString()
		// );

		/*
		 * if (attr._position == TickLineAttr.BOTTOM) p.put(localTag +
		 * "position", "TickLineAttr.BOTTOM"); else if (attr._position ==
		 * TickLineAttr.TOP) p.put(localTag + "position", "TickLineAttr.TOP");
		 * else if (attr._position == TickLineAttr.LEFT) p.put(localTag +
		 * "position", "TickLineAttr.LEFT"); else if (attr._position ==
		 * TickLineAttr.RIGHT) p.put(localTag + "position",
		 * "TickLineAttr.RIGHT");
		 */

		if (attr._tickLocation == TickLineAttr.INSIDE)
			p.put(localTag + "tickLocation", "TickLineAttr.INSIDE");
		if (attr._tickLocation == TickLineAttr.OUTSIDE)
			p.put(localTag + "tickLocation", "TickLineAttr.OUTSIDE");
		if (attr._tickLocation == TickLineAttr.BOTH)
			p.put(localTag + "tickLocation", "TickLineAttr.BOTH");

		p.put(localTag + "plotMajorTicks", new Boolean(attr._plotMajorTicks)
				.toString());
		p.put(localTag + "plotMinorTicks", new Boolean(attr._plotMinorTicks)
				.toString());
		p.put(localTag + "plotLine", new Boolean(attr._plotLine).toString());

		super.toProperties(p, localTag);
	}

	/**
   *
   */
	public String getPrefixTag(String prefixTag) {
		return prefixTag + getName() + "TickLine.";
	}

	/**
	 * initializes attributes and state from Properties object.
	 */
	public void fromProperties(Properties p, String prefixTag) {
		String localTag = getPrefixTag(prefixTag);
		TickLineAttr attr = (TickLineAttr) getAttributes();

		String property = p.getProperty(localTag + "percentMajorTickLength");
		if (property != null) {
			attr._percentMajorTickLength = new Double(property).doubleValue();
		}
		property = p.getProperty(localTag + "percentMinorTickLength");
		if (property != null) {
			attr._percentMinorTickLength = new Double(property).doubleValue();
		}

		// property = p.getProperty(localTag + "thickness"); new
		// Integer(attr._thickness).toString() );
		/*
		 * if (attr._position == TickLineAttr.BOTTOM) property =
		 * p.getProperty(localTag + "position"); "TickLineAttr.BOTTOM"); else if
		 * (attr._position == TickLineAttr.TOP) property =
		 * p.getProperty(localTag + "position"); "TickLineAttr.TOP"); else if
		 * (attr._position == TickLineAttr.LEFT) property =
		 * p.getProperty(localTag + "position"); "TickLineAttr.LEFT"); else if
		 * (attr._position == TickLineAttr.RIGHT) property =
		 * p.getProperty(localTag + "position"); "TickLineAttr.RIGHT");
		 */

		property = p.getProperty(localTag + "tickLocation");
		if (attr._tickLocation == TickLineAttr.INSIDE)
			if (property != null) {
				attr._tickLocation = parseTickLocationProperty(property);
			}

		property = p.getProperty(localTag + "plotMajorTicks");
		if (property != null) {
			attr._plotMajorTicks = new Boolean(property).booleanValue();
		}

		property = p.getProperty(localTag + "plotMinorTicks");
		if (property != null) {
			attr._plotMinorTicks = new Boolean(property).booleanValue();
		}

		property = p.getProperty(localTag + "plotLine");
		if (property != null) {
			attr._plotLine = new Boolean(property).booleanValue();
		}

		super.fromProperties(p, getPrefixTag(prefixTag));
	}

	/**
	 * parses string containing property to get an integer constant for location
	 * of tick marks
	 */
	public final int parseTickLocationProperty(String property) {
		int o = TickLineAttr.INSIDE;
		if (property.equals("TickLineAttr.INSIDE")) {
			o = TickLineAttr.INSIDE;
		} else if (property.equals("TickLineAttr.OUTSIDE")) {
			o = TickLineAttr.OUTSIDE;
		} else if (property.equals("TickLineAttr.BOTH")) {
			o = TickLineAttr.BOTH;
		}
		return o;
	}

	/**
	 * Tick mark data.
	 */
	private TickData[] _ticks;
	/**
	 * index in _ticks array of major tick data
	 */
	private int MAJOR_TICK_INDEX = 0;
	/**
	 * index in _ticks array of major 2 tick data
	 */
	private int MAJOR2_TICK_INDEX = 1;
	/**
	 * index in _ticks array of minor tick data
	 */
	private int MINOR_TICK_INDEX = 2;

}
