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
package vista.app;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;

import vista.graph.Axis;
import vista.graph.Curve;
import vista.graph.CurveAttr;
import vista.graph.DefaultGraphFactory;
import vista.graph.GEContainer;
import vista.graph.Graph;
import vista.graph.GraphFactory;
import vista.graph.GraphicElement;
import vista.graph.Legend;
import vista.graph.LegendItem;
import vista.graph.LegendItemAttr;
import vista.graph.MultiPlot;
import vista.graph.Plot;
import vista.graph.SimpleTickGenerator;
import vista.graph.TextLine;
import vista.graph.TextLineAttr;
import vista.graph.TickGenerator;
import vista.graph.TimeTickGenerator;
import vista.set.DataReference;
import vista.set.DataRetrievalException;
import vista.set.DataSet;

/**
 * Builds a graph from given data sets.
 * 
 * @author Nicky Sandhu
 * @version $Id: DefaultGraphBuilder.java,v 1.1 2003/10/02 20:48:28 redwood Exp
 *          $
 */
public class DefaultGraphBuilder implements GraphBuilder {
	/**
	 * initializes the graph builder with no data sets
	 */
	public DefaultGraphBuilder() {
		_dataRefs = new ArrayList<DataReference>();
	}

	/**
	 * adds a data set to the graph builder
	 */
	public void addData(DataReference ref) {
		if (ref == null)
			return;
		_dataRefs.add(ref);
	}

	/**
	 * removes the dat set
	 */
	public void removeData(DataReference ref) {
		if (ref == null)
			return;
		_dataRefs.remove(ref);
	}

	/**
	 * removes all the data sets
	 */
	public void removeAll() {
		if (_dataRefs.size() > 0) {
			_dataRefs.clear();
		}
	}

	/**
	 * creates graph from existing data sets
	 */
	public Graph[] createGraphs() {
		if (_dataRefs.size() == 0)
			return null;
		DataReference[] drefs = new DataReference[_dataRefs.size()];
		drefs = _dataRefs.toArray(drefs);
		GraphBuilderInfo info = new GraphBuilderInfo(drefs, MainProperties
				.getProperties());
		if (DEBUG)
			System.out.println(info);
		Graph[] graphs = new Graph[info.getNumberOfGraphs()];
		for (int i = 0; i < info.getNumberOfGraphs(); i++)
			graphs[i] = createGraphForIndex(info, i);
		return graphs;
	}

	/**
	 * creates graph from data sets
	 */
	private Graph createGraphForIndex(GraphBuilderInfo info, int index) {
		String title = info.getGraphTitle(index);

		// create container hierarchy of graph -> multi plot -> plot
		Graph graph = _factory.createGraph();
		MultiPlot multiPlot = _factory.createMultiPlot();
		graph.addPlot(multiPlot);
		//
		int nplots = info.getNumberOfPlots(index);
		for (int i = 0; i < nplots; i++) {
			multiPlot.add(_factory.createPlot());
		}

		// set title
		graph.setTitle(info.getGraphTitle(index));
		// Graph Insets: Template file
		graph.setInsets(new java.awt.Insets(5, 20, 10, 25));
		// Legend attributes: Template file
		Legend legend = _factory.createLegend();
		graph.setLegend(legend);

		for (int pIndex = 0; pIndex < nplots; pIndex++) {

			multiPlot.setCurrentPlot(pIndex);
			// Plot Attributes: Template file
			Plot plot = graph.getPlot();
			plot.setInsets(new java.awt.Insets(20, 5, 20, 50));
			// add axes to plot
			int numberOfAxes = 4;
			TickGenerator simpleTG = new SimpleTickGenerator();
			TickGenerator timeTG = new TimeTickGenerator();
			for (int i = 1; i <= numberOfAxes; i++) {
				String axisLabel = info.getAxisLabel(index, pIndex, i);
				if (axisLabel != null) {
					Axis axis = plot.createAxis(i);
					axis.setAxisLabel(axisLabel);
					if (axisLabel.equals("TIME")) {
						axis.setTickGenerator(timeTG);
					} else {
						axis.setTickGenerator(simpleTG);
					}
				}
			}
			// add data to plot
			DataReference[] refs = info.getDataReferences(index, pIndex);
			if (refs == null)
				throw new IllegalArgumentException(
						"All references to be graphed are null");
			for (int i = 0; i < refs.length; i++) {
				if (DEBUG)
					System.out.println("Adding reference " + refs[i]
							+ " to plot");
				int xPos = info.getXAxisPosition(refs[i]);
				int yPos = info.getYAxisPosition(refs[i]);
				try {
					DataSet ds = refs[i].getData();
					Curve crv = CurveFactory.createCurve(refs[i], xPos, yPos,
							info.getLegendLabel(refs[i]));
					plot.addCurve(crv);
					CurveAttr ca = (CurveAttr) crv.getAttributes();
					Color currentColor = _factory.getNextColor();
					ca._foregroundColor = currentColor;
					if (DEBUG)
						System.out.println("Done adding curve");
					LegendItem li = _factory.createLegendItem();
					li.setLegendName(info.getLegendLabel(refs[i]));
					li.setCurve(crv);
					LegendItemAttr lia = (LegendItemAttr) li.getAttributes();
					legend.add(li);
					lia._foregroundColor = Color.black;
				} catch (DataRetrievalException dre) {
					dre.printStackTrace();
				}
			}
		}

		// Scale components in layout resizing: Template file
		// GEBorderLayout plotLayout = (GEBorderLayout) plot.getLayout();
		// plotLayout.setScaleComponents(true);
		TextLineAttr dateAttr = new TextLineAttr();
		dateAttr._font = new java.awt.Font("Times Roman", java.awt.Font.PLAIN,
				10);
		dateAttr._foregroundColor = java.awt.Color.red;
		dateAttr._resizeProportionally = true;
		dateAttr._justification = TextLineAttr.RIGHT;
		graph.getLegend().add(new TextLine(dateAttr, new Date().toString()));

		if (DEBUG)
			printAll(graph);
		return graph;
	}

	/**
   *
   */
	void printAll(GEContainer c) {
		System.out.println(c + " { ");
		int count = c.getElementCount();
		for (int i = 0; i < count; i++) {
			GraphicElement ge = (GraphicElement) c.getElement(i);
			if (ge instanceof GEContainer)
				printAll((GEContainer) ge);
			else
				print(ge);
		}
		System.out.println(" }");
	}

	/**
   *
   */
	void print(GraphicElement ge) {
		System.out.println(ge);
	}

	/**
   *
   */
	private ArrayList<DataReference> _dataRefs;
	/**
	 * debuggin' flag...
	 */
	private static final boolean DEBUG = false;
	/**
   *
   */
	private GraphFactory _factory = new DefaultGraphFactory();
}
