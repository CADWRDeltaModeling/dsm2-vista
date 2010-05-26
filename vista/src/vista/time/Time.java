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

/**
 * An interface to a time object.
 * 
 * @author Nicky Sandhu
 * @version $Id: Time.java,v 1.1 2003/10/02 20:49:36 redwood Exp $
 */
public interface Time extends java.io.Serializable {
	/**
	 * creates a copy of the time object
	 * 
	 * @return the newly created copy
	 */
	public Time create(Time time);

	/**
	 * create a time object
	 * 
	 * @param tm
	 *            The time in minutes since base time.
	 * @return the time object
	 */
	public Time create(long tm);

	/**
	 * create a time object using default format and string
	 * 
	 * @param tmstr
	 *            The time string
	 * @return the time object
	 */
	public Time create(String tmstr);

	/**
	 * create a time object using default format and string
	 * 
	 * @param tmstr
	 *            The time string
	 * @return the time object
	 */
	public Time create(String tmstr, String pattern);

	/**
	 * create a time object initialized with time using time string and the
	 * formatter
	 * 
	 * @param tmstr
	 *            The time string
	 * @param formatter
	 *            The formatter for parsing the time string
	 * @return the time object
	 */
	public Time create(String tmstr, String pattern, TimeFormat formatter);

	/**
	 * get time in minutes since base time.
	 * 
	 * @return the time in minutes
	 */
	public long getTimeInMinutes();

	/**
	 * @param time
	 *            the time object to which this is compared to.
	 * @return 0 if equal, -ve if this time < given time or +ve if this time >
	 *         given time
	 */
	public int compare(Time time);

	/**
	 * formats date with default date formatter
	 * 
	 * @return a string representation of the time.
	 */
	public String format();

	/**
	 * formats date with given date formatter
	 * 
	 * @param formatter
	 *            The formatting object
	 * @return a string representation of the time.
	 */
	public String format(TimeFormat formatter);

	/**
	 * @param intervalId
	 *            The id of the field to be incremented
	 * @param numberOfIntervals
	 *            The number of intervals of that field to be incremented by.
	 */
	public void incrementBy(int intervalId, int numberOfIntervals);

	/**
	 * @param ti
	 *            The time interval by which current time is incremented by.
	 */
	public void incrementBy(TimeInterval ti);

	/**
	 * @param numberOfIncrements
	 *            the number of increments
	 * @param ti
	 *            The interval to be incremented by
	 */
	public void incrementBy(TimeInterval ti, int numberOfIncrements);

	/**
	 * @param the
	 *            time being compared with
	 * @return the number of minutes to given time
	 */
	public long getNumberOfMinutesTo(Time time);
	
	/**
	 * Converts the time value to date
	 * @return
	 */
	public Date getDate();

	/**
	 * calculate exactly the number of intervals as defined by the interval to
	 * be incremented for this time to be equal to given time.
	 * 
	 * @param time
	 *            The time to which interval is desiredd
	 * @param ti
	 *            the time interval by which current time will be incremented
	 *            by.
	 * @return number of times to be incremented by ti to exceed or be equal to
	 *         given time
	 */
	public long getExactNumberOfIntervalsTo(Time time, TimeInterval ti);

	/**
	 * @param time
	 *            The time to which interval is desiredd
	 * @param ti
	 *            the time interval by which current time will be incremented
	 *            by.
	 * @return number of times to be incremented by ti to exceed or be equal to
	 *         given time
	 */
	public long getNumberOfIntervalsTo(Time time, TimeInterval ti);

	/**
	 * rounds time to nearest nice interval >= current time and returns a new
	 * time with value
	 */
	public Time ceiling(TimeInterval ti);

	/**
	 * rounds time to nearest nice interval =< current time and returns a new
	 * time with value
	 */
	public Time floor(TimeInterval ti);

	/**
	 * rounds time to nearest nice interval. If exactly in middle it rounds to
	 * ceiling.
	 */
	public Time round(TimeInterval ti);

	/**
	 * for python
	 */
	public TimeInterval __sub__(Time tm);

	/**
	 * for python
	 */
	public Time __add__(TimeInterval ti);

	/**
	 * for python
	 */
	public Time __radd__(TimeInterval ti);

	/**
	 * for python
	 */
	public Time __sub__(TimeInterval ti);

	/**
	 * for python
	 */
	public Time __rsub__(TimeInterval ti);

	/**
	 * for python
	 */
	public Time __add__(String ti);

	/**
	 * for python
	 */
	public Time __radd__(String ti);

	/**
	 * for python
	 */
	public Object __sub__(String ti);

	/**
	 * for python
	 */
	public Object __rsub__(String ti);
}
