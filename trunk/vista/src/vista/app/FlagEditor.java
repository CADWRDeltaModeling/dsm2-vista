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

import java.awt.Frame;

import javax.swing.JOptionPane;

import vista.db.dss.DSSUtil;
import vista.graph.Curve;
import vista.graph.GECanvas;
import vista.graph.Graph;
import vista.graph.RangeActor;
import vista.graph.RangeSelected;
import vista.graph.RangeSelector;
import vista.gui.SendMailDialog;
import vista.set.Constants;
import vista.set.DataReference;
import vista.set.DataRetrievalException;
import vista.set.DataSet;
import vista.set.DataSetElement;
import vista.set.DataSetIterator;
import vista.set.FlagUtils;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeInterval;

/**
 * an instance of this class starts editing, pops up query for range selection,
 * pops up query for flag id to set range to and then sets the range to that
 * flag. If range is already flagged it requests for override vs preserve
 * option.
 * 
 * @author Nicky Sandhu
 * @version $Id: FlagEditor.java,v 1.1 2003/10/02 20:48:30 redwood Exp $
 */
public class FlagEditor implements RangeActor{
	private Curve curve;
	private GECanvas gC;
	private RangeSelected rs;
	private FlagChoiceFrame fcf;

	/**
	 * Edits the flags for the given curve
	 */
	public FlagEditor(GECanvas gC, Curve curve, boolean boxSelection) {
		this.gC = gC;
		this.curve = curve;
		if (boxSelection){
			rs = new RangeSelector(gC, curve, this);
		} else {
			rs = new XRangeSelector(this, gC, curve);
		}
	}

	@Override
	public void selectedRange(int xmin, int xmax, int ymin, int ymax) {
		fcf = new FlagChoiceFrame(JOptionPane.getFrameForComponent(gC), this);
	}
	/**
   *
   */
	public void emailRangeTo() {
		String rcp = MainProperties.getProperty("email.maintainers");
		String subject = "Data Quality Flags [VISTA]";
		DataReference ref = (DataReference) curve.getModel()
				.getReferenceObject();
		TimeFactory tf = DSSUtil.getTimeFactory();
		Time time = tf.getTimeInstance();
		TimeInterval ti = DSSUtil.createTimeInterval(ref.getPathname());
		String msg = "Data Reference:\n" + "Server: " + ref.getServername()
				+ "\n" + "Filename: " + ref.getFilename() + "\n" + "Pathname: "
				+ ref.getPathname() + "\n" + "Data Start Time: "
				+ time.create(Math.round(rs.getXRangeMin())).floor(ti) + "\n"
				+ "Data End Time: "
				+ time.create(Math.round(rs.getXRangeMax())).ceiling(ti) + "\n";
		Frame f = JOptionPane.getFrameForComponent(gC);
		new SendMailDialog(f, rcp, subject, msg);
	}

	/**
	 * This method is called by the flag choice dialog once the user is done
	 * selecting the flag to which to set the data. Then given the minimum and
	 * maximum range, the data set and the flag Id, the data set is changed.
	 */
	public void flagRangeTo(int flagId) {
		gC.paint(gC.getGraphics());
		DataSet ds = null;
		try {
			ds = ((DataReference) curve.getModel().getReferenceObject())
					.getData();
		} catch (DataRetrievalException dre) {
			throw new IllegalArgumentException(dre.getMessage());
		}
		DataSetIterator iterator = ds.getIterator();
		boolean overrideDecided = false;
		boolean override = true;
		double minx = rs.getXRangeMin();
		double maxx = rs.getXRangeMax();
		double miny = rs.getYRangeMin();
		double maxy = rs.getYRangeMax();
		int userId = DSSUtil.getUserId();
		for (iterator.resetIterator(); !iterator.atEnd(); iterator.advance()) {
			DataSetElement dse = iterator.getElement();
			double x = dse.getX();
			double y = dse.getY();
			if ((x >= minx && x <= maxx) && (y >= miny && y <= maxy)) {
				
				if (!overrideDecided && FlagUtils.isScreened(dse)) {
					overrideDecided = true;
					// check one of override vs preserve with modal dialog
					Object[] choices = { "Override flags", "Preserve flags" };
					Object choice = JOptionPane.showInputDialog(gC,
							"Override vs Preserve Flags",
							"Flag preservation choice",
							JOptionPane.QUESTION_MESSAGE, null, choices,
							choices[1]);
					if (choice.equals(choices[0]))
						override = true;
					else
						override = false;
				}
				//
				if (FlagUtils.isScreened(dse) && !override) {
					// don't do anything...
				} else {
					if (y == Constants.MISSING || y == Constants.MISSING_VALUE
							|| y == Constants.MISSING_RECORD){
						FlagUtils.setQualityFlag(dse, FlagUtils.MISSING_FLAG,
								userId);
					}else{
						FlagUtils.setQualityFlag(dse, flagId, userId);
					}
					iterator.putElement(dse);
				}
			}
		}
		Graph graph = (Graph) gC.getGraphicElement();
		AppUtils.setCurveFilter(graph, AppUtils.getCurrentCurveFilter());
		gC.redoNextPaint();
		gC.paint(gC.getGraphics());
		// curve.dataSetChanged();
		// table.dataSetChanged();
	}

	/**
   *
   */
	public void doneChanges() {
		Graph graph = (Graph) gC.getGraphicElement();
		AppUtils.setCurveFilter(graph, AppUtils.getCurrentCurveFilter());
		gC.redoNextPaint();
		gC.paint(gC.getGraphics());
	}

}
