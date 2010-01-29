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

import vista.set.DataReference;
import COM.objectspace.jgl.Array;

/**
 * 
 * 
 * @author Nicky Sandhu
 * @version $Id: GraphInfo.java,v 1.1 2003/10/02 20:48:30 redwood Exp $
 */
class GraphInfo {
	private int PLOTS_PER_GRAPH = 2;
	private int index;
	private String title;
	private Array plots;

	/**
   *
   */
	public GraphInfo(int index, String title) {
		plots = new Array();
	}

	/**
   *
   */
	public int getNumberOfPlots() {
		return plots.size();
	}

	/**
   *
   */
	public PlotInfo getPlotInfo(int index) {
		return (PlotInfo) plots.at(index);
	}

	/**
   *
   */
	public DataSetInfo addDataReference(DataReference ref)
			throws GraphLimitException {

		PlotInfo pInfo = null;
		DataSetInfo dsi = null;

		boolean setAdded = false;

		for (Enumeration e = plots.elements(); e.hasMoreElements();) {
			pInfo = (PlotInfo) e.nextElement();
			try {
				dsi = pInfo.addDataReference(ref);
				setAdded = true;
				break;
			} catch (PlotLimitException ple) {
			}
		}
		if (!setAdded) {
			if (limitReached())
				throw new GraphLimitException();
			plots.add(pInfo = new PlotInfo());
			pInfo.setIndex(plots.size());
			pInfo.setTitle("Plot # " + plots.size());
			pInfo.setGraphIndex(index);
			try {
				dsi = pInfo.addDataReference(ref);
			} catch (PlotLimitException ple) {
				throw new GraphLimitException();
			}
		}
		return dsi;
	}

	/**
   *
   */
	public boolean limitReached() {
		return (plots.size() >= PLOTS_PER_GRAPH);
	}

	/**
   *
   */
	public String toString() {
		StringBuffer buf = new StringBuffer(25);
		buf.append("Graph # ").append(index).append("\n");
		buf.append("Graph title ").append(index).append("\n");
		buf.append("# of plots ").append(plots.size()).append("\n");
		for (int i = 0; i < plots.size(); i++)
			buf.append(((PlotInfo) plots.at(i)).toString());
		return buf.toString();
	}
}
