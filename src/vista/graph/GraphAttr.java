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
 * Attributes of a graph.
 * 
 * @author Nicky Sandhu
 * @version $Id: GraphAttr.java,v 1.1 2003/10/02 20:49:00 redwood Exp $
 */
public class GraphAttr extends GEAttr {
	/**
	 * attributes for the title
	 */
	private TextLineAttr _tla = new TextLineAttr();
	/**
	 * attributes for the plot
	 */
	private PlotAttr _pa = new PlotAttr();
	/**
	 * attributes for the legend
	 */
	private GEAttr _la = new LegendAttr();

	/**
	 * copies the fields into the given GEAttr object. Also copies in the
	 * TextLineAttr if the object is of that type.
	 */
	public void copyInto(GEAttr ga) {
		super.copyInto(ga);
		if (ga instanceof GraphAttr) {
			GraphAttr tla = (GraphAttr) ga;
		}
	}

	/**
	 * sets plot attributes
	 */
	public void setPlotAttributes(PlotAttr pa) {
		_pa = pa;
	}

	/**
	 * gets plot attributes
	 */
	public PlotAttr getPlotAttributes() {
		return _pa;
	}

	/**
	 * sets legend attributes
	 */
	public void setLegendAttributes(GEAttr la) {
		_la = la;
	}

	/**
	 * gets legend attributes
	 */
	public GEAttr getLegendAttributes() {
		return _la;
	}

	/**
	 * sets title attributes
	 */
	public void setTitleAttributes(TextLineAttr tla) {
		_tla = tla;
	}

	/**
	 * gets title attributes
	 */
	public TextLineAttr getTitleAttributes() {
		return _tla;
	}

	/**
	 * sets up default color table.
	 */
	public static Color[] getDefaultColorTable() {
		Color[] colorTable = { Color.red, Color.green, Color.blue, Color.pink,
				Color.cyan, Color.orange, Color.black, Color.magenta,
				Color.yellow, Color.white };
		return colorTable;
	}
}
