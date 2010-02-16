/*
    Copyright (C) 1996-2000 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0
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
package vista.dm;
import javax.swing.*;
import javax.swing.table.*;
/**
 * A table model for multiple time series
 *
 * @see DerivedTimeSeries
 *
 * @author Nicky Sandhu
 * @version $Id: MTSTableModel.java,v 1.3 2000/03/21 18:16:22 nsandhu Exp $
 */
public class MTSTableModel extends AbstractTableModel{
  public static String [] tableHeaders = { 
    "Derived Time Series", "A part", "B part", "C part", "Time Window", "E part", "F part"
  };
  /**
   * creates a table model from the given MTS
   */
  public MTSTableModel(MultipleTimeSeries mts){
    _mts = mts;
  }
  /**
   * Number of data references used in MTS calculations
   */
  public int getRowCount(){
    return _mts.getCount();
  }
  /**
   * Number of columns in the table
   */
  public int getColumnCount(){
    int count = 0;
    for(int i=0; i < _mts._ignoreParts.length; i++){
      if ( ! _mts._ignoreParts[i] ) count ++;
    }
    return 1+count;
  }
  /**
   * Returns the name of the column at <i>columnIndex</i>. 
   * @param	columnIndex	the index of column
   * @return  the name of the column
   */
  public String getColumnName(int columnIndex){
    int id = columnIndex == 0 ? columnIndex : getIdForColumn(columnIndex)+1;
    return tableHeaders[id];
  }
  /**
   * Returns true if the cell at <I>rowIndex</I> and <I>columnIndex</I>
   * is editable.  Otherwise, setValueAt() on the cell will not change
   * the value of that cell.
   *
   * @param	rowIndex	the row whose value is to be looked up
   * @param	columnIndex	the column whose value is to be looked up
   * @return	true if the cell is editable.
   * @see #setValueAt
   */
  public boolean isCellEditable(int rowIndex, int columnIndex){
    String dtsname = _mts.getRowAt(rowIndex).getDTSName();
    if ( columnIndex == 0 ) return false;
    if ( columnIndex > 1 ){
      if ( dtsname == null ){
	return true;
      }else {
	if ( dtsname.equals("") ){
	  int id = getIdForColumn(columnIndex);
	  if ( _mts._ignoreParts[id] ) return false;
	  return true;
	}else{
	  return false;
	}
      }
    } else {
      return true;
    }
  }
  /**
   * Returns an attribute value for the cell at <I>columnIndex</I>
   * and <I>rowIndex</I>.
   *
   * @param	rowIndex	the row whose value is to be looked up
   * @param	columnIndex 	the column whose value is to be looked up
   * @return	the value Object at the specified cell
   */
  public Object getValueAt(int rowIndex, int columnIndex){
    MTSRow row = _mts.getRowAt(rowIndex);
    String dtsname = row.getDTSName();
    if ( columnIndex == 0 ){
      if ( dtsname == null ){
	return "";
      }else{
	return dtsname;
      }
    } else {
      int id = getIdForColumn(columnIndex);
      if ( dtsname != null && ! dtsname.equals("") ) return "";
      String part = row.getPathPart(id);
      return part == null ? "" : part;
    }
  }
  /**
   *
   */
  private int getIdForColumn(int columnIndex){
    int nskip = 0;
    int index = 0;
    while(index < _mts._ignoreParts.length && nskip <= columnIndex-1){
      if ( !_mts._ignoreParts[index] ) 
	nskip++;
      index++;
    }
    return index-1;
  }
  /**
   * Sets an attribute value for the record in the cell at
   * @param	aValue		 the new value
   * @param	rowIndex	 the row whose value is to be changed
   * @param	columnIndex 	 the column whose value is to be changed
   * @see #getValueAt
   * @see #isCellEditable
   */
  public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    MTSRow row = _mts.getRowAt(rowIndex);
    if ( columnIndex == 0 ){
      String dtsname = (String) aValue;
      if ( dtsname.length() > 0 )
	row.setDTSName(dtsname);
    } else {
      // don't allow editing if dts name is non-empty
      String dtsname = row.getDTSName();
      if ( dtsname != null && ! dtsname.equals("") ) return;
      String part = (String) aValue;
      int id = getIdForColumn(columnIndex);
      row.setPathPart(part,id);
    }
  }
  /**
   *
   */
  private MultipleTimeSeries _mts;
}
