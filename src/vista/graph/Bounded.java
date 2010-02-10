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
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * This interface is implemented by any object that has bounds defined by a
 * rectangular region. Also this object should be able to be queried for its
 * preferred/minimum dimensions.
 * <p>
 * This interface is primarily in this package for purposes of interacting with
 * layout managers and other interfaces that require only the bounds information
 * of a graphic element.
 * <p>
 * 
 * @see LayoutManager
 * @see GraphicElement
 * @author Nicky Sandhu
 * @version $Id: Bounded.java,v 1.1 2003/10/02 20:48:49 redwood Exp $
 */
public interface Bounded {
	/**
	 * set this element's bounds
	 * 
	 * @param
	 */
	public void setBounds(Rectangle r);

	/**
	 * gets this element's bounds
	 * 
	 * @return the minimum rectangular region enclosing this object
	 */
	public Rectangle getBounds();

	/**
	 * calculates the preferred size of this element
	 * 
	 * @return the preferred size
	 */
	public Dimension getPreferredSize();

	/**
	 * calculates the minimum size of this element
	 * 
	 * @return the minimum size
	 */
	public Dimension getMinimumSize();

	/**
	 * sets size of this element.
	 * 
	 * @param d
	 *            The dimension to which the element is to be set.
	 */
	public void setSize(Dimension d);

	/**
	 * gets the elements size, i.e. the width and height
	 * 
	 * @return a new Dimension object with the element size
	 */
	public Dimension getSize();

	/**
	 * get bounds of rectangle after allowing for insets
	 * 
	 * @return the rectangular region after accounting for insets
	 */
	public Rectangle getInsetedBounds();

	/**
	 * Sets the insets for this element
	 * 
	 * @param i
	 *            The insets for this bounded object
	 */
	public void setInsets(Insets i);

	/**
	 * The insets for this element
	 * 
	 * @return The Insets object.
	 */
	public Insets getInsets();

	/**
	 * checks to see if point is contained with element dimensions
	 * 
	 * @param p
	 *            The point for which containment is to be checked
	 * @return true if point lies within bounds
	 */
	public boolean contains(Point p);

	/**
	 * checks to see if point is contained with element dimensions
	 * 
	 * @param x
	 *            The x co-ordinate of the point
	 * @param y
	 *            The y co-ordinate of the point
	 * @return true if point lies within bounds
	 */
	public boolean contains(int x, int y);

	/**
	 * Checks if two rectangles intersect.
	 * 
	 * @param r
	 *            The rectangle with which intersection of this object is to be
	 *            checked
	 * @return true if rectangle intersects with this object.
	 */
	public boolean intersects(Rectangle r);
}
