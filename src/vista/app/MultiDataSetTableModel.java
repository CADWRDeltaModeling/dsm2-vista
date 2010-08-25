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
package vista.app;

import java.text.NumberFormat;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import vista.set.DataSetElement;
import vista.set.DataSetIterator;
import vista.set.MultiIterator;
import vista.set.TimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;

/**
 * An adapter to adapt the data sets to the table model to allow representation
 * of the data set as a table.
 * 
 * @author Nicky Sandhu
 * @version $Id: MultiDataSetTableModel.java,v 1.1 2003/10/02 20:48:35 redwood
 *          Exp $
 */
public class MultiDataSetTableModel extends AbstractTableModel {
	/**
	 * constructs an adapter for data set for use by JTable
	 */
	public MultiDataSetTableModel(TimeSeries[] ts) {
		_columnNames = new String[ts.length];
		_xColumnHeader = "TIME";
		_dsi = new MultiIterator(ts);
		_dataCount = 0;
		while (!_dsi.atEnd()) {
			_dataCount++;
			_dsi.advance();
		}
		// System.out.println("Data count = " + _dataCount);
		_dsi.resetIterator();
		for (int i = 0; i < ts.length; i++) {
			_columnNames[i] = ts[i].getName();
		}
		_numberOfColumns = _dsi.getElement().getDimension();
		_flagDisplayed = false;
	}

	/**
	 * returns the number of rows or size of data...
	 */
	public int getRowCount() {
		return _dataCount;
	}

	/**
	 * The number of columns 2 or 3 depending upon whether or not to show flags
	 */
	public int getColumnCount() {
		if (_flagDisplayed) {
			return _numberOfColumns * 2 - 1;
		} else {
			return _numberOfColumns;
		}
	}

	/**
	 * true if flags are displayed in table
	 */
	public boolean isFlagDisplayed() {
		return _flagDisplayed;
	}

	/**
	 * false if flags are displayed in table.
	 */
	public void setFlagDisplayed(boolean b) {
		_flagDisplayed = b;
	}

	/**
	 * true except for time string in time series
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return false;
		else
			return true;
	}

	/**
	 * returns name of column for given index
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return _xColumnHeader;
		else {
			if (_flagDisplayed) {
				if (columnIndex % 2 == 1)
					return _columnNames[(columnIndex + 1) / 2 - 1];
				else
					return "FLAG VALUE";
			} else {
				return _columnNames[columnIndex - 1];
			}
		}
	}

	/**
	 * returns value of object at row, column.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		_dsi.positionAtIndex(rowIndex);
		if (_flagDisplayed) {
			if (columnIndex == 0) {
				return _dsi.getElement().getXString(columnIndex);
			} else if (columnIndex % 2 == 1) {
				return _dsi.getElement().getXString((columnIndex + 1) / 2);
			} else {
				return _dsi.getElement().getFlagString(columnIndex / 2 - 1);
			}
		} else {
			return _dsi.getElement().getXString(columnIndex);
		}
	}

	/**
	 * sets value to edited value...
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		_dsi.positionAtIndex(rowIndex);
		if (columnIndex > 0) {
			DataSetElement dse = _dsi.getElement();
			try {
				if (_flagDisplayed) {
					if (columnIndex == 0) {
						dse.setX(columnIndex, _formatter.parse((String) aValue)
								.doubleValue());
					} else if (columnIndex % 2 == 1) {
						dse.setX((columnIndex + 1) / 2, _formatter.parse(
								(String) aValue).doubleValue());
					} else {
						// don't allow editing of flags...
						// dse.setFlag(columnIndex/2-1,_formatter.parse(
						// (String) aValue).intValue());
					}
				} else {
					dse.setX(columnIndex, _formatter.parse((String) aValue)
							.doubleValue());
				}
			} catch (java.text.ParseException pe) {
				JOptionPane.showMessageDialog(null, pe.getMessage());
			}
			_dsi.putElement(dse);
		}
	}

	/**
	 * data set
	 */
	private TimeSeries[] _ts;
	/**
	 * iterator
	 */
	private DataSetIterator _dsi;
	/**
	 * row count and column count
	 */
	private int _dataCount, _numberOfColumns;
	/**
	 * The header for x values
	 */
	private String _xColumnHeader;
	private String[] _columnNames;
	private boolean _flagDisplayed;
	/**
	 * time format
	 */
	private Time _time;
	/**
	 * number format
	 */
	private NumberFormat _formatter = NumberFormat.getInstance();
	/**
	 * number format
	 */
	private NumberFormat _flagFormatter = NumberFormat.getInstance();
	/**
   *
   */
	private TimeFactory _tf = TimeFactory.getInstance();
}
