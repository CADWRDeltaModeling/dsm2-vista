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

import java.io.Serializable;

import vista.time.TimeFactory;
import vista.time.TimeInterval;
import vista.time.TimeWindow;

/**
 * Contains information for referencing a certain data set uniquely. This
 * specifies the name of the server, the filename, the pathname and the time
 * window of the data. This object is immutable.
 * 
 * @author Nicky Sandhu
 * @version $Id: DataReference.java,v 1.1 2003/10/02 20:49:19 redwood Exp $
 */
public abstract class DataReference implements Comparable, Named, Serializable {
	/**
	 * reloads data
	 */
	public abstract void reloadData();

	/**
	 * Retrieves data from the data base if data is null. If retrieval fails it
	 * throws a RuntimeException.
	 * 
	 * @return reference to the initialized data set.
	 */
	public abstract DataSet getData();

	/**
	 * create a clone of itself
	 */
	protected abstract DataReference createClone();

	/**
	 * true if object is referencing the same pathname in the same file
	 */
	public boolean equals(Object obj) {
		return (obj != null) && (obj instanceof DataReference)
				&& (isSameAs((DataReference) obj));
	}

	/**
	 * checks equivalency of two data references.
	 */
	public boolean isSameAs(DataReference ref) {
		return (ref != null)
				&& ((ref.getTimeWindow() == null && getTimeWindow() == null) || (ref
						.getTimeWindow() != null && ref.getTimeWindow().equals(
						getTimeWindow())))
				&& ((ref.getTimeInterval() == null && getTimeInterval() == null) || (ref
						.getTimeInterval() != null && ref.getTimeInterval()
						.equals(getTimeInterval())))
				&& (ref.getFilename().equals(getFilename()))
				&& (ref.getServername().equals(getServername()))
				&& (ref.getPathname().equals(getPathname()));
	}
	
	@Override
	public int compareTo(Object o) {
		if (!(o instanceof DataReference)){
			return 1;
		}
		DataReference other = (DataReference) o;
		if (this.getName() == null){
			return -1;
		}
		if (other.getName() == null){
			return 1;
		}
		int compareTo =  this.getName().compareTo(other.getName());
		if (compareTo==0){
			System.out.println("compareTo == 0 :: this.getName(): "+this.getName()+" & other.getName(): "+other.getName());
		}
		return compareTo;
	}


	/**
	 * creates a data reference similar to the given reference in all respects
	 * except that it has the given time window.
	 * 
	 * @return a new data reference with the time window or null if the
	 *         intersection of time window with data reference's time window is
	 *         null.
	 */
	public static DataReference create(DataReference ref, TimeWindow tw) {
		if (tw == null)
			return null; // a null time window
		if (ref.getTimeInterval() == null)
			return null; // a non-time series reference
		TimeWindow newTW = null;
		// if irregular don't round window
		if (ref.getPathname().getPart(Pathname.E_PART).indexOf("IR-") >= 0) {
			newTW = ref.getTimeWindow().intersection(tw);
		} else { // round of regular window to ceiling / floor (expanding time
					// window)
			newTW = ref.getTimeWindow().intersection(
					TimeFactory.createRoundedTimeWindow(tw, ref
							.getTimeInterval()));
		}
		if (newTW == null)
			return null;
		DataReference newRef = ref.createClone();
		newRef.setTimeWindow(newTW);
		return newRef;
	}

	/**
	 * creates a data reference similar to the given reference in all respects
	 * except that it has the given time window. This method does not check for
	 * intersection with current time window.
	 */
	public static DataReference createExpanded(DataReference ref, TimeWindow tw) {
		if (tw == null)
			return null; // a null time window
		if (ref.getTimeInterval() == null)
			return null; // a non-time series reference
		TimeWindow newTW = null;
		// if irregular don't round window
		if (ref.getPathname().getPart(Pathname.E_PART).indexOf("IR-") >= 0) {
			newTW = tw;
		} else { // round of regular window to ceiling / floor (expanding time
					// window)
			newTW = TimeFactory.createRoundedTimeWindow(tw, ref
					.getTimeInterval());
		}
		if (newTW == null)
			return null;
		DataReference newRef = ref.createClone();
		newRef.setTimeWindow(newTW);
		return newRef;
	}

	/**
	 * returns a copy of the data reference
	 */
	public static DataReference create(DataReference ref) {
		return ref.createClone();
	}

	/**
	 * returns a copy of itself.
	 */
	public Object clone() {
		return this.createClone();
	}

