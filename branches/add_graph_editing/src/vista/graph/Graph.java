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
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Properties;

/**
 * This class manages the plot, its title and its legend. Currently it uses the
 * border layout manager to layout these elements with title to the north, the
 * plot in the center and the legend in the south. In further releases the
 * layout manager should be more flexibile. Something along the lines of
 * java.awt.GridBagLayout.
 * 
 * @see Plot
 * @see Legend
 * @see TextLine
 * @author Nicky Sandhu
 * @version $Id: Graph.java,v 1.1 2003/10/02 20:48:59 redwood Exp $
 */
public class Graph extends GEContainer implements FontResizable {
	/**
	 * debuggin
	 */
	public static final boolean DEBUG = false;

	/**
	 * constructor. Sets layout to border layout.
	 */
	public Graph() {
		this(new GraphAttr());
	}

	/**
	 * constructor. Sets layout to border layout.
	 */
	public Graph(GraphAttr attributes) {
		super(attributes);
		setLayout(new GEBorderLayout());
	}

	/**
	 * adds title to graph
	 */
	public void setTitle(String title) {
		TextLineAttr tla = ((GraphAttr) getAttributes()).getTitleAttributes();
		TextLine tl = new TextLine(tla, title);
		tl.setName("GraphTitle");
		getLayout().addLayoutElement(GEBorderLayout.NORTH, tl);
		add(tl);
	}

	/**
   *
   */
	public void add(Leaf ge) {
		if (ge instanceof Plot) {
			addPlot((Plot) ge);
		} else {
			super.add(ge);
		}
	}

	/**
	 * adds plot to graph
	 */
	public void addPlot(Plot plot) {
		_plot = plot;
		_plot.setAttributes(((GraphAttr) getAttributes()).getPlotAttributes());
		// set the foreground color same as plot
		_plot.getAttributes()._foregroundColor = getAttributes()._foregroundColor;
		_plot.setInsets(new Insets(5, 25, 5, 25));
		getLayout().addLayoutElement(GEBorderLayout.CENTER, _plot);
		super.add(_plot);
	}

	/**
	 * adds grid to plot
	 */
	public void addGrid() {
		_plot.addGrid(AxisAttr.TOP);
		_plot.addGrid(AxisAttr.BOTTOM);
		_plot.addGrid(AxisAttr.LEFT);
		_plot.addGrid(AxisAttr.RIGHT);
	}

	/**
	 * adds legend to graph
	 */
	public void setLegend(Legend legend) {
		_legend = legend;
		_legend.setAttributes(((GraphAttr) getAttributes())
				.getLegendAttributes());
		getLayout().addLayoutElement(GEBorderLayout.SOUTH, _legend);
		add(_legend);
	}

	// /**
	// * adds a data set to the plot
	// */
	// public void addData(DataReference ref, int xAxisPosition, int
	// yAxisPosition){
	// if (isAxisPositionValid(xAxisPosition, yAxisPosition)) {
	// DataSet ds = null;
	// try {
	// ds=ref.getData();
	// }
	// catch(DataRetrievalException dre){
	// throw new RuntimeException("Could not retrieve data: " + ref);
	// }
	// double [] missingValues = { Float.MIN_VALUE,
	// DWR.Data.Set.Constants.MISSING_VALUE,
	// DWR.Data.Set.Constants.MISSING_RECORD
	// };
	// ElementFilter compFilter = new CompositeFilter( new ElementFilter [] {
	// new MultiValueFilter(missingValues),
	// FlagUtils.MISSING_FILTER, FlagUtils.REJECT_FILTER, new NaNFilter()
	// });

	// DefaultCurveDataModel cdm = new DefaultCurveDataModel(ds, compFilter,
	// xAxisPosition,
	// yAxisPosition,
	// ds.getName());
	// DefaultCurve dataCurve = new DefaultCurve(new CurveAttr(), cdm);
	// _plot.addCurve(dataCurve);
	// // set attributes
	// CurveAttr ca = (CurveAttr) dataCurve.getAttributes();
	// if( _colorTableIndex < _colorTable.length) {
	// ca._foregroundColor = _colorTable[_colorTableIndex];
	// if (DEBUG) System.out.println("Setting color of curve to " +
	// ca._foregroundColor);
	// if (_legend != null){
	// LegendItemAttr lia = new LegendItemAttr();
	// lia._foregroundColor = _colorTable[_colorTableIndex];
	// lia.getLineElementAttributes()._foregroundColor =
	// _colorTable[_colorTableIndex];
	// _legend.add(new LegendItem(lia, dataCurve));
	// }
	// }
	// else{
	// // throw an ArrayIndexOutOfBoundException with meaning ful message
	// }
	// _colorTableIndex++;
	// } else {
	// // throw an IllegalArgumentException
	// }
	// }
	/**
	 * checks if axis positions given are valid.
	 */
	public boolean isAxisPositionValid(int xAxisPosition, int yAxisPosition) {
		boolean valid = true;
		if (xAxisPosition == AxisAttr.LEFT || xAxisPosition == AxisAttr.RIGHT)
			valid = false;
		if (yAxisPosition == AxisAttr.TOP || yAxisPosition == AxisAttr.BOTTOM)
			valid = false;
		return valid;
	}

	/**
	 * returns the plot object. Currently only one plot per graph. But this will
	 * be updated soon.
	 */
	public Plot getPlot() {
		Plot plot = null;
		if (_plot != null)
			plot = _plot.getCurrentPlot();
		return plot;
	}

	/**
	 * Returns the reference to the legend
	 */
	public Legend getLegend() {
		return _legend;
	}

	/**
	 * returns a reference to the title object
	 */
	public TextLine getTitle() {
		for (int i = 0; i < getElementCount(); i++) {
			GraphicElement ge = (GraphicElement) getElement(i);
			if (ge instanceof TextLine)
				return (TextLine) ge;
		}
		return null;
	}

	/**
	 * Returns its properties in a Properties object. These are saved on disk.
	 * 
	 * @param prefixTag
	 *            A tag to assign the context for these properties e.g. if these
	 *            properties belong to Axis class then prefixTag will be "Axis."
	 */
	public void toProperties(Properties p, String prefixTag) {
		super.toProperties(p, getPrefixTag(prefixTag));
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
		return prefixTag + "Graph.";
	}

	/**
	 * index to the current _colorTable color
	 */
	private int _colorTableIndex = 0;
	/**
	 * The color table initialized to default table
	 */
	private Color[] _colorTable = GraphAttr.getDefaultColorTable();
	/**
	 * The plot
	 */
	private Plot _plot;
	/**
	 * The legend
	 */
	private Legend _legend;
	/**
	 * Stores the bounds of the first time the graph is displayed.
	 */
	private Rectangle _originalBounds = null;
}
