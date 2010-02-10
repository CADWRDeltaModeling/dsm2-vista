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

/**
 * A interface encapsulating the interval associated with a time series.
 * 
 * Intervals can be converted to a time interval in minutes, however some
 * intervals are independent of the current time context such as minutes, hours,
 * days and other intervals have to be interpreted in relation to the current
 * time e.g. months, years, decades, centuries.
 * 
 * This is meaningful really in the context of a regular time series.
 *  FIXME: Time interval should have a conversion method to change say 60 min interval to 1 hour interval
 * @author Nicky Sandhu
 * @version $Id: TimeInterval.java,v 1.1 2003/10/02 20:49:37 redwood Exp $
 */
public interface TimeInterval extends java.io.Serializable {
	/**
	 * minute interval. The smallest interval represented by this interface
	 */
	public static int MIN_INTERVAL = 1;
	/**
	 * hour interval = 60 minutes
	 */
	public static int HOUR_INTERVAL = 2;
	/**
	 * day interval = 24 hours
	 */
	public static int DAY_INTERVAL = 3;
	/**
	 * week interval = 7 days
	 */
	public static int WEEK_INTERVAL = 4;
	/**
	 * month interval ~ (28,29,30,31) days
	 */
	public static int MONTH_INTERVAL = 5;
	/**
	 * year interval = 12 months
	 */
	public static int YEAR_INTERVAL = 6;
	/**
	 * decade interval = 10 years
	 */
	public static int DECADE_INTERVAL = 7;
	/**
	 * century interval = 100 years
	 */
	public static int CENTURY_INTERVAL = 8;
	/**
   *
   */
	public static final String MIN_INTERVAL_STR = "MIN";
	/**
   *
   */
	public static final String HOUR_INTERVAL_STR = "HOUR";
	/**
   *
   */
	public static final String DAY_INTERVAL_STR = "DAY";
	/**
   *
   */
	public static final String WEEK_INTERVAL_STR = "WEEK";
	/**
   *
   */
	public static final String MONTH_INTERVAL_STR = "MON";
	/**
   *
   */
	public static final String YEAR_INTERVAL_STR = "YEAR";
	/**
   *
   */
	public static final String DECADE_INTERVAL_STR = "DECADE";
	/**
   *
   */
	public static final String CENTURY_INTERVAL_STR = "CENTURY";

	/**
	 * create copy of self
	 * 
	 * @return copy of self
	 */
	public TimeInterval create(TimeInterval ti);

	/**
	 * creates a time interval from number of intervals and type of interval.
	 * 
	 * @param numberOfIntervals
	 *            The number of intervals
	 * @param intervalType
	 *            The type of interval as defined by XXX_INTERVAL constants in
	 *            this interface.
	 * 
	 */
	public TimeInterval create(int numberOfIntervals, int intervalType);

	/**
	 * @param intervalRep
	 *            A underscore delimited string of sign, integer and interval
	 *            type strings. E.g. 1day_+6hours or 3years_-5months_+3mins
	 *            create a interval in minutes from a string.
	 */
	public TimeInterval create(String intervalRep);

	/**
	 * gets the number of intervals of type field. This field is one of the
	 * XXX_INTERVAL types.
	 * 
	 * @return the number of intervals of type field
	 */
	public int getNumberOfIntervals(int field);

	/**
	 * @return a string representing the field
	 */
	public String getFieldName(int field);

	/**
	 * true if current time matters in converting interval to minutes
	 */
	public boolean isTimeContextDependent();

	/**
	 * converts current interval to minutes depending upon current time.
	 */
	public long getIntervalInMinutes(Time time);

	/**
	 * @return the string associated with type of interval
	 */
	public String getIntervalAsString();

	/**
	 * Construct a time interval representing a time interval as calculated
	 * using this interval and the multiplying factor.
	 * 
	 * @return a new time interval
	 */
	public TimeInterval createByMultiplying(int factor);

	/**
	 * Construct a time interval representing a time interval as calculated
	 * using this time interval and adding it to the given time interval
	 * 
	 * @return a new time interval
	 */
	public TimeInterval createByAdding(TimeInterval ti);

	/**
	 * returns 0 if equal, -ve if less than this interval and +ve if more than
	 * this interval
	 */
	public int compare(TimeInterval ti);

	/**
	 * for python
	 */
	public TimeInterval __add__(TimeInterval ti);

	/**
	 * for python
	 */
	public TimeInterval __sub__(TimeInterval ti);

	/**
	 * for python
	 */
	public TimeInterval __mul__(int factor);

	/**
	 * for python
	 */
	public TimeInterval __rmul__(int factor);

	/**
	 * for python
	 */
	public int __div__(TimeInterval ti);

	/**
	 * for python
	 */
	public TimeInterval __add__(String tistr);

	/**
	 * for python
	 */
	public TimeInterval __sub__(String tistr);

	/**
	 * for python
	 */
	public int __div__(String tistr);
}
