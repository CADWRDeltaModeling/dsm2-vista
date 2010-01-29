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

import vista.time.Time;
import vista.time.TimeInterval;
import vista.time.TimeWindow;
import COM.objectspace.jgl.Array;
import COM.objectspace.jgl.BinaryPredicate;
import COM.objectspace.jgl.Sorting;

/**
 * This proxy is for merging data references. The time window is the union of
 * the time windows of the given references
 * 
 * @author Nicky Sandhu
 * @version $Id: MergingProxy.java,v 1.1 2003/10/02 20:49:26 redwood Exp $
 */
public class MergingProxy extends DataReference {
	/**
   *
   */
	protected String getProxyName(DataReference[] refs) {
		return refs[0].getName() + " | " + getOperationName();
	}

	/**
   *
   */
	protected String getProxyServerName(DataReference[] refs) {
		return refs[0].getServername();
	}

	/**
   *
   */
	protected String getProxyFileName(DataReference[] refs) {
		return refs[0].getFilename();
	}

	/**
   *
   */
	protected Pathname getProxyPathname(DataReference[] refs) {
		String[] parts = new String[Pathname.MAX_PARTS];
		for (int j = 0; j < refs.length; j++) {
			Pathname path = refs[j].getPathname();
			for (int i = 0; i < parts.length; i++) {
				String part = path.getPart(i);
				if (parts[i] == null)
					parts[i] = part;
				else if (parts[i].indexOf(part) < 0)
					parts[i] += " & " + part;
			}
		}
		parts[Pathname.B_PART] = parts[Pathname.B_PART] + "("
				+ getOperationName() + ")";
		parts[Pathname.E_PART] = getTimeInterval().getIntervalAsString();
		parts[Pathname.F_PART] = parts[Pathname.F_PART] + "("
				+ getOperationName() + ")";
		return Pathname.createPathname(parts);
	}

	/**
	 * returns a union of all the references time window.
	 */
	protected TimeWindow getProxyTimeWindow(DataReference[] refs) {
		Time stime = refs[0].getTimeWindow().getStartTime(), etime = refs[0]
				.getTimeWindow().getEndTime();
		for (int i = 1; i < refs.length; i++) {
			Time stm = refs[i].getTimeWindow().getStartTime();
			Time etm = refs[i].getTimeWindow().getEndTime();
			if (stime.compare(stm) > 0)
				stime = stm;
			if (etime.compare(etm) < 0)
				etime = etm;
		}
		if (DEBUG)
			System.out.println("Proxy time window : " + stime + " - " + etime);
		return refs[0].getTimeWindow().create(stime, etime);
	}

