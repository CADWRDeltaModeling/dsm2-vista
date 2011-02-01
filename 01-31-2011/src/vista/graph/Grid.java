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
import java.awt.Rectangle;

/**
 * This controls the drawing of a line within the bounds of the given rectangle.
 * <p>
 * Currently this class only draws horizontal and vertical lines clipped within
 * the drawing region. Furthermore each line is drawn from a starting point to
 * the end of the drawing Area dimensions.
 * <p>
 * Also grid are drawn for each axis by the Plot object. This further means that
 * the lines for grid on opposing axis must match up. This can be done by making
 * sure that both axes share the same major tick marks. This is something that
 * needs to be done inside Plot object
 * 
 * @see Axis
 * @see Plot
 * @author Nicky Sandhu
 * @version $Id: Grid.java,v 1.1 2003/10/02 20:49:01 redwood Exp $
 */
public class Grid extends GraphicElement {

	/**
	 * for debugging
	 */
	public static final boolean DEBUG = false;

	/**
	 * Constructor initializes the attributes of this element.
	 */
	public Grid(Axis axis) {
		this(new GridAttr(), axis);
	}

	/**
	 * Constructor initializes the attributes of this element.
	 */
	public Grid(GridAttr attributes, Axis axis) {
		super(attributes);
		_axis = axis;

	}

	/**
	 * draws horizontal or vertical line with the major tick marks as references
	 */
	public void Draw() {
		GridAttr ga = (GridAttr) getAttributes();
		Rectangle drawingArea = getDrawBounds();
		Color prevColor = getGraphics().getColor();
		getGraphics().setColor(ga._color);
		_tickData = _axis.getMajorTickData();
		int n = _tickData.getNumber();
		if (ga._orientation == GEAttr.HORIZONTAL) {
			for (int i = 1; i < n - 1; i++) {
				int y = _tickData.getUCValue(i);
				getGraphics().drawLine(drawingArea.x, y,
						drawingArea.x + drawingArea.width, y);
			}
		} else if (ga._orientation == ga.VERTICAL) {
			for (int i = 1; i < n - 1; i++) {
				int x = _tickData.getUCValue(i);
				getGraphics().drawLine(x, drawingArea.y, x,
						drawingArea.y + drawingArea.height);
			}
		}

		getGraphics().setColor(prevColor);
	}

	/**
	 * Calculates the preferred size of this element
	 */
	public Dimension getPreferredSize() {
		GEAttr attr = getAttributes();
		if (attr._orientation == attr.HORIZONTAL)
			return new Dimension(25, 10);

		return new Dimension(10, 25);
	}

	/**
	 * Calculates the minimum size of this element
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * The major tick marks
	 */
	private TickData _tickData;
	private Axis _axis;
}
