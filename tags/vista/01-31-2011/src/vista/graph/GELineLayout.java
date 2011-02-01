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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * Lays out Bounded(s) in a linear fashion. It can layout elements either
 * vertically or horizontally. Elements may be either centered on bounds or
 * between bounds. Each element is given an equal space which corresponds to the
 * maximum sized element
 * <p>
 * 
 * The layout of components is done either by scaling the components in the
 * ratio of actual to preferred sizes or just by placing them without any
 * scaling.
 * <p>
 * 
 * @author Nicky Sandhu (DWR).
 * @version $Id: GELineLayout.java,v 1.1 2003/10/02 20:48:58 redwood Exp $
 */
public class GELineLayout implements GELayoutManager {
	/**
	 * debuggin' purposes
	 */
	public static final boolean DEBUG = false;
	/**
	 * Element centered on boundaries
	 */
	public static final int CENTERED_ON_BOUNDS = 1;
	/**
	 * Elements centered between boundaries.
	 */
	public static final int CENTERED_BETWEEN_BOUNDS = CENTERED_ON_BOUNDS + 1;
	/**
	 * horizontal layout
	 */
	public final static int HORIZONTAL = 10;
	/**
	 * vertical layout
	 */
	public final static int VERTICAL = HORIZONTAL + 1;

	/**
	 * constructor
	 */
	public GELineLayout(int orientation, int elementPosition) {
		_orientation = orientation;
		_elementPosition = elementPosition;
	}

	/**
	 * Adds the specified element with the specified name to the layout.
	 * 
	 * @param name
	 *            the element name
	 * @param comp
	 *            the element to be added
	 */
	public void addLayoutElement(Object obj, Bounded comp) {

	}

	/**
	 * Removes the specified element from the layout.
	 * 
	 * @param comp
	 *            the element ot be removed
	 */
	public void removeLayoutElement(Bounded comp) {
	}

	/**
	 * Calculates the preferred size dimensions for the specified panel given
	 * the Boundeds in the specified parent GEContainer.
	 * 
	 * @param parent
	 *            the Bounded to be laid out
	 * 
	 * @see #minimumLayoutSize
	 */
	public Dimension preferredLayoutSize(BoundedComposite parent) {
		int n = parent.getElementCount();
		Insets insets = parent.getInsets();

		int pWidth = 0;
		int pHeight = 0;
		Bounded ge;

		for (int i = 0; i < n; i++) {
			ge = (Bounded) parent.getElement(i);
			Dimension d = ge.getPreferredSize();
			if (_orientation == VERTICAL) {
				pHeight += d.height;
				pWidth = Math.max(pWidth, d.width);
			} else if (_orientation == HORIZONTAL) {
				pWidth += d.width;
				pHeight = Math.max(pHeight, d.height);
			}
		}
		return new Dimension(pWidth + (insets.left + insets.right), pHeight
				+ (insets.top + insets.bottom));
	}

	/**
	 * Calculates the minimum size dimensions for the specified panel given the
	 * Boundeds in the specified parent GEContainer.
	 * 
	 * @param parent
	 *            the Bounded to be laid out
	 * @see #preferredLayoutSize
	 */
	public Dimension minimumLayoutSize(BoundedComposite parent) {
		return preferredLayoutSize(parent);
	}

	/**
	 * Lays out the GEContainer in the specified panel. Lays out all the
	 * elements in a single row with the space equally divided amongst the
	 * elements.
	 * 
	 * @param parent
	 *            the Bounded which needs to be laid out
	 */
	public void layoutContainer(BoundedComposite parent) {
		int n = parent.getElementCount();
		if (parent.getElementCount() == 0)
			return;
		Bounded ge = null;

		Dimension geSize = null;

		Dimension parentPreferredSize = parent.getPreferredSize();
		Dimension parentActualSize = parent.getSize();

		Rectangle parentBounds = parent.getInsetedBounds();
		Rectangle geBounds = new Rectangle(parentBounds);

		double xOffset = 0, yOffset = 0;
		double nextX = 0, nextY = 0;

		double widthScale = 1.0;
		double heightScale = 1.0;
		if (_scaleComponents) {
			widthScale = (1.0 * parentActualSize.width)
					/ parentPreferredSize.width;
			heightScale = (1.0 * parentActualSize.height)
					/ parentPreferredSize.height;
		}

		ge = (Bounded) parent.getElement(0);
		geSize = ge.getPreferredSize();

		geSize.width = (int) Math.round(geSize.width * widthScale);
		geSize.height = (int) Math.round(geSize.height * heightScale);

		if (DEBUG)
			System.out.println("Scaled Size is " + geSize);
		if (DEBUG)
			System.out.println("Parent " + parent + " bounds is "
					+ parentBounds);

		if (_elementPosition == CENTERED_ON_BOUNDS) {
			if (_orientation == HORIZONTAL)
				xOffset = geSize.width / 2.0;
			else if (_orientation == VERTICAL)
				yOffset = geSize.height / 2.0;
		}

		nextX = parentBounds.x + xOffset;
		nextY = parentBounds.y + yOffset;
		double width = 0.0, height = 0.0;

		for (int i = 0; i < n; i++) {

			ge = (Bounded) parent.getElement(i);

			geSize = ge.getPreferredSize();

			width = geSize.width * widthScale;
			height = geSize.height * heightScale;

			if (_orientation == HORIZONTAL) {
				geBounds.width = (int) Math.round(width);
				geBounds.height = parentActualSize.height;
			} else if (_orientation == VERTICAL) {
				geBounds.width = parentActualSize.width;
				geBounds.height = (int) Math.round(height);
			}

			geBounds.x = (int) Math.round(nextX);
			geBounds.y = (int) Math.round(nextY);

			ge.setBounds(geBounds);
			if (DEBUG)
				System.out.println("Element " + i + " has bounds " + geBounds);

			if (_orientation == HORIZONTAL)
				nextX += width;
			else if (_orientation == VERTICAL)
				nextY += height;
		}
	}

	/**
	 * sets the scaling paramenter
	 * 
	 * @param b
	 *            true if scaling is to be done
	 */
	public void setScaleComponents(boolean b) {
		_scaleComponents = b;
	}

	/**
	 * sets the position of elements to be centered on bounds or between bounds
	 */
	public void setElementPositioning(int position) {
		_elementPosition = position;
	}

	/**
	 * gets the positioning of the elements
	 */
	public int getElementPositioning() {
		return _elementPosition;
	}

	/**
	 * sets orientation of layout
	 */
	public void setOrientation(int orientation) {
		_orientation = orientation;
	}

	/**
	 * gets orientation of layout
	 */
	public int getOrientation() {
		return _orientation;
	}

	/**
	 * element positioning
	 */
	private int _elementPosition = CENTERED_ON_BOUNDS;
	/**
	 * layout orientation
	 */
	private int _orientation = HORIZONTAL;
	/**
	 * if true then scale components in the ratio of actual to preferred size.
	 */
	private boolean _scaleComponents = true;
}
