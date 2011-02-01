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

/**
 * Filters the given iterator's values using a filter function.
 * 
 * @author Nicky Sandhu
 * @version $Id: MultiPairedIterator.java,v 1.3 2000/05/15 19:39:10 nsandhu Exp
 *          $
 */
public class MultiPairedIterator implements DataSetIterator {
	private DataSetIterator[] _dsis;
	private double[] _y;
	private TimeTuple _dse;
	private Tuple _dse2;

	/**
	 * Filters the given iterator through the given filter.
	 */
	public MultiPairedIterator(TimeSeries[] ts, ElementFilter f) {
		DataSetIterator[] iterators = new DataSetIterator[ts.length];
		if (f == null) {
			_dsis = iterators;
		} else {
			for (int i = 0; i < ts.length; i++)
				iterators[i] = new ElementFilterIterator(ts[i].getIterator(), f);
			_dsis = iterators;
		}
		_y = new double[iterators.length];
		int[] flags = new int[iterators.length];
		_dse = new TimeTuple(0, _y, flags);
		_dse2 = new Tuple(_y);
		resetIterator();
	}

	/**
	 * Filters the given iterator through the given filter.
	 */
	public MultiPairedIterator(TimeSeries[] ts) {
		DataSetIterator[] iterators = new DataSetIterator[ts.length];
		for (int i = 0; i < ts.length; i++)
			iterators[i] = ts[i].getIterator();
		_dsis = iterators;
		_y = new double[iterators.length];
		int[] flags = new int[iterators.length];
		_dse = new TimeTuple(0, _y, flags);
		_dse2 = new Tuple(_y);
		resetIterator();
	}

	/**
	 * Resets the iterator to the beginning of data
	 */
	public void resetIterator() {
		for (int i = 0; i < _dsis.length; i++) {
			_dsis[i].resetIterator();
		}
		_index = 0;
	}

	/**
	 * gets the element at the current location
	 */
	public DataSetElement getElement() {
		long currentX = getMinX();
		for (int i = 0; i < _dsis.length; i++) {
			DataSetIterator dsi = _dsis[i];
			if (dsi.atEnd()) {
				_y[i] = Float.NaN;
			} else {
				DataSetElement dse = dsi.getElement();
				if (Math.round(dse.getX()) == currentX) {
					_y[i] = dse.getY();
				} else {
					_y[i] = Float.NaN;
				}
			}
		}
		_dse.setX(currentX);
		_dse.setY(_y); // not really necessary as dse contains the same array
		_dse2.setX(_y);
		return _dse2;
		// experimental
		// return new TimeTuple(_currentX, _y);
	}

	/**
	 * puts the element at the current location
	 */
	public void putElement(DataSetElement e) {
		if (e.getDimension() != _dse.getDimension())
			throw new IllegalArgumentException("Dimensions mismatch");
		long currentX = getMinX();
		for (int i = 0; i < _dsis.length; i++) {
			DataSetIterator dsi = _dsis[i];
			if (dsi.atEnd()) {
			} else {
				DataSetElement dse = dsi.getElement();
				if (Math.round(dse.getX()) == currentX) {
					dse.setY(e.getX(i + 1));
					dsi.putElement(dse);
				} else {
				}
			}
		}
	}

	/**
   *
   */
	private void checkIndex(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("Index is negative");
	}

	/**
   *
   */
	public void positionAtIndex(int index) {
		checkIndex(index);
		int currentIndex = getIndex();
		int diff = index - currentIndex;
		if (diff > 0) {
			for (int i = 0; i < diff; i++)
				advance();
		} else if (diff < 0) {
			diff = -diff;
			for (int i = 0; i < diff; i++)
				retreat();
		}
	}

	/**
	 * Advance by one.
	 */
	public void advance() {
		if (atEnd())
			return;
		// get the smallest time value
		long x = getMinX();
		// for all the iterators with the smallest time value advance them.
		for (int i = 0; i < _dsis.length; i++) {
			DataSetIterator dsi = _dsis[i];
			if (!dsi.atEnd()) {
				if (x == Math.round(dsi.getElement().getX()))
					dsi.advance();
			}
		}
		_index++;
	}

