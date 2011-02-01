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

import java.util.Date;

import org.python.core.Py;

/**
 * A factory initialized with instances of classes to be used as prototypes for
 * creating further instances.
 * 
 * @author Nicky Sandhu
 * @version $Id: TimeFactory.java,v 1.1 2003/10/02 20:49:36 redwood Exp $
 */
public class TimeFactory implements java.io.Serializable {
	/**
	 * instantiate the time factory with prototype instances
	 */
	public TimeFactory(Time time, TimeInterval ti, TimeFormat format,
			TimeWindow tw) {
		_time = time;
		_ti = ti;
		_format = format;
		_tw = tw;
	}

	/**
	 * an instance of the default implemenations
	 */
	private static TimeFactory _instance = new TimeFactory(new DefaultTime(),
			new DefaultTimeInterval(), new DefaultTimeFormat(),
			new DefaultTimeWindow());

	/**
	 * get the default implementation
	 */
	public static TimeFactory getInstance() {
		return _instance;
	}

	/**
	 * get prototype for time
	 * 
	 * @return an instance of the time object to be used as a prototype for
	 *         creating other time objects using this instance
	 */
	public Time getTimeInstance() {
		return _time;
	}

	/**
	 * creates a time object containing the same time as tm object
	 */
	public Time createTime(Time tm) {
		return _time.create(tm);
	}

	/**
	 * creates a time object containing the same time as tm object
	 */
	public Time createTime(long tm) {
		return _time.create(tm);
	}
	
	public Time createTime(Date date) {
		 Time time = _time.create(0);
		 time.setDate(date);
		 return time;
	}

	/**
	 * creates a time object containing the same time as tm object
	 */
	public Time createTime(String tmstr) {
		return _time.create(tmstr);
	}

	/**
	 * creates a time object containing the same time as tm object
	 */
	public Time createTime(String tmstr, String pattern) {
		return _time.create(tmstr, pattern);
	}

	/**
	 * creates a time object containing the same time as tm object
	 */
	public Time createTime(String tmstr, String pattern, TimeFormat formatter) {
		return _time.create(tmstr, pattern, formatter);
	}

	/**
	 * get prototype for time interval instance
	 * 
	 * @return a prototype for time interval
	 */
	public TimeInterval getTimeIntervalInstance() {
		return _ti;
	}

	/**
	 * creates a time interval
	 */
	public TimeInterval createTimeInterval(TimeInterval ti) {
		return _ti.create(ti);
	}

	/**
	 * creates a time interval
	 */
	public TimeInterval createTimeInterval(int numberOfIntervals,
			int intervalType) {
		return _ti.create(numberOfIntervals, intervalType);
	}

	/**
	 * creates a time interval
	 */
	public TimeInterval createTimeInterval(String intervalStr) {
		return _ti.create(intervalStr);
	}

	/**
	 * get prototype for time format
	 * 
	 * @return a prototype for time format object
	 */
	public TimeFormat getTimeFormatInstance() {
		return _format;
	}

	/**
	 * get instance of time window
	 */
	public TimeWindow getTimeWindowInstance() {
		return _tw;
	}

	/**
	 * creates time window with given start and end times
	 */
	public TimeWindow createTimeWindow(Time st, Time et) {
		return _tw.create(st, et);
	}

	/**
	 * creates time window from given string of TimeFormat time strings
	 * separated by a dash '-'
	 */
	public TimeWindow createTimeWindow(String s) {
		int dIndex = s.indexOf("-");
		if (dIndex < 0)
			throw new IllegalArgumentException(
					"Invalid string for time window " + s);
		String ststr = s.substring(0, dIndex).trim();
		String etstr = s.substring(dIndex + 1, s.length()).trim();
		Time stime = this.createTime(ststr);
		Time etime = this.createTime(etstr);
		return _tw.create(stime, etime);
	}

	/**
	 * creates time window from given string of TimeFormat time strings
	 * separated by a dash '-'
	 */
	public TimeWindow createTimeWindow(String s, String pattern) {
		int dIndex = s.indexOf("-");
		if (dIndex < 0)
			throw new IllegalArgumentException(
					"Invalid string for time window " + s);
		String ststr = s.substring(0, dIndex).trim();
		String etstr = s.substring(dIndex + 1, s.length()).trim();
		Time stime = this.createTime(ststr, pattern);
		Time etime = this.createTime(etstr, pattern);
		return _tw.create(stime, etime);
	}

	/**
	 * creates a time window from the given time window and time interval such
	 * that starting and ending times of the window are sane choices.
	 */
	public static TimeWindow createRoundedTimeWindow(TimeWindow tw,
			TimeInterval ti) {
		if (ti == null)
			return tw;
		Time stm = tw.getStartTime().create(tw.getStartTime());
		Time etm = stm.create(tw.getEndTime());
		stm = stm.create(stm.floor(ti));
		etm = stm.create(etm.ceiling(ti));
		return tw.create(stm, etm);
	}

	/**
	 * for python
	 */
	static TimeInterval getTimeIntervalFromString(String tistr) {
		TimeInterval ti = null;
		try {
			ti = TimeFactory.getInstance().createTimeInterval(tistr);
		} catch (Exception e) {
			throw Py.TypeError("time interval string expected");
		}
		return ti;
	}

	/**
	 * for python
	 */
	static Time getTimeFromString(String tmstr) {
		Time tm = null;
		try {
			tm = TimeFactory.getInstance().createTime(tmstr);
		} catch (Exception e) {
			throw Py.TypeError("time interval string expected");
		}
		return tm;
	}

	private Time _time;
	private TimeInterval _ti;
	private TimeFormat _format;
	private TimeWindow _tw;
}
