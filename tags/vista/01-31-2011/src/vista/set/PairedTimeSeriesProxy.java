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
package vista.set;

import vista.time.TimeInterval;
import vista.time.TimeWindow;

/**
 * 
 * 
 * @author Nicky Sandhu
 * @version $Id: PairedTimeSeriesProxy.java,v 1.1 2003/10/02 20:49:28 redwood
 *          Exp $
 */
public class PairedTimeSeriesProxy extends DataReference {
	/**
   *
   */
	protected String getProxyName(DataReference refx, DataReference refy) {
		return refx.getName() + "," + refy.getName() + " | "
				+ getOperationName();
	}

	/**
   *
   */
	protected String getProxyServerName(DataReference refx, DataReference refy) {
		return "";
	}

	/**
   *
   */
	protected String getProxyFileName(DataReference refx, DataReference refy) {
		return "";
	}

	/**
   *
   */
	protected Pathname getProxyPathname(DataReference refx, DataReference refy) {
		String[] parts = new String[Pathname.MAX_PARTS];
		Pathname pathx = refx.getPathname();
		Pathname pathy = refy.getPathname();
		//  
		for (int i = 0; i < parts.length; i++) {
			if (pathx.getPart(i).equals(pathy.getPart(i)))
				parts[i] = pathx.getPart(i);
			else
				parts[i] = pathx.getPart(i) + "," + pathy.getPart(i);
		}
		parts[Pathname.F_PART] = parts[Pathname.F_PART] + "("
				+ getOperationName() + ")";
		return Pathname.createPathname(parts);
	}

	/**
	 * returns a union of all the references time window.
	 */
	protected TimeWindow getProxyTimeWindow(DataReference refx,
			DataReference refy) {
		return refx.getTimeWindow().intersection(refy.getTimeWindow());
	}

	/**
   *
   */
	protected TimeInterval getProxyTimeInterval(DataReference refx,
			DataReference refy) {
		TimeInterval ti = refx.getTimeInterval();
		if (ti.compare(refy.getTimeInterval()) < 0) {
			ti = refy.getTimeInterval();
		}
		if (DEBUG)
			System.out.println("Proxy time interval: " + ti);
		return ti.create(ti);
	}

	/**
   *
   */
	protected String getOperationName() {
		return "MERGER";
	}

	/**
   *
   */
	private void setReferences(DataReference refx, DataReference refy) {
		TimeWindow tw = getTimeWindow();
		// uniformitized time window
		if (refx.getTimeWindow().equals(tw))
			_refx = refx;
		else
			_refx = DataReference.create(refx, tw);
		if (refy.getTimeWindow().equals(tw))
			_refy = refy;
		else
			_refy = DataReference.create(refy, tw);
		// uniformitize time interval
		TimeInterval ti = getTimeInterval();
		if (ti.compare(_refx.getTimeInterval()) != 0)
			_refx = ProxyFactory.createPeriodOperationProxy(
					ProxyFactory.PERIOD_AVERAGE, _refx, ti);
		if (ti.compare(_refy.getTimeInterval()) != 0)
			_refy = ProxyFactory.createPeriodOperationProxy(
					ProxyFactory.PERIOD_AVERAGE, _refy, ti);
	}

	/**
   *
   */
	public PairedTimeSeriesProxy(DataReference refx, DataReference refy) {
		// check nput
		checkInput(refx, refy);
		// set server, file, pathname, time window, time interval
		// choose max interval
		super.setTimeInterval(getProxyTimeInterval(refx, refy));
		super.setName(getProxyName(refx, refy));
		super.setServername(getProxyServerName(refx, refy));
		super.setFilename(getProxyFileName(refx, refy));
		super.setPathname(getProxyPathname(refx, refy));
		super.setTimeWindow(getProxyTimeWindow(refx, refy));
		// create references with correct time window and time interval
		setReferences(refx, refy);
	}

