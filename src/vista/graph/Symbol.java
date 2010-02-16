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
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Properties;

/**
 * Symbol is a polygon shape drawn with respect to the x and y coordinates of
 * rectangle. No scaling is currently done.
 * <p>
 * The symbol is a polygon and can be represented by a series of x and y
 * coordinates. The symbol may be filled or outlined. The x and y coordinates
 * are taken to be in units of pixels ~ 1/72 inch for most displays.
 * 
 * @author Nicky Sandhu
 * @version $Id: Symbol.java,v 1.1 2003/10/02 20:49:08 redwood Exp $
 */
public class Symbol extends GraphicElement {
	/**
	 * for debugging
	 */
	public static final boolean DEBUG = false;
	private Polygon _sp = new Polygon();

	/**
	 * Constructor.
	 */
	public Symbol() {
		this(new SymbolAttr());
	}

	/**
	 * Constructor.
	 */
	public Symbol(SymbolAttr attributes) {
		super(attributes);
		setName("Symbol");
	}

	/**
	 * true if x,y hits the drawing. More precise than contains(x,y)
	 */
	public boolean hitsDrawing(int x, int y) {
		Rectangle r = getInsetedBounds();
		_sp.translate(r.x, r.y);
		boolean drawingHit = _sp == null ? false : _sp.contains(x, y);
		_sp.translate(-r.x, -r.y);
		return drawingHit;
	}

	/**
	 * draws polygon representing symbol. Currently for efficiency no scaling is
	 * done. However a subclass will do so. In this way it would be the users
	 * choice to use the most suitable class.
	 */
	public void Draw() {
		SymbolAttr attr = (SymbolAttr) getAttributes();
		Graphics gc = getGraphics();

		Rectangle r = getInsetedBounds();
		_sp = attr.getSymbol();
		_sp.translate(r.x, r.y);

		if (attr._isFilled)
			getGraphics().fillPolygon(_sp);
		else
			getGraphics().drawPolygon(_sp);

		_sp.translate(-r.x, -r.y);
	}

	/**
	 * calculates the preferred size of this element
	 * 
	 * @return The preferred size
	 */
	public Dimension getPreferredSize() {
		return new Dimension(25, 25);
	}

	/**
	 * calculates the minimum size of this element
	 * 
	 * @return The minimum size
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
   *
   */
	public String getPrefixTag(String prefixTag) {
		return prefixTag + getName() + ".";
	}

	/**
   *
   */
	public void toProperties(Properties p, String prefixTag) {
		super.toProperties(p, getPrefixTag(prefixTag));
	}

	/**
   *
   */
	public void fromProperties(Properties p, String prefixTag) {
		super.fromProperties(p, getPrefixTag(prefixTag));
	}
}
