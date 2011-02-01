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
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import vista.set.Pathname;
import vista.gui.TableCellToolTip;
/**
 * The user interface object for a multiple time series
 *
 * @author Nicky Sandhu ,Armin Munevar
 * @version $Id: MTSUI.java,v 1.3 2000/03/21 18:16:22 nsandhu Exp $
 */
public class MTSUI extends JPanel{
  protected JTable _table;
  protected JRadioButton [] _useRB;
  protected Hashtable _actionTable = new Hashtable();
  private MultipleTimeSeries _mts;
  /**
    *
    */
  public MTSUI(MultipleTimeSeries mts){
    _mts = mts;
    // create a table for this mts
    _table = new JTable(new MTSTableModel(mts));
    int uw = 100;
    _table.getColumnModel().getColumn(0).setPreferredWidth(4*uw);
    for( int i=1; i < _table.getColumnCount(); i++)
      _table.getColumnModel().getColumn(i).setPreferredWidth(2*uw);
    //
    final Portfolio dtspt = mts.getStudy().getDTSPortfolio();
    // add a double click editor to pop up a jtree in a dialog box for selection
    // this selction is then displayed in the cell.
    new DTSCellAction(_table,dtspt);
    //new TableCellToolTip(_table);
    // 
    // create radio buttons for options
    _useRB = new JRadioButton[Pathname.MAX_PARTS];
    for(int i=0; i < Pathname.MAX_PARTS; i++){
      _useRB[i] = new JRadioButton(Pathname.getPartName(i));
      _useRB[i].setSelected(!_mts._ignoreParts[i]);
    }
    // add change listener for radio buttons.
    ChangeListener cl = new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
	JRadioButton btn = (JRadioButton) evt.getSource();
	for(int i=0; i < Pathname.MAX_PARTS; i++){
	  if ( btn == _useRB[i] ) _mts._ignoreParts[i] = ! btn.isSelected();
	}
	_table.editingStopped(new ChangeEvent(_table));
	((AbstractTableModel)_table.getModel()).fireTableStructureChanged();
	_table.sizeColumnsToFit(-1);
      }
    };
    for(int i=0; i < Pathname.MAX_PARTS; i++){
      _useRB[i].addChangeListener(cl);
    }
    // create an options panel
    JPanel optionsPanel = new JPanel();
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Parts displayed"));
    optionsPanel.setLayout(new GridLayout(1,Pathname.MAX_PARTS));
    for(int i=0; i < Pathname.MAX_PARTS; i++){
      optionsPanel.add( _useRB[i] );
    }
    // create a cell value display label
    JTextField ctf = new JTextField(40);
    ctf.setEditable(false);
    new TableCellListener(_table,ctf);
    JPanel cPanel = new JPanel();
    cPanel.setBorder(BorderFactory.createTitledBorder("Cell Value"));
    cPanel.add(ctf);
    // intialize actions
    initActions();
    // make menu bar for each of the actions
    JMenu editMenu = new JMenu("Edit");
    editMenu.add(getAction("Add Row"));
    editMenu.add(getAction("Insert Row"));
    editMenu.add(getAction("Delete Row(s)"));
    JMenuBar mbar = new JMenuBar();
    mbar.add(editMenu);
    // do layout of options panel and table
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(new JScrollPane(_table), BorderLayout.CENTER);
    JPanel np = new JPanel();
    np.setLayout(new BorderLayout());
    np.add(optionsPanel, BorderLayout.NORTH);
    np.add(cPanel, BorderLayout.SOUTH);
    panel.add(np, BorderLayout.NORTH);
    setLayout(new BorderLayout());
    add(mbar,BorderLayout.NORTH);
    add(panel,BorderLayout.CENTER);
  }
  /**
    * returns the action associated with the given name.
    */
  public Action getAction(String actionName){
    return (Action) _actionTable.get(actionName);
  }
  /**
    *
    */
  public String [] getAllActionNames(){
    int size = _actionTable.size();
    if ( size == 0 ) return null;
    String [] actionNames = new String[size];
    int count = 0;
    for(Enumeration e = _actionTable.keys(); e.hasMoreElements(); ){
      actionNames[count++] = e.nextElement().toString();
    }
    return actionNames;
  }
  /**
    *
    */
  protected void initActions(){
    _actionTable.put("Add Row",getAddRowAction());
    _actionTable.put("Insert Row",getInsertRowAction());
    _actionTable.put("Delete Row(s)",getDeleteRowAction());
  }
  /**
    *
    */
  public Action getAddRowAction(){
    Action action = new AbstractAction("Add Row"){
      public void actionPerformed(ActionEvent evt){
	_table.editingStopped(new ChangeEvent(_table));
	int index = _mts.getCount();
	_mts.add(MTSDefaults.createDefaultRow());
	_table.tableChanged(new TableModelEvent(_table.getModel()));
      }
    };
    return action;
  }
  /**
    *
    */
  public Action getInsertRowAction(){
    final Component comp = this;
    Action action = new AbstractAction("Insert Row"){
      public void actionPerformed(ActionEvent evt){
	_table.editingStopped(new ChangeEvent(_table));
	int ri = _table.getSelectedRow();
	if ( ri == -1 ){
	  JOptionPane.showMessageDialog(comp,"Message",
					"Select a row first!",
					JOptionPane.PLAIN_MESSAGE);
	  return;
	}
	_mts.insertAt(MTSDefaults.createDefaultRow(),ri);
	_table.tableChanged(new TableModelEvent(_table.getModel()));
      }
    };
    return action;
  }
  /**
    *
    */
  public Action getDeleteRowAction(){
    final Component comp = this;
    Action action = new AbstractAction("Delete Row(s)"){
      public void actionPerformed(ActionEvent evt){
	_table.editingStopped(new ChangeEvent(_table));
	// get user selected rows
	int[] ri = _table.getSelectedRows();
	if ( ri == null || ri.length == 0 ){
	  JOptionPane.showMessageDialog(comp,"Message",
					"Select a few rows first!",
					JOptionPane.PLAIN_MESSAGE);
	  return;
	}
	int numberDeleted = 0;
	for(int i=0; i < ri.length; i++) {
	  int currentIndex = ri[i] - numberDeleted;
	  if ( currentIndex >= _mts.getCount()) {
	    continue;
	  }
	  _mts.remove(currentIndex);
	  numberDeleted++;
	}
	_table.tableChanged(new TableModelEvent(_table.getModel()));
      }
    };
    return action;
  }
  /**
    *
    */
  class TableCellListener implements MouseMotionListener{
    JTable _table;
    JTextField _tf;
    public TableCellListener(JTable table, JTextField tf){
      _table = table;
      _table.addMouseMotionListener(this);
      _tf = tf;
    }
    //
    public void mouseMoved(MouseEvent evt){
      TableColumnModel columnModel = _table.getColumnModel();
      int viewColumn = columnModel.getColumnIndexAtX( evt.getX() );
      int column = _table.convertColumnIndexToModel(viewColumn); 
      int row = _table.rowAtPoint(evt.getPoint());
      _tf.setText(_table.getValueAt(row,column).toString());
    }
    //
    public void mouseDragged(MouseEvent evt){
    }
  }
}
