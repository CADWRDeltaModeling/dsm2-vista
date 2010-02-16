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
 * This class encapsulates tick data information such as the data values at
 * which to place ticks and the length of the tick as a percentage of the
 * drawing area.
 * 
 * @author Nicky Sandhu (DWR).
 * @version $Id: TickData.java,v 1.1 2003/10/02 20:49:10 redwood Exp $
 */
public class TickData {
	/**
	 * for debuggin
	 */
	public static final boolean DEBUG = false;
	private double _dataMin, _dataMax;

	/**
	 * constructor
	 * 
	 * @param values
	 *            An array containing the data values at which to place the tick
	 *            marks at.
	 * @param percentTickLength
	 *            The length of the ticks as a percentage of the drawing area.
	 */
	public TickData(double[] values, String[] labels, double dataMin,
			double dataMax) {
		_values = values;
		_number = _values.length;
		_labels = labels;
		_dataMin = dataMin;
		_dataMax = dataMax;
		_scale = new Scale(_dataMin, _dataMax, 0, 10);
	}

	/**
	 * returns the maximum data coordinate value
	 */
	public double getMaxDCValue() {
		return _scale.getDataMaximum();
	}

	/**
	 * returns the minimum data coordinate value
	 */
	public double getMinDCValue() {
		return _scale.getDataMinimum();
	}

	/**
	 * sets scale to new data range
	 */
	public void setDCRange(double min, double max) {
		_scale.setDCRange(min, max);
	}

	/**
	 * sets scale to new user coordinate range.
	 */
	public void setUCRange(int amin, int amax) {
		_scale.setUCRange(amin, amax);
	}

	/**
	 * returns user coordinate value of the indexed tick
	 * 
	 * @param index
	 *            The tick at index in the tick values array
	 * @return The tick value converted to user coordinates
	 */
	public int getUCValue(int index) {
		if (DEBUG && index == 0)
			System.out.println(_values[index]);
		if (DEBUG && index == 0)
			System.out.println(_scale.scaleToUC(_values[index]));
		return _scale.scaleToUC(_values[index]);
	}

	/**
	 * Returns the data coordinate value for the indexed tick
	 * 
	 * @param index
	 *            The tick at index in the tick values array
	 * @return The tick value
	 */
	public double getDCValue(int index) {
		return _values[index];
	}

	/**
	 * gets scale of tick data
	 */
	public Scale getScale() {
		return _scale;
	}

	/**
	 * get number of tick marks in complete range.
	 */
	public int getNumber() {
		return _number;
	}

	/**
	 * get label for each tick mark.
	 */
	public String[] getLabels() {
		return _labels;
	}

	/**
   *
   */
	public String toString() {
		StringBuffer sb = new StringBuffer(this.getClass().getName());
		sb.append("Number of ticks = " + this.getNumber() + "\n");
		sb.append("TickValues: Index, Value \n");
		for (int i = 0; i < this.getNumber(); i++) {
			sb.append(i + ", " + this.getDCValue(i) + " | ");
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * scale factor = screenLength/dataRange
	 */
	private Scale _scale;
	/**
	 * number of tick marks
	 */
	private int _number;
	/**
	 * an array of data values spanning the entire range. These data values
	 * correspond to tick positions.
	 */
	private double[] _values;
	/**
	 * an array of labels for each index of the _values array.
	 */
	private String[] _labels;
};
