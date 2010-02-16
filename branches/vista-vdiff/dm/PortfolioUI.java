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
import vista.gui.VistaUtils;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;
/**
 * The user interface for a portfolio. This generally consists ot two panels.
 * The left panel displays a tree showing the structure of the Portfolio while
 * right panel displays the current selected leaf's ( if any ) UI.
 *
 * @author Nicky Sandhu 
 * @version $Id: PortfolioUI.java,v 1.2 2000/03/21 18:16:23 nsandhu Exp $
 */
public class PortfolioUI extends JPanel{
  private Portfolio _portfolio;
  private Hashtable _actionTable = new Hashtable();
  /**
   * constructs a UI initialized with the state of the given portfolio.
   */
  public PortfolioUI(Portfolio pr){
    this.setLayout(new BorderLayout());
    setPortfolio(pr);
    initActions();
  }
  /**
   * sets the portfolio for this UI.
   */
  public void setPortfolio(Portfolio pr){
    _portfolio = pr;
    if ( _tree == null ) _tree = new JTree();
    _tree.setModel(pr);
    this.add(new JScrollPane(_tree),BorderLayout.WEST);
    _tree.addTreeSelectionListener( new LeafSelectionListener() );
    // LeafMover mover = new LeafMover();
    // _tree.addMouseListener( mover );
    // _tree.addMouseMotionListener( mover );
    _tree.setEditable(true);
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
   * initialize actions
   */
  private void initActions(){
    _actionTable.put("Add Folder",getAddFolderAction());
    _actionTable.put("Add Leaf",getAddLeafAction());
    _actionTable.put("Remove Selected",getRemoveAction());
    _actionTable.put("Move Into", getMoveIntoAction());
    _actionTable.put("Move Up", getMoveUpAction());
    _actionTable.put("Save (XML) ...", getSaveXMLAction());
    _actionTable.put("Load (XML) ...", getLoadXMLAction());
  }
  /**
   * 
   */
  public Action getAddFolderAction(){
    Action action = new AbstractAction("Add Folder"){
      public void actionPerformed(ActionEvent evt){
	try {
	  TreePath path = _tree.getSelectionPath();
	  if ( path != null ) {
	    DefaultMutableTreeNode nearestChild = 
	      (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-1);
	    DefaultMutableTreeNode parent = null;
	    if ( nearestChild.getAllowsChildren() ) 
	      parent  = nearestChild;
	    else
	      parent = 
		(DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-2);
	    int index = parent == nearestChild ? 0 : parent.getIndex(nearestChild);
	    _portfolio.addFolder("Untitled", parent, index);
	  }
	}catch(Exception e){
	  VistaUtils.displayException(this$0,e);
	}
      }
    };
    return action;
  }
  /**
   *
   */
  public Action getAddLeafAction(){
    Action action = new AbstractAction("Add Leaf"){
      public void actionPerformed(ActionEvent evt){
	try {
	  TreePath path = _tree.getSelectionPath();
	  if ( path != null ) {
	    DefaultMutableTreeNode nearestChild = 
	      (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-1);
	    DefaultMutableTreeNode parent = null;
	    if ( nearestChild.getAllowsChildren() ) 
	      parent  = nearestChild;
	    else
	      parent = 
		(DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-2);
	    int index = parent == nearestChild ? 0 : parent.getIndex(nearestChild);
	    _portfolio.addInFolder("Untitled", parent, index);
	  }
	}catch(Exception e){
	  VistaUtils.displayException(this$0,e);
	}
      }
    };
    return action;
  }
  /**
   *
   */
  public Action getRemoveAction(){
    Action action = new AbstractAction("Remove Selected"){
      public void actionPerformed(ActionEvent evt){
	try {
	  TreePath path = _tree.getSelectionPath();
	  if ( path != null ) {
	    DefaultMutableTreeNode nearestNode = 
	      (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-1);
	    _portfolio.removeNodeFromParent(nearestNode);
	  }
	}catch(Exception e){
	  VistaUtils.displayException(this$0,e);
	}
      }
    };
    return action;
  }
  /**
   *
   */
  public Action getMoveIntoAction(){
    final Component comp = this;
    Action action = new AbstractAction("Move Into"){
      public void actionPerformed(ActionEvent evt){
	try {
	  // Query for folder
	  String folderName = JOptionPane.showInputDialog(comp,"Move into folder ->");
	  if ( folderName == null ) return;
	  //
	  TreePath path = _tree.getSelectionPath();
	  if ( path != null ) {
	    DefaultMutableTreeNode nearestNode = 
	      (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-1);
	    // look for folder name at same level
	    DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) nearestNode.getParent();
	    int count = pnode.getChildCount();
	    DefaultMutableTreeNode cnode = null;
	    for(int i=0; i < count; i++){
	      cnode = (DefaultMutableTreeNode) pnode.getChildAt(i);
	      System.out.println("Child node " + i + " = " + cnode);
	      if ( cnode.toString().equals(folderName.toUpperCase()) ) break;
	    }
	    if ( cnode == null ) 
	      throw new RuntimeException("No folder " + folderName + " @ the selected items level");
	    if ( cnode == nearestNode )
	      throw new RuntimeException("Attempt to move item into itself");
	    if ( ! cnode.getAllowsChildren()) 
	      throw new RuntimeException(folderName + " is not a folder");
	    // remove node from here
	    _portfolio.removeNodeFromParent(nearestNode);
	    // add it to named folder, the index doesn't matter as it is decided alphabetically...
	    if ( nearestNode.getAllowsChildren() )
	      _portfolio.addFolder(nearestNode.toString(),cnode,0);
	    else 
	      _portfolio.addInFolder((NamedLeaf) nearestNode.getUserObject(), cnode, 0);
	  }
	}catch(Exception e){
	  VistaUtils.displayException(this$0,e);
	}
      }
    };
    return action;
  }
  /**
   *
   */
  public Action getMoveUpAction(){
    Action action = new AbstractAction("Move Up"){
      public void actionPerformed(ActionEvent evt){
	try {
	  TreePath path = _tree.getSelectionPath();
	  if ( path != null ) {
	    DefaultMutableTreeNode nearestNode = 
	      (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount()-1);
	    // look for folder name at same level
	    DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) nearestNode.getParent();
	    if ( pnode == null ) 
	      throw new RuntimeException("No parent folder for this one");
	    pnode = (DefaultMutableTreeNode) pnode.getParent();
	    //
	    if ( pnode == null ) 
	      throw new RuntimeException("No folder above the selected items level");
	    // remove node from here
	    _portfolio.removeNodeFromParent(nearestNode);
	    // add it to named folder, the index doesn't matter as it is decided alphabetically...
	    try {
	      if ( nearestNode.getAllowsChildren() )
		_portfolio.addFolder(nearestNode.toString(),pnode,0);
	      else 
		_portfolio.addInFolder((NamedLeaf) nearestNode.getUserObject(), pnode, 0);
	    }catch(Exception e){
	      // if there is a problem add the node back 
	      if ( nearestNode.getAllowsChildren() )
		_portfolio.addFolder(nearestNode.toString(),
				     (DefaultMutableTreeNode) nearestNode.getParent(),0);
	      else 
		_portfolio.addInFolder((NamedLeaf) nearestNode.getUserObject(), 
				       (DefaultMutableTreeNode) nearestNode.getParent(),0);
	      throw new RuntimeException("Nested Exception: " + e);
	    }
	  }
	}catch(Exception e){
	  VistaUtils.displayException(this$0,e);
	}
      }
    };
    return action;
  }
  /**
   *
   */
  public Action getSaveXMLAction(){
    final Component comp = this;
    Action action = new AbstractAction("Save (XML) ..."){
      public void actionPerformed(ActionEvent evt){
	try {
	  String saveFile = VistaUtils.getFilenameFromDialog(comp, FileDialog.SAVE, "xml", "XML format");
	  if ( saveFile == null ) return;
	  _portfolio.save(saveFile);
	}catch(Exception e){
	  VistaUtils.displayException(comp,e);
	}
      }
    };
    return action;
  }
  /**
   *
   */
  public Action getLoadXMLAction(){
    final Component comp = this;
    Action action = new AbstractAction("Load (XML) ..."){
      public void actionPerformed(ActionEvent evt){
	try {
	  String loadFile = VistaUtils.getFilenameFromDialog(comp, FileDialog.LOAD, "xml", "XML format");
	  if ( loadFile == null ) return;
	  _portfolio.load(loadFile);
	}catch(Exception e){
	  VistaUtils.displayException(comp,e);
	}
      }
    };
    return action;
  }
  /**
   * displays the leaf ui when informed by the leaf selection listener
   */
  public void displayLeafUI(DefaultMutableTreeNode tn){
    String name = ((NamedLeaf) tn.getUserObject()).getName();
    if ( _portfolio.getNamed(name)  == null ) return;
    if ( _currentUI != null )
      this.remove(_currentUI);
    _currentUI = _portfolio.getNamed(name).getUI();
    System.out.println("Displaying UI for " + name);
    this.add(_currentUI,BorderLayout.CENTER);
    this.validate();
    this.repaint();
  }
  /**
   * the tree display for the left panel.
   */
  private JTree _tree;
  private JPanel _currentUI;
  /**
   * A listener for leaves to display the leaves UI when selected.
   *
   * @author Nicky Sandhu 
   * @version $Id: PortfolioUI.java,v 1.2 2000/03/21 18:16:23 nsandhu Exp $
   */
  class LeafSelectionListener implements TreeSelectionListener{
    /**
     * calls back a method in main class...
     */
    public void valueChanged(TreeSelectionEvent evt){
      TreePath path = evt.getPath();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
      if ( node.getAllowsChildren() ) return;
      displayLeafUI(node);
    }
  }
  /**
   *
   *
   * @author Nicky Sandhu
   * @version $Id: PortfolioUI.java,v 1.2 2000/03/21 18:16:23 nsandhu Exp $
   */
  class LeafMover implements MouseMotionListener, MouseListener{
    private Portfolio model;
    private DefaultMutableTreeNode _node;
    public LeafMover(){
      model = (Portfolio) _tree.getModel();
    }
    public void mouseClicked(MouseEvent evt){
    }
    public void mousePressed(MouseEvent evt){
      TreePath path = _tree.getSelectionPath();
      if ( path != null ){
	_node = (DefaultMutableTreeNode) path.getLastPathComponent();
	if ( _node.getAllowsChildren() ) _node = null;
      }
      else
	_node = null;
    }
    public void mouseReleased(MouseEvent evt){
      if ( _node != null ){
	TreePath path = _tree.getClosestPathForLocation(evt.getX(), evt.getY());
	int row = _tree.getRowForPath(path);
	if ( row == -1 ) return;
	if ( path == null ) {
	  _node = null;
	  return;
	}
	// get nearest folder above this point
	MutableTreeNode folder = null;
	int count = path.getPathCount() -1;
	while( folder == null || !folder.getAllowsChildren()){
	  if ( count < 0 ) break;
	  folder = (MutableTreeNode) path.getPathComponent(count);
	  if ( folder.getAllowsChildren() )break;
	}
	if ( count < 0 ) return;
	// remove from tree.
	model.removeNodeFromParent( _node );
	// insert into nearest folder at nearest point
	model.insertNodeInto( _node, folder, row);
      }
    }
    public void mouseEntered(MouseEvent evt){
    }
    public void mouseExited(MouseEvent evt){
    }
    public void mouseDragged(MouseEvent evt){
    }
    public void mouseMoved(MouseEvent evt){
    }
  }
}

