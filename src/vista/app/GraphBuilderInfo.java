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

import java.util.Enumeration;
import java.util.Properties;

import vista.graph.AxisAttr;
import vista.set.DataReference;
import vista.set.PathnameFormat;
import COM.objectspace.jgl.Array;

/**
 * This class calculates the x and y axis positions of the given data sets. Each
 * unique pair of units is assigned a pair of axes. Each data set is assigned a
 * pair of units in the existing pair of units or a pair of such units is
 * created for it.
 */
public class GraphBuilderInfo {
	static int DATA_PER_PLOT = 5;
	static int PLOTS_PER_GRAPH = 2;
	static boolean DEBUG = false;

	private Array _dataRefs, _graphInfo, _dataSetInfo;
	private Properties _props;
	private boolean _useUnits;
	private static int ngraphs = 1;

	/**
   *
   */
	public static String getTitleTemplate() {
		return MainProperties.getProperty("graph.titleTemplate");
	}

	/**
   *
   */
	public static String getLegendTemplate() {
		return MainProperties.getProperty("graph.legendTemplate");
	}

	/**
	 * create an empty graph info object
	 */
	public GraphBuilderInfo(Properties props) {
		_props = props;
		if (_props.getProperty("graph.useUnits").indexOf("true") >= 0)
			_useUnits = true;
		else
			_useUnits = false;
		_dataRefs = new Array();
		_graphInfo = new Array();
		_dataSetInfo = new Array();
	}

	/**
	 * Constructor. Builds the graph information from the given data set
	 * applying simple rules.
	 */
	public GraphBuilderInfo(DataReference[] dsArray, Properties props) {
		this(props);
		if (dsArray == null)
			return;
		for (int i = 0; i < dsArray.length; i++) {
			if (dsArray[i] != null)
				_dataRefs.add(dsArray[i]);
		}
		if (_dataRefs.size() == 0)
			throw new IllegalArgumentException("All data sets are null");
		updateInfo();
	}

	/**
	 * add data set to graph info.
	 */
	public void add(DataReference ds) {
		if (ds == null)
			return;
		_dataRefs.add(ds);
		updateInfo();
	}

	/**
	 * remove data set and regenerate graphs and plots information
	 */
	public void remove(DataReference ds) {
		if (ds == null)
			return;
		_dataRefs.remove(ds);
		updateInfo();
	}

	/**
	 * Gets the number of graphs
	 */
	public int getNumberOfGraphs() {
		return _graphInfo.size();
	}

	/**
	 * get number of plots for given graph index
	 */
	public int getNumberOfPlots(int graphIndex) {
		if (graphIndex >= getNumberOfGraphs())
			return 0;
		else
			return ((GraphInfo) _graphInfo.at(graphIndex)).getNumberOfPlots();
	}

	/**
	 * gets the graph title for graph
	 */
	public String getGraphTitle(int graphIndex) {
		if (graphIndex >= getNumberOfGraphs())
			return "";
		// else return ((GraphInfo) _graphInfo.at(graphIndex)).title;
		else {
			DataReference[] refs = new DataReference[_dataRefs.size()];
			_dataRefs.copyTo(refs);
			return PathnameFormat.format(getTitleTemplate(), refs);
		}
	}

	/**
	 * returns the legend label for the given data set
	 */
	public String getLegendLabel(DataReference ds) {
		DataSetInfo dsi = getDataSetInfo(ds);
		if (dsi == null)
			return null;
		else
			return dsi.getLegendLabel();
	}

	/**
	 * Gets the graph index for the particular data set only if the data set had
	 * been in the data set array specified in the constructor.
	 * 
	 * @return the index of graph to which data set belongs. if data sets is not
	 *         found in that array, -1 is returned
	 */
	public int getGraphIndex(DataReference ds) {
		DataSetInfo dsi = getDataSetInfo(ds);
		if (dsi == null)
			return -1;
		else
			return dsi.getGraphIndex();
	}