	/**
	 * sets the name for this reference
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * gets the name for this reference along with associated pathname.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * gets the server name for this reference
	 */
	public String getServername() {
		return _servername;
	}

	/**
	 * gets the server name for this reference
	 */
	protected void setServername(String servername) {
		_servername = servername;
	}

	/**
	 * gets the filename for this reference
	 */
	public String getFilename() {
		return _filename;
	}

	/**
	 * gets the filename for this reference
	 */
	public void setFilename(String filename) {
		_filename = filename;
	}

	/**
	 * gets the pathname for this reference
	 */
	public Pathname getPathname() {
		return _pathname;
	}

	/**
	 * gets the pathname for this reference
	 */
	public void setPathname(Pathname pathname) {
		_pathname = pathname;
	}

	/**
	 * gets the time window for this reference
	 */
	public TimeWindow getTimeWindow() {
		return _timeWindow;
	}

	/**
	 * gets the time window for this reference
	 */
	protected void setTimeWindow(TimeWindow tw) {
		if (tw == null)
			return;
		TimeWindow ntw = TimeFactory.createRoundedTimeWindow(tw,
				getTimeInterval());
		_timeWindow = ntw;
	}

	/**
	 * This is the time interval for a regular time series.
	 * 
	 * @return TimeInterval object representing the interval between two
	 *         consecutive times in a regular time series or null if this is not
	 *         a time series
	 */
	public TimeInterval getTimeInterval() {
		return _ti;
	}

	/**
	 * sets the time interval for this reference
	 */
	protected void setTimeInterval(TimeInterval ti) {
		_ti = ti;
	}

	/**
	 * This represents the location at which the measurement was taken.
	 */
	public Location getLocation() {
		return _location;
	}

	/**
	 * set the location of this data reference
	 */
	protected void setLocation(Location l) {
		_location = l;
	}

	/**
   *
   */
	public DataReference __add__(DataReference tids) {
		return (DataReference) DataReferenceMath.vectorOperation(this, tids,
				DataReferenceMath.ADD);
	}

	/**
   *
   */
	public DataReference __sub__(DataReference tids) {
		return (DataReference) DataReferenceMath.vectorOperation(this, tids,
				DataReferenceMath.SUB);
	}

	/**
   *
   */
	public DataReference __mul__(DataReference tids) {
		return (DataReference) DataReferenceMath.vectorOperation(this, tids,
				DataReferenceMath.MUL);
	}

	/**
   *
   */
	public DataReference __div__(DataReference tids) {
		return (DataReference) DataReferenceMath.vectorOperation(this, tids,
				DataReferenceMath.DIV);
	}

	/**
   *
   */
	public DataReference __add__(double d) {
		return (DataReference) DataReferenceMath.scalarOperation(this, d,
				DataReferenceMath.ADD);
	}

	/**
   *
   */
	public DataReference __sub__(double d) {
		return (DataReference) DataReferenceMath.scalarOperation(this, d,
				DataReferenceMath.SUB);
	}

	/**
   *
   */
	public DataReference __mul__(double d) {
		return (DataReference) DataReferenceMath.scalarOperation(this, d,
				DataReferenceMath.MUL);
	}

	/**
   *
   */
	public DataReference __div__(double d) {
		return (DataReference) DataReferenceMath.scalarOperation(this, d,
				DataReferenceMath.DIV);
	}

	/**
   *
   */
	public DataReference __radd__(double d) {
		return __add__(d);
	}

	/**
   *
   */
	public DataReference __rsub__(double d) {
		return (DataReference) DataReferenceMath.scalarOperation(this, d,
				DataReferenceMath.SUB, DataReferenceMath.FIRST_LAST);
	}

	/**
   *
   */
	public DataReference __rmul__(double d) {
		return __mul__(d);
	}

	/**
   *
   */
	public DataReference __rdiv__(double d) {
		return (DataReference) DataReferenceMath.scalarOperation(this, d,
				DataReferenceMath.DIV, DataReferenceMath.FIRST_LAST);
	}

	/**
	 * The name of this reference. Define default ?
	 */
	private String _name;
	/**
	 * The name of the server
	 */
	private String _servername;
	/**
	 * The context within which this pathname is guarenteed to be unique.
	 */
	private String _filename;
	/**
	 * The pathname
	 */
	private Pathname _pathname;
	/**
	 * The time window for data
	 */
	private TimeWindow _timeWindow;
	/**
	 * the time interval for data
	 */
	private TimeInterval _ti;
	/**
	 * the location for data
	 */
	private Location _location;
}
