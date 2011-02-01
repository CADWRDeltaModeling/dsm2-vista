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
package vista.time;

import java.io.Serializable;

/**
 * Defines a range of time. This is used to specify the range for which the data
 * may be retrieved.
 */
public class DefaultTimeWindow implements TimeWindow, Serializable {
	/**
	 * private constructor
	 */
	DefaultTimeWindow() {
	}

	/**
	 * creates a time window using the given start and end minutes since Dec 31,
	 * 1899.
	 */
	DefaultTimeWindow(long stime, long etime) {
		if (stime > etime) {
			throw new IllegalArgumentException("Start time: " + stime + " > "
					+ "End time: " + etime);
		}
		TimeFactory _tf = TimeFactory.getInstance();
		_startTime = _tf.getTimeInstance().create(stime);
		_endTime = _tf.getTimeInstance().create(etime);
	}

	/**
	 * create a copy of itself
	 * 
	 * @return the copy
	 */
	public TimeWindow create() {
		DefaultTimeWindow ntw = new DefaultTimeWindow();
		ntw._startTime = _startTime;
		ntw._endTime = _endTime;
		return ntw;
	}

	/**
	 * creates a time window with the given start and end times.
	 */
	public TimeWindow create(Time startTime, Time endTime) {
		return new DefaultTimeWindow(startTime.getTimeInMinutes(), endTime
				.getTimeInMinutes());
	}

	/**
	 * Gets the starting time for this window
	 * 
	 * @return the starting time since Dec 31, 1899 Midnight
	 */
	public Time getStartTime() {
		return _startTime;
	}

	/**
	 * Gets the end time for this window
	 * 
	 * @return the ending time since Dec 31, 1899 Midnight
	 */
	public Time getEndTime() {
		return _endTime;
	}
	
	/**
	 * creates a time window which spans both this and the provided time window
	 * @param timeWindow
	 * @return 
	 */
	public TimeWindow union(TimeWindow timeWindow){
		if (timeWindow == null){
			return this.create(this.getStartTime(), this.getEndTime());
		}
		long stime = _startTime.getTimeInMinutes();
		long etime = _endTime.getTimeInMinutes();
		long ostime = timeWindow.getStartTime().getTimeInMinutes();
		long oetime = timeWindow.getEndTime().getTimeInMinutes();
		long nstime = Math.min(stime, ostime);
		long netime = Math.max(etime, oetime);
		if (nstime > netime)
			return null;
		else
			return new DefaultTimeWindow(nstime, netime);
	}

	/**
	 * creates a time window which is the intersection of this time window with
	 * given time window.
	 * 
	 * @return new time window object representing intersection or null if no
	 *         intersection is possible.
	 */
	public TimeWindow intersection(TimeWindow timeWindow) {
		if (timeWindow == null)
			return null;
		long stime = _startTime.getTimeInMinutes();
		long etime = _endTime.getTimeInMinutes();
		long ostime = timeWindow.getStartTime().getTimeInMinutes();
		long oetime = timeWindow.getEndTime().getTimeInMinutes();
		long nstime = Math.max(stime, ostime);
		long netime = Math.min(etime, oetime);
		if (nstime > netime)
			return null;
		else
			return new DefaultTimeWindow(nstime, netime);
	}

	/**
	 * true if time window intersects with the given time window
	 */
	public boolean intersects(TimeWindow timeWindow) {
		if (timeWindow == null)
			return false;
		long stime = _startTime.getTimeInMinutes();
		long etime = _endTime.getTimeInMinutes();
		long ostime = timeWindow.getStartTime().getTimeInMinutes();
		long oetime = timeWindow.getEndTime().getTimeInMinutes();
		if ((ostime > etime) || (oetime < stime))
			return false;
		else
			return true;
	}

	/**
	 * returns true if given time window is contained completely in the current
	 * time window.
	 */
	public boolean contains(TimeWindow timeWindow) {
		if (timeWindow == null)
			return false;
		long stime = _startTime.getTimeInMinutes();
		long etime = _endTime.getTimeInMinutes();
		long ostime = timeWindow.getStartTime().getTimeInMinutes();
		long oetime = timeWindow.getEndTime().getTimeInMinutes();
		return ((ostime >= stime) && (oetime <= etime))
				|| ((stime >= ostime) && (etime <= oetime));
	}

	/**
	 * returns true if this time window contains the given time
	 */
	public boolean contains(Time time) {
		return ((time != null) && (time.compare(_startTime) >= 0 && time
				.compare(_endTime) <= 0));
	}

	/**
	 * equals implementation
	 */
	public boolean equals(Object obj) {
		return (obj != null) && (obj instanceof TimeWindow)
				&& (((TimeWindow) obj).isSameAs(this));
	}

	/**
	 * tests for similarity
	 */
	public boolean isSameAs(TimeWindow tw) {
		return (tw.getEndTime().getTimeInMinutes() == _endTime
				.getTimeInMinutes())
				&& (tw.getStartTime().getTimeInMinutes() == _startTime
						.getTimeInMinutes());
	}

	/**
	 * string representation of the time window
	 */
	public String toString() {
		// TimeFormat formatter = new DefaultTimeFormat("ddMMMyyyy");
		// //DSSSessionBuilder.getTimeFactory().getTimeFormatInstance();
		// StringBuffer buf = new StringBuffer(100);
		// buf.append(_startTime.format(formatter));
		// buf.append(" - ");
		// buf.append(_endTime.format(formatter));
		// return buf.toString();
		StringBuffer buf = new StringBuffer(100);
		buf.append(_startTime.format());
		buf.append(" - ");
		buf.append(_endTime.format());
		return buf.toString();
	}

	Time _endTime;
	Time _startTime;
}