	/**
   *
   */
	protected TimeInterval getProxyTimeInterval(DataReference[] refs) {
		TimeInterval ti = refs[0].getTimeInterval();
		for (int i = 1; i < refs.length; i++) {
			if (ti.compare(refs[i].getTimeInterval()) < 0) {
				ti = refs[i].getTimeInterval();
			}
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
	protected void checkInput(DataReference[] refs) {
		// check for null input
		if (refs == null)
			throw new IllegalArgumentException("References for proxy are null");
		// check for empty input
		if (refs.length == 0)
			throw new IllegalArgumentException("What? no references for proxy!");
		// check for empty input
		for (int i = 0; i < refs.length; i++) {
			if (refs[i] == null)
				continue; // will discard later...
			// throw new
			// IllegalArgumentException("Hmmm...one of the references for proxy is null");
			if (refs[i].getPathname().getPart(Pathname.E_PART).indexOf("IR-") >= 0)
				throw new IllegalArgumentException(
						"Hmmm...one of the references for proxy is irregular");
			if (refs[i].getTimeWindow() == null)
				throw new IllegalArgumentException("Hey! no time window on "
						+ refs[i]);
			if (refs[i].getTimeInterval() == null)
				throw new IllegalArgumentException("Hey! no time interval on "
						+ refs[i]);
		}
		// check on location proximity ?
	}

	/**
   *
   */
	public MergingProxy(DataReference[] refs) {
		checkInput(refs);
		// filter out null references
		_refs = new DataReference[refs.length];
		int k = 0;
		for (int i = 0; i < refs.length; i++) {
			if (refs[i] == null)
				continue;
			_refs[k] = refs[i];
			k++;
		}
		if (k == 0)
			throw new IllegalArgumentException("All data references are null");
		refs = new DataReference[k];
		for (int i = 0; i < k; i++) {
			refs[i] = _refs[i];
		}
		// choose max interval
		super.setTimeInterval(getProxyTimeInterval(refs));
		// set state variables
		super.setName(getProxyName(refs));
		super.setServername(getProxyServerName(refs));
		super.setFilename(getProxyFileName(refs));
		super.setPathname(getProxyPathname(refs));
		super.setTimeWindow(getProxyTimeWindow(refs));
		// handle to references being merged.
		_refs = refs;
		if (TimeSeriesMath.DUMB_PATCH) {
			// choose priority ordering from greatest to least
			sort(_refs);
			// period average other intervals to max interval
			replaceWithPeriodAverages(_refs);
		}
	}

	/**
   *
   */
	protected void setTimeWindow(TimeWindow tw) {
		super.setTimeWindow(tw);
		int index = 0;
		for (int i = 0; i < _refs.length; i++) {
			DataReference ref = DataReference.create(_refs[i], getTimeWindow());
			if (ref == null)
				continue;
			_refs[index] = ref;
			index++;
		}
		DataReference[] refs = new DataReference[index];
		System.arraycopy(_refs, 0, refs, 0, index);
		_refs = refs;
	}

	/**
	 * creates a clone of itself and returns the reference to it. This is used
	 * in creating a clone of itself
	 */
	public DataReference createClone() {
		DataReference ref = new MergingProxy(this._refs);
		return ref;
	}

	private DataReference[] _refs;

	/**
   *
   */
	public void setFilter(ElementFilter f) {
		_filter = f;
	}

	/**
   *
   */
	private ElementFilter _filter = Constants.DEFAULT_FLAG_FILTER;

	/**
   *
   */
	public void reloadData() {
		_dataSet = null;
		if (_refs != null) {
			for (int i = 0; i < _refs.length; i++) {
				if (_refs[i] != null)
					_refs[i].reloadData();
			}
		}
	}

	/**
   *
   */
	public DataSet getData() throws DataRetrievalException {
		if (_dataSet == null) {
			// merge the references
			_dataSet = getMergedDataSet(_refs);
		}
		return _dataSet;
	}

	private transient DataSet _dataSet = null;

	/**
   *
   */
	private DataSet getMergedDataSet(DataReference[] refs)
			throws DataRetrievalException {
		Time stime = getTimeWindow().getStartTime();
		stime = stime.create(getTimeWindow().getStartTime());
		Time etime = getTimeWindow().getEndTime();
		TimeInterval ti = getTimeInterval();
		// create values array.
		if (DEBUG) {
			System.out.print("Merging references: ");
			for (int i = 0; i < refs.length; i++) {
				System.out.print(refs[i]);
			}
			System.out.println();
		}

		boolean flagged = false;
		int nvals = (int) (stime.getExactNumberOfIntervalsTo(etime, ti) + 1);
		if (DEBUG)
			System.out.println("Number of values: = " + nvals);
		double yArray[] = new double[nvals];
		int flagArray[] = null;
		if (flagged)
			flagArray = new int[nvals];
		DataSetElement dvals[] = new DataSetElement[refs.length];
		// create data for all references
		DataSetIterator[] dsi = new DataSetIterator[refs.length];
		int numberNull = 0;
		DataSetAttr attr = null;
		for (int i = 0; i < refs.length; i++) {
			if (refs[i].getData() != null) {
				dsi[i] = refs[i].getData().getIterator();
				attr = refs[i].getData().getAttributes();
			} else {
				dsi[i] = null;
				numberNull++;
			}
		}
		if (numberNull == refs.length) {
			return null;
		}
		// loop over every
		int yIndex = 0;
		for (Time tm = stime; tm.compare(etime) <= 0; tm.incrementBy(ti)) {
			long xval = tm.getTimeInMinutes();
			updateArray(xval, dvals, dsi);
			yArray[yIndex] = getMergedValue(dvals).getY();
			if (flagged)
				flagArray[yIndex] = getMergedValue(dvals).getFlag();
			if (DEBUG)
				System.out.println("Time: " + tm + " & value = "
						+ yArray[yIndex]);
			yIndex++;
		}
		stime = getTimeWindow().getStartTime();
		attr = new DataSetAttr(attr.getType(), attr.getXUnits(), attr
				.getYUnits(), attr.getXType(), attr.getYType());
		return new RegularTimeSeries(getName(), stime, ti, yArray, flagArray,
				attr);
	}

	private static transient DataSetElement _missingElement = new DefaultDataSetElement();
	static {
		_missingElement.setY(Constants.MISSING_VALUE);
	}

	/**
	 * simply return the first good value.
	 */
	protected DataSetElement getMergedValue(DataSetElement[] dvals) {
		for (int i = 0; i < dvals.length; i++) {
			if (dvals[i] != null && _filter.isAcceptable(dvals[i]))
				return dvals[i];
		}
		return _missingElement;
	}

	/**
   *
   */
	private void updateArray(long xval, DataSetElement[] dvals,
			DataSetIterator[] dsis) {
		DataSetElement dse = null;
		for (int i = 0; i < dsis.length; i++) {
			DataSetIterator dsi = dsis[i];
			if (dsi == null) {
				dvals[i] = _missingElement;
				continue;
			}
			dse = dsi.getElement();
			if (dse == null) {
				dvals[i] = _missingElement;
				continue;
			}
			long x = Math.round(dse.getX());
			if (x > xval) {
				dvals[i] = _missingElement;
				continue;
			} else if (x == xval) {
				dvals[i] = dse;
				if (!dsi.atEnd())
					dsi.advance();
			} else {
				if (dsi.atStart()) {
					while (true) {
						if (!dsi.atEnd())
							dsi.advance();
						if (Math.round(dsi.getElement().getX()) == xval) {
							dvals[i] = dsi.getElement();
							if (!dsi.atEnd())
								dsi.advance();
							break;
						}
						if (dsi.atEnd()) {
							dvals[i] = _missingElement;
							break;
						}
					}
					continue;
				}
				if (!dsi.atEnd()) {
					if (DEBUG) {
						System.out.println("X val requested: " + xval);
						System.out.println("Val @: " + dsi.getElement());
						System.out.println("DataSet # : " + i);
					}
					throw new RuntimeException(
							"Iterators in merged data set are not synchronized");
				} else {
					dvals[i] = _missingElement;
				}
			}
		}
	}

	/**
   *
   */
	private void replaceWithPeriodAverages(DataReference[] refs) {
		TimeInterval ti = getTimeInterval();
		for (int i = 0; i < refs.length; i++) {
			if (ti.compare(refs[i].getTimeInterval()) == 0)
				continue;
			refs[i] = ProxyFactory.createPeriodOperationProxy(
					ProxyFactory.PERIOD_AVERAGE, refs[i], ti);
		}
	}

	/**
   *
   */
	protected void sort(DataReference[] refs) {
		Array a = new Array(refs);
		Sorting.sort(a, new MergingOrder());
		DataReference[] nrefs = new DataReference[a.size()];
		a.copyTo(nrefs);
		System.arraycopy(nrefs, 0, refs, 0, refs.length);
		if (DEBUG) {
			System.out.println("New order is ");
			for (int i = 0; i < nrefs.length; i++) {
				System.out.println("Reference " + i + " = " + nrefs[i]);
			}
		}
	}

	/**
   *
   */
	private class MergingOrder implements BinaryPredicate {
		/**
		 * return true if obj1 comes before obj2 in sort order
		 */
		public boolean execute(Object obj1, Object obj2) {
			if (!(obj1 instanceof DataReference))
				return false;
			if (!(obj2 instanceof DataReference))
				return false;
			DataReference ref1 = (DataReference) obj1;
			DataReference ref2 = (DataReference) obj2;
			if (ref1.getTimeInterval().compare(ref2.getTimeInterval()) < 0)
				return true;
			else
				return false;
		}
	}

	/**
   *
   */
	private static final boolean DEBUG = false;

	/**
   *
   */
	public String toString() {
		StringBuffer buf = new StringBuffer(100 + 150 * _refs.length);
		buf.append("Regular Time Series Merging Proxy: ").append("\n");
		for (int i = 0; i < _refs.length; i++) {
			buf.append("Reference #:").append(i).append(" -> ")
					.append(_refs[i]).append("\n");
		}
		return buf.toString();
	}
}