	/**
   *
   */
	public long getMinX() {
		double x = Float.MAX_VALUE;
		for (int i = 0; i < _dsis.length; i++) {
			DataSetIterator dsi = _dsis[i];
			if (!dsi.atEnd()) {
				x = Math.min(dsi.getElement().getX(), x);
			}
		}
		return Math.round(x);
	}

	/**
	 * Retreat by one
	 */
	public void retreat() {
		if (atStart())
			return;
		// get the smallest time value
		long x = getMinX();
		// for all the iterators with the smallest time value advance them.
		for (int i = 0; i < _dsis.length; i++) {
			DataSetIterator dsi = _dsis[i];
			if (!dsi.atStart()) {
				if (dsi.atEnd())
					dsi.retreat();
				if (x == Math.round(dsi.getElement().getX()))
					dsi.retreat();
			}
		}
		_index--;
	}

	/**
	 * true if the iterator has skipped a few elements of the underlying data
	 * set
	 */
	public int hasSkipped() {
		int skipped = Integer.MAX_VALUE;
		long x = getMinX();
		for (int i = 0; i < _dsis.length; i++) {
			DataSetIterator dsi = _dsis[i];
			if (!dsi.atEnd()) {
				skipped = Math.min(skipped, dsi.hasSkipped());
			}
		}
		return skipped;
	}

	/**
	 * Gets the current index for the iterator. This is the index of this
	 * iterator on the underlying iterator's index of that of the data set
	 */
	public int getIndex() {
		return _index;
	}

	/**
   *
   */
	public int getUnderlyingIndex() {
		return _index;
	}

	/**
	 * if iterator is at start of data
	 */
	public boolean atStart() {
		for (int i = 0; i < _dsis.length; i++) {
			if (!_dsis[i].atStart())
				return false;
		}
		return true;
	}

	/**
	 * if iterator is at end of data.
	 */
	public boolean atEnd() {
		for (int i = 0; i < _dsis.length; i++) {
			if (!_dsis[i].atEnd())
				return false;
		}
		return true;
	}

	/**
	 * The maximum of x and y range encapsulated as a data set element.
	 */
	public DataSetElement getMaximum() {
		double xmax = -Float.MAX_VALUE;
		double ymax = -Float.MAX_VALUE;
		int prevIndex = this.getIndex();
		DataSetIterator dsi = this;
		dsi.resetIterator();
		_maximum = dsi.getElement().createClone();
		double[] maxs = new double[_maximum.getDimension()];
		while (!dsi.atEnd()) {
			DataSetElement dse = dsi.getElement();
			for (int i = 0; i < maxs.length; i++)
				maxs[i] = Math.max(maxs[i], dse.getX(i));
			dsi.advance();
		}
		this.positionAtIndex(prevIndex);
		for (int i = 0; i < maxs.length; i++)
			_maximum.setX(i, maxs[i]);
		return _maximum;
	}

	/**
	 * The minimum of x and y range encapsulated as a data set element.
	 */
	public DataSetElement getMinimum() {
		double xmin = Float.MAX_VALUE;
		double ymin = Float.MAX_VALUE;
		int prevIndex = this.getIndex();
		DataSetIterator dsi = this;
		dsi.resetIterator();
		_minimum = dsi.getElement().createClone();
		double[] mins = new double[_minimum.getDimension()];
		while (!dsi.atEnd()) {
			DataSetElement dse = dsi.getElement();
			for (int i = 0; i < mins.length; i++)
				mins[i] = Math.min(mins[i], dse.getX(i));
			dsi.advance();
		}
		this.positionAtIndex(prevIndex);
		for (int i = 0; i < mins.length; i++)
			_minimum.setX(i, mins[i]);
		return _minimum;
	}

	/**
	 * The current index on the iterator
	 */
	private int _index;
	/**
	 * the maximum and minimum values
	 */
	private DataSetElement _maximum, _minimum;
	/**
   *
   */
	private static final boolean DEBUG = false;
}
