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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Properties;

/**
 * This class draws a poly line using itself as a GEContainer to which the
 * LineElement ( a graphic element ) is added at the appropriate places to
 * simulate the line.
 * 
 * @author Nicky Sandhu
 * @version $Id: PolyLineElement.java,v 1.1 1999/12/29 17:16:18 nsandhu Exp $
 */
public class PolyLineElement extends GEContainer implements ScaledElement {
	public Polygon _filledPolygon = new Polygon();
	private int[] _xpoints;
	private int[] _ypoints;
	private int _npoints;
	/**
	 * for debugging
	 */
	public static final boolean DEBUG = false;

	/**
	 * constructor.
	 */
	public PolyLineElement(int[] xpoints, int[] ypoints, int npoints) {
		this(new LineElementAttr(), xpoints, ypoints, npoints);
	}

	/**
	 * constructor. In addition to construction parameters of its parent class
	 * it has color, thickess and style as its parameters
	 * 
	 * @param lineColor
	 *            color of the line
	 * @param thicknessMM
	 *            thickness of the line
	 * @param lineStyle
	 *            dashed or solid line
	 */
	public PolyLineElement(LineElementAttr attributes, int[] xpoints,
			int[] ypoints, int npoints) {
		super(attributes);
		_xpoints = xpoints;
		_ypoints = ypoints;
		_npoints = npoints;
	}

	/**
	 * set orienation to horizontal or vertical
	 */
	public void setOrientation(int orientation) {
		// LineElementAttr newAttrib = new LineElementAttr();
		// getAttributes().copyInto(newAttrib);
		// getAttributes() = newAttrib;
		getAttributes()._orientation = orientation;
	}

	/**
	 * true if x,y hits the drawing. More precise than contains(x,y)
	 */
	public boolean hitsDrawing(int x, int y) {
		return _filledPolygon.contains(x, y);
	}

	/**
	 * draws horizontal or vertical line with reference to starting point
	 */
	public void Draw() {
		LineElementAttr lea = (LineElementAttr) getAttributes();
		Color prevColor = getGraphics().getColor();
		getGraphics().setColor(lea._foregroundColor);
		Rectangle drawingArea = getDrawBounds();
		if (lea._orientation == lea.HORIZONTAL) {
			int y = (int) (drawingArea.y + drawingArea.height / 2.0 + _startingPoint.y);
			getGraphics().drawLine(drawingArea.x, y,
					drawingArea.x + drawingArea.width, y);
		} else if (lea._orientation == lea.VERTICAL) {
			int x = (int) (drawingArea.x + drawingArea.width / 2.0 + _startingPoint.x);
			getGraphics().drawLine(x, drawingArea.y, x,
					drawingArea.y + drawingArea.height);
		} else {
			// draws line across diagonal from top right to bottom left
			int x1 = drawingArea.x;
			int y1 = drawingArea.y;
			int x2 = drawingArea.x + drawingArea.width;
			int y2 = drawingArea.y + drawingArea.height;
			// have to call local method as it sets filled polygon for use in
			// get hit element ! should change this when i get the time.
			drawThickLine(getGraphics(), x1, y1, x2, y2, lea._thickness);
		}
		getGraphics().setColor(prevColor);
	}

	/**
	 * Simulates drawing of different thickness lines by using filled polygon.
	 * 
	 * @param g
	 *            Graphics on which to draw
	 * @param x1
	 *            The starting x co-ordinate of line
	 * @param y1
	 *            The starting y co-ordinate of line
	 * @param x2
	 *            The ending x co-ordinate of line
	 * @param y2
	 *            The ending y co-ordinate of line
	 * @param t
	 *            The thickness of the line in pixels
	 */
	public final void drawThickLine(Graphics g, int x1, int y1, int x2, int y2,
			double t) {
		double theta;
		if (Math.abs(x2 - x1) > 0.01)
			theta = Math.atan((y2 - y1) / (x2 - x1));
		else
			theta = Math.PI / 2;
		double ct = Math.cos(theta), st = Math.sin(theta);
		_filledPolygon = new Polygon();
		_filledPolygon.addPoint((int) (x1 - t / 2 * st),
				(int) (y1 + t / 2 * ct));
		_filledPolygon.addPoint((int) (x1 + t / 2 * st),
				(int) (y1 - t / 2 * ct));
		_filledPolygon.addPoint((int) (x2 + t / 2 * st),
				(int) (y2 - t / 2 * ct));
		_filledPolygon.addPoint((int) (x2 - t / 2 * st),
				(int) (y2 + t / 2 * ct));
		g.fillPolygon(_filledPolygon);
	}

	/**
	 * calculates the preferred size of this element
	 * 
	 * @return The preferred size
	 */
	public Dimension getPreferredSize() {
		GEAttr attr = getAttributes();
		if (attr._orientation == attr.HORIZONTAL)
			return new Dimension(25, 10);

		return new Dimension(10, 25);
	}

	/**
	 * calculates the minimum size of this element
	 * 
	 * @return the minimum size
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
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
		LineElementAttr attr = (LineElementAttr) getAttributes();

		p.put(localTag + "color", attr._foregroundColor.toString());
		if (attr._style == LineElementAttr.PLAIN)
			p.put(localTag + "style", "LineElementAttr.PLAIN");
		else if (attr._style == LineElementAttr.DASHED)
			p.put(localTag + "style", "LineElementAttr.DASHED");
		else if (attr._style == LineElementAttr.DOTTED)
			p.put(localTag + "style", "LineElementAttr.DOTTED");

		p.put(localTag + "thickness", new Integer(attr._thickness).toString());

		super.toProperties(p, localTag);
	}

	/**
	 * initializes attributes and state from Properties object.
	 */
	public void fromProperties(Properties p, String prefixTag) {

		super.fromProperties(p, getPrefixTag(prefixTag));
	}

	/**
   *
   */
	public String getPrefixTag(String prefixTag) {
		return prefixTag + getName() + "LineElement.";
	}

	/**
   *
   */
	public final static int parseStyleProperty(String property) {
		int o = 0;
		if (property.equals("LineElementAttr.PLAIN")) {
			o = LineElementAttr.PLAIN;
		} else if (property.equals("LineElementAttr.DASHED")) {
			o = LineElementAttr.DASHED;
		} else if (property.equals("LineElementAttr.DOTTED")) {
			o = LineElementAttr.DOTTED;
		}
		return o;
	}

	/**
	 * The point which lies on the line drawn. This may or may not be at the
	 * extremes of the line. Also this point is measured from the middle of both
	 * width and height dimensions. In other words point x=0, y=0 is exactly in
	 * the middle of the region.
	 */
	private Point _startingPoint = new Point(0, 0);
	private Point _endingPoint = new Point(0, 0);
}