	/**
	 * return index of plot within graph in which data set is viewed
	 */
	public int getPlotIndex(DataReference ds) {
		DataSetInfo dsi = getDataSetInfo(ds);
		if (dsi == null)
			return -1;
		else
			return dsi.getPlotIndex();
	}

	/**
	 * returns the x axis position of the data set.
	 */
	public int getXAxisPosition(DataReference ds) {
		DataSetInfo dsi = getDataSetInfo(ds);
		if (dsi == null)
			return -1;
		else
			return dsi.getXAxisPosition();
	}

	/**
	 * returns the y axis position of the data set.
	 */
	public int getYAxisPosition(DataReference ds) {
		DataSetInfo dsi = getDataSetInfo(ds);
		if (dsi == null)
			return -1;
		else
			return dsi.getYAxisPosition();
	}

	/**
	 * @param axisPosition
	 *            an integer specifying the axis location.
	 * @see AxisAttr
	 * @return the axis label for given graph index and axis position
	 */
	public String getAxisLabel(int graphIndex, int plotIndex, int axisPosition) {
		PlotInfo pInfo = getPlotInfo(graphIndex, plotIndex);
		return pInfo.getAxisLabel(axisPosition);
	}

	/**
	 * gets an array of data sets which belong to a certain graph index.
	 */
	public DataReference[] getDataReferences(int graphIndex, int plotIndex) {
		PlotInfo pInfo = getPlotInfo(graphIndex, plotIndex);
		if (pInfo == null)
			return null;
		return pInfo.getDataReferences();
	}

	/**
	 * update information as data sets have been added or removed
	 */
	private void updateInfo() {
		if (_graphInfo.size() > 0) {
			_graphInfo.remove(0, ngraphs - 1);
			ngraphs = 1;
		}
		int setCount = _dataRefs.size();
		for (Enumeration e = _dataRefs.elements(); e.hasMoreElements();) {
			_dataSetInfo.add(addSetInfo((DataReference) e.nextElement()));
		}
	}

	/**
   *
   */
	private DataSetInfo addSetInfo(DataReference ref) {
		DataSetInfo dsi = null;
		if (_graphInfo.size() == 0) {
			GraphInfo info = new GraphInfo(ngraphs, "Graph # " + ngraphs);
			_graphInfo.add(info);
		}
		while (true) {
			try {
				GraphInfo info = (GraphInfo) _graphInfo.back();
				dsi = info.addDataReference(ref);
				break;
			} catch (GraphLimitException e) {
				int index = ngraphs++;
				GraphInfo newInfo = new GraphInfo(index, "Graph # " + index);
				_graphInfo.add(newInfo);
			}
		}
		return dsi;
	}

	/**
   *
   */
	public String toString() {
		StringBuffer buf = new StringBuffer(getClass().getName());
		buf.append("\n").append("Number Of Graphs: ").append(
				getNumberOfGraphs()).append("\n");
		for (int i = 0; i < getNumberOfGraphs(); i++) {
			buf.append(((GraphInfo) _graphInfo.at(i)).toString());
		}
		buf.append("---------").append("\n");
		return buf.toString();
	}

	/**
   *
   */
	public GraphInfo getGraphInfo(int graphIndex) {
		return (GraphInfo) _graphInfo.at(graphIndex);
	}

	/**
   *
   */
	public PlotInfo getPlotInfo(int graphIndex, int plotIndex) {
		GraphInfo gInfo = getGraphInfo(graphIndex);
		return (PlotInfo) gInfo.getPlotInfo(plotIndex);
	}

	/**
   *
   */
	public DataSetInfo getDataSetInfo(DataReference ref) {
		DataSetInfo info = null;
		for (Enumeration e = _dataSetInfo.elements(); e.hasMoreElements();) {
			DataSetInfo dsi = (DataSetInfo) e.nextElement();
			if (ref.equals(dsi.getReference())) {
				info = dsi;
				break;
			}
		}
		return info;
	}
}// end of class GraphBuilder Info
