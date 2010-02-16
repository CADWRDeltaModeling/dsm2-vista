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

import javax.swing.table.AbstractTableModel;

/**
 * This class displays the session information as a table by implementing the
 * TableModel. The table columns are the parts of the pathname and the ordering
 * can be shuffled.
 * 
 * @author Nicky Sandhu
 * @version $Id: SessionTableModel.java,v 1.1 2003/10/02 20:49:32 redwood Exp $
 */
public class SessionTableModel extends AbstractTableModel {
	/**
	 * Construct a table
	 */
	public SessionTableModel(Session ss) {
		_session = ss;
	}

	/**
	 * returns the number of rows or size of data...
	 */
	public int getRowCount() {
		return _session.getNumberOfGroups();
	}

	/**
	 * The number of columns
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 * returns name of column for given index
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return "No.";
		else
			return "GROUP NAME";
	}

	/**
	 * returns value of object at row, column.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return "" + (rowIndex + 1);
		else
			return _session.getGroup(rowIndex).getName();
	}

	/**
	 * set value to aValue for row and column
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
		} else {
			_session.getGroup(rowIndex).setName((String) aValue);
		}
	}

	/**
	 * returns true for all cells
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * the session
	 */
	private Session _session;
}
