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
package vista.graph;

/**
 * An interface containing the data required for drawing a Curve.
 * 
 * @author Nicky Sandhu
 * @version $Id: CurveDataModel.java,v 1.1 2003/10/02 20:48:52 redwood Exp $
 */
public interface CurveDataModel {
	/**
	 * draws lines connecting points
	 */
	public static final int INST_VAL = 0;
	/**
	 * draws a horizontal line for the period
	 */
	public static final int PER_VAL = 1;
	/**
	 * draws the horizontal line at the last value
	 */
	public static final int LAST_VAL = 2;
	/**
   *
   */
	public static final int MOVE_TO = 1;
	/**
   *
   */
	public static final int LINE_TO = 2;
	/**
   *
   */
	public static final int QUESTIONABLE_AT = 3;
	/**
   *
   */
	public static final int REJECT_AT = 4;

	/**
	 * an object associated with this model.
	 */
	public Object getReferenceObject();

	/**
	 * an object associated with this model.
	 */
	public void setReferenceObject(Object obj);

	/**
	 * sets the maximum value for the current x axis
	 */
	public void setXViewMax(double max);

	/**
	 * gets the minimum value for the current x axis
	 */
	public void setXViewMin(double min);

	/**
	 * gets the maximum value for the x axis
	 */
	public double getXMax();

	/**
	 * gets the maximum value for the x axis
	 */
	public double getXMin();

	/**
	 * gets the maximum value for the x axis
	 */
	public double getYMax();

	/**
	 * gets the maximum value for the x axis
	 */
	public double getYMin();

	/**
	 * gets the interpolation type of the data
	 */
	public int getInterpolationType();

	/**
	 * resets the data iterator to beginning of curve
	 */
	public void reset();

	/**
	 * gets the next point
	 * 
	 * @param points
	 *            is an array wher points[0] contains the next x value and
	 *            points[1] contains the next y value
	 * @return an integer specifing movevment only or line drawing motion
	 */
	public int nextPoint(double[] points);

	/**
	 * @return true while has more points on curve
	 */
	public boolean hasMorePoints();

	/**
	 * gets teh legend text for this curve
	 */
	public String getLegendText();

	/**
	 * get the x axis position for this curve
	 */
	public int getXAxisPosition();

	/**
	 * geth the y axis position for this curve
	 */
	public int getYAxisPosition();

	/**
	 * get the tick generator for x axis
	 */
	public TickGenerator getXAxisTickGenerator();

	/**
	 * get the tick generator for the y axis
	 */
	public TickGenerator getYAxisTickGenerator();

	/**
	 * sets filter, I don't want to determine the exact class to not allow
	 * dependency between this package and other packages
	 */
	public void setFilter(Object filter);
	/*
	 * a rudimentary method to be called if data changes. A more sophisticated
	 * model will have to be made when curve editing is to be done. public void
	 * dataChanged();
	 */
}
