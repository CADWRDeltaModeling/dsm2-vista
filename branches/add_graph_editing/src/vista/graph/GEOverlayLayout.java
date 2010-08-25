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
 * Lays out Bounded(s) on top of each other.
 * 
 * @author Nicky Sandhu (DWR).
 * @version $Id: GEOverlayLayout.java,v 1.1 2003/10/02 20:48:58 redwood Exp $
 */
public class GEOverlayLayout implements GELayoutManager {
	/**
	 * debuggin' purposes
	 */
	public static final boolean DEBUG = false;

	/**
	 * constructor
	 */
	public GEOverlayLayout() {
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
	 * the Boundeds in the specified parent BoundedComposite.
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
			pWidth = Math.max(pWidth, d.width);
			pHeight = Math.max(pHeight, d.height);
		}
		return new Dimension(pWidth + (insets.left + insets.right), pHeight
				+ (insets.top + insets.bottom));
	}

	/**
	 * Calculates the minimum size dimensions for the specified panel given the
	 * Boundeds in the specified parent BoundedComposite.
	 * 
	 * @param parent
	 *            the Bounded to be laid out
	 * @see #preferredLayoutSize
	 */
	public Dimension minimumLayoutSize(BoundedComposite parent) {
		return preferredLayoutSize(parent);
	}

	/**
	 * Lays out the BoundedComposite in the specified panel. Lays out all the
	 * elements in a single row with the space equally divided amongst the
	 * elements.
	 * 
	 * @param parent
	 *            the Bounded which needs to be laid out
	 */
	public void layoutContainer(BoundedComposite parent) {
		int n = parent.getElementCount();

		Bounded ge = null;

		Rectangle parentBounds = parent.getInsetedBounds();
		Rectangle geBounds = new Rectangle(parentBounds);

		for (int i = 0; i < n; i++) {
			ge = (Bounded) parent.getElement(i);
			ge.setBounds(geBounds);
		}
	}
}
