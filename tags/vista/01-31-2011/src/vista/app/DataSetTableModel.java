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
import java.text.ParseException;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import vista.db.dss.DSSUtil;
import vista.gui.VistaUtils;
import vista.set.Constants;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.DataSetElement;
import vista.set.DataSetIterator;
import vista.set.FlagUtils;
import vista.set.SetUtils;
import vista.set.TimeSeries;
import vista.set.Units;
import vista.time.Time;
import vista.time.TimeFactory;

/**
 * An adapter to adapt the data sets to the table model to allow representation
 * of the data set as a table.
 * 
 * @author Nicky Sandhu
 * @version $Id: DataSetTableModel.java,v 1.1 2003/10/02 20:48:27 redwood Exp $
 */
public class DataSetTableModel extends AbstractTableModel {
	private boolean _flagOverride;
	/**
	 * missing value and record display strings.
	 */
	public static final String MV = "MISSING VALUE", MR = "MISSING RECORD";

	/**
	 * constructs an adapter for data set for use by JTable
	 */
	public DataSetTableModel(DataSet data) {
		_flagOverride = false;
		_data = data;
		_dsi = _data.getIterator();
		_dataCount = _data.size();
		if (_data.isFlagged())
			_numberOfColumns = 3;
		else
			_numberOfColumns = 2;
		DataSetAttr attr = _data.getAttributes();
		if (data instanceof TimeSeries) {
			_showXAsTime = true;
			_time = _tf.getTimeInstance().create(0);
			_xColumnHeader = "Time";
			_yColumnHeader = "Value";
			_flagColumnHeader = "Flag Value";
		} else if (attr != null && attr.getXUnits().equals(Units.TIME)) {
			_showXAsTime = true;
			_time = _tf.getTimeInstance().create(0);
			_xColumnHeader = "Time";
			_yColumnHeader = "Value";
			_flagColumnHeader = "Flag Value";
		} else {
			_showXAsTime = false;
			_xColumnHeader = "X Value";
			_yColumnHeader = "Y Value";
			_flagColumnHeader = "Flag Value";
		}
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
		return _numberOfColumns;
	}

	/**
	 * true if flags are displayed in table
	 */
	public boolean isFlagDisplayed() {
		return _numberOfColumns == 3;
	}

	/**
	 * false if flags are displayed in table.
	 */
	public void setFlagDisplayed(boolean b) {
		if (b) {
			if (_data.isFlagged())
				_numberOfColumns = 3;
			else
				JOptionPane.showMessageDialog(null, "No flags available?");
		} else
			_numberOfColumns = 2;
		fireTableStructureChanged();
	}

	/**
   *
   */
	public boolean isFlagOveridden() {
		return _flagOverride;
	}

	/**
   *
   */
	public void setFlagOveridden(boolean b) {
		_flagOverride = b;
	}

	/**
	 * true except for time string in time series
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (_showXAsTime) {
			if (columnIndex == 0)
				return false;
			else
				return true;
		} else {
			return true;
		}
	}

	/**
	 * returns name of column for given index
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return _xColumnHeader;
		else if (columnIndex == 1)
			return _yColumnHeader;
		else if (columnIndex == 2)
			return _flagColumnHeader;
		else
			return "";
	}

	/**
	 * returns value of object at row, column.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		positionAtIndex(rowIndex);
		if (columnIndex == 0) {
			if (_showXAsTime) {
				_time = _tf.getTimeInstance().create(
						Math.round(_dsi.getElement().getX()));
				return _time.toString();
			} else {
				return SetUtils.format(_dsi.getElement().getX());
			}
		} else if (columnIndex == 1) {
			double y = _dsi.getElement().getY();
			if (y == Constants.MISSING || y == Constants.MISSING_VALUE)
				return MV;
			else if (y == Constants.MISSING_RECORD)
				return MR;
			else if (Double.doubleToLongBits(y) == 0x7ff8000000000000L)
				return "NaN";
			else
				return SetUtils.format(y);
		} else if (columnIndex == 2) {
			DataSetElement dse = _dsi.getElement();
			return FlagUtils.getQualityFlagName(FlagUtils.getQualityFlag(dse))
					+ " | " + FlagUtils.getLastCheckedBy(dse);
		} else {
			throw new IllegalArgumentException(
					"attempted to get value at column " + columnIndex);
		}
	}

	/**
	 * sets value to edited value...
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		positionAtIndex(rowIndex);
		if (columnIndex == 0) {
			if (_showXAsTime) {
				try {
					_time = _tf.getTimeInstance().create((String) aValue);
					DataSetElement dse = _dsi.getElement();
					dse.setX(_time.getTimeInMinutes());
					_dsi.putElement(dse);
				} catch (IllegalArgumentException pe) {
					System.out
							.println("Could not change data: Time format may be incorrect");
				}
			} else {
				try {
					DataSetElement dse = _dsi.getElement();
					dse.setX(_formatter.parse((String) aValue).doubleValue());
					_dsi.putElement(dse);
				} catch (ParseException pe) {
					System.out.println("Incorrect format for double");
				}
			}
		} else if (columnIndex == 1) {
			try {
				DataSetElement dse = _dsi.getElement();
				dse.setY(_formatter.parse((String) aValue).doubleValue());
				_dsi.putElement(dse);
			} catch (ParseException pe) {
				System.out.println("Incorrect format for double");
			}
		} else if (columnIndex == 2) {
			try {
				String flagName = (String) aValue;
				if (flagName.equals("                "))
					return;
				DataSetElement dse = _dsi.getElement();
				int flagType = FlagUtils.getQualityFlagId(flagName);
				int userId = DSSUtil.getUserId();
				double y = _dsi.getElement().getY();
				if (FlagUtils.isScreened(dse) && !_flagOverride) {
				} else if (y == Constants.MISSING
						|| y == Constants.MISSING_VALUE
						|| y == Constants.MISSING_RECORD) {
					FlagUtils.setQualityFlag(dse, FlagUtils.MISSING_FLAG,
							userId);
					_dsi.putElement(dse);
				} else {
					if (flagType == FlagUtils.UNSCREENED_FLAG) {
						FlagUtils.clearAllFlags(dse, userId);
					} else {
						FlagUtils.setQualityFlag(dse, flagType, userId);
					}
					_dsi.putElement(dse);
				}
			} catch (Exception e) {
				VistaUtils.displayException(null, e);
			}
		} else {
		}
	}

	/**
	 * positions the iterator at correct index
	 */
	private void positionAtIndex(int rowIndex) {
		int currentIndex = _dsi.getIndex();
		int diff = rowIndex - currentIndex;
		if (diff > 0) {
			for (int i = 0; i < diff; i++)
				_dsi.advance();
		} else if (diff < 0) {
			diff = -diff;
			for (int i = 0; i < diff; i++)
				_dsi.retreat();
		}
	}

	/**
	 * data set
	 */
	private DataSet _data;
	/**
	 * iterator
	 */
	private DataSetIterator _dsi;
	/**
	 * row count and column count
	 */
	private int _dataCount, _numberOfColumns;
	/**
	 * show x column as time
	 */
	private boolean _showXAsTime;
	/**
	 * The header for x values
	 */
	private String _xColumnHeader;
	/**
	 * The header for y values
	 */
	private String _yColumnHeader;
	/**
	 * The header for flag values
	 */
	private String _flagColumnHeader;
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