	/**
   *
   */
	protected void setTimeWindow(TimeWindow tw) {
		super.setTimeWindow(tw);
		_refx = DataReference.create(_refx, getTimeWindow());
		_refy = DataReference.create(_refy, getTimeWindow());
	}

	/**
	 * creates a clone of itself and returns the reference to it. This is used
	 * in creating a clone of itself
	 */
	public DataReference createClone() {
		DataReference ref = new PairedTimeSeriesProxy(this._refx, this._refy);
		return ref;
	}

	/**
   *
   */
	public void reloadData() {
		_dataSet = null;
		_refx.reloadData();
		_refy.reloadData();
	}

	/**
   *
   */
	public DataSet getData() throws DataRetrievalException {
		if (_dataSet == null) {
			DataSet dsx = _refx.getData();
			DataSet dsy = _refy.getData();
			DataSetIterator dsix = dsx.getIterator(), dsiy = dsy.getIterator();
			double[] xArray = new double[dsx.size()];
			double[] yArray = new double[dsy.size()];
			int index = 0;
			while (true) {
				DataSetElement dsex = dsix.getElement();
				DataSetElement dsey = dsiy.getElement();
				//
				if (_filter.isAcceptable(dsex) && _filter.isAcceptable(dsey)) {
					xArray[index] = dsex.getY();
					yArray[index] = dsey.getY();
					index++;
				}
				dsix.advance();
				dsiy.advance();
				//
				if (dsix.atEnd())
					break;
				if (dsiy.atEnd())
					break;
			}
			if (index == 0)
				throw new DataRetrievalException(
						"Atleast one set has no values");
			double[] tmpArray = new double[index];
			System.arraycopy(xArray, 0, tmpArray, 0, tmpArray.length);
			xArray = tmpArray;
			tmpArray = new double[index];
			System.arraycopy(yArray, 0, tmpArray, 0, tmpArray.length);
			yArray = tmpArray;

			DataSetAttr attr = new DataSetAttr(DataType.PAIRED, dsx
					.getAttributes().getYUnits(), dsy.getAttributes()
					.getYUnits(), dsx.getAttributes().getYType(), dsy
					.getAttributes().getYType());
			_dataSet = new DefaultDataSet("Paired: (" + dsx.getName() + ","
					+ dsx.getName() + ")", xArray, yArray, null, attr);
		}
		return _dataSet;
	}

	/**
   *
   */
	protected void checkInput(DataReference refx, DataReference refy) {
		// check for non-null
		if (refx == null || refy == null)
			throw new IllegalArgumentException("References for proxy are null");
		// check for being time series
		if (refx.getPathname().getPart(Pathname.E_PART).indexOf("IR-") >= 0)
			throw new IllegalArgumentException("Oops... " + refx
					+ " is irregular.");
		if (refy.getPathname().getPart(Pathname.E_PART).indexOf("IR-") >= 0)
			throw new IllegalArgumentException("Oops... " + refy
					+ " is irregular.");
		// check for non-null common time window
		if (refx.getTimeWindow() == null)
			throw new IllegalArgumentException("Hey! no time window on " + refx);
		if (refx.getTimeInterval() == null)
			throw new IllegalArgumentException("Hey! no time interval on "
					+ refx);
		if (refy.getTimeWindow() == null)
			throw new IllegalArgumentException("Hey! no time window on " + refy);
		if (refy.getTimeInterval() == null)
			throw new IllegalArgumentException("Hey! no time interval on "
					+ refy);
		// check for compatible interval or ability to do so
		if (!(refx.getTimeWindow().intersects(refy.getTimeWindow()))) {
			throw new IllegalArgumentException(refx + " & " + refy
					+ " have no common time window");
		}
	}

	/**
   *
   */
	private transient DataSet _dataSet;
	private DataReference _refx, _refy;
	private ElementFilter _filter = Constants.DEFAULT_FILTER;
	private static final boolean DEBUG = false;
}
