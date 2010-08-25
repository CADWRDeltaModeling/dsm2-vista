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
import javax.swing.tree.*;
import java.util.*;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import java.io.*;
/**
  * A portfolio contains name objects that it keeps track of. A portfolio allows
  * the addition of multiple folders in a heirarchy with each level allowed to 
  * to contain leaf objects which contain NamedLeaf object. 
  * <p>
  * A portfolio also maintains the uniqueness of the named leaves. While adding
  * the names are checked for uniqueness. Once added can only be changed via the 
  * portfolio's UI where they are again checked for uniqueness.
  * <p>
  * The NamedLeaf objects can be added to folders or removed from them. The name 
  * of the named objects must be unique within this portfolio.
  *
  * @author Nicky Sandhu
  * @version $Id: Portfolio.java,v 1.4 2000/03/21 18:16:23 nsandhu Exp $
  */
public class Portfolio extends DefaultTreeModel{
  /**
   *
   */
  public static final String SEPARATOR = "/";
  /**
   *
   */
  public static boolean DEBUG = false;
  public static boolean OVERRIDE_DEFINES = false;
  private Hashtable _leafList;
  private boolean _needsSaving = true;
  private PortfolioUI _ui;
  private NamedLeaf _proto;
  /**
   * starts a portfolio with the root folder
   */
  public Portfolio(NamedLeaf proto){
    super(new DefaultMutableTreeNode(proto.getXmlTagName()),true);
    _leafList = new Hashtable();
    _proto = proto;
  }
  /**
   * gets the user interface for this class.
   */
  public PortfolioUI getUI(){
    if ( _ui == null ) new PortfolioUI(this);
    return _ui;
  }
  /**
   * gets named leaf with given name or null if none found.
   * Name is first converted to uppercase.
   */
  public NamedLeaf getNamed(String name){
    name = name.toUpperCase();
    return (NamedLeaf) _leafList.get(name);
  }
  /**
   * @return the number of named
   */
  public int getNumberOfNamed(){
    return _leafList.size();
  }
  /**
   * @return  the list of named or null if none present
   */
  public NamedLeaf [] getList(){
    if ( _leafList.size() == 0 ) return null;
    NamedLeaf [] array = new NamedLeaf[_leafList.size()];
    int count = 0;
    for(Enumeration e = _leafList.elements(); e.hasMoreElements();){
      array[count] = (NamedLeaf) e.nextElement();
      count ++;
    }
    return array;
  }
  /**
   * @return the names of all the named items in this portfolio
   */
  public String [] getNames(){
    if ( _leafList.size() == 0 ) return null;
    String [] array = new String[_leafList.size()];
    int count = 0;
    for(Enumeration e = _leafList.elements(); e.hasMoreElements();){
      array[count] = ((NamedLeaf) e.nextElement()).getName();
      count ++;
    }
    return array;
  }
  /**
   * adds a named leaf and creates folders in the process of doing so.
   */
  public void addNamedLeaf(NamedLeaf leaf){
    String folderPath = Portfolio.getFolderName(leaf);
    DefaultMutableTreeNode parentNode = addPath(folderPath);
    addInFolder(leaf, parentNode, 0);
  }
  /**
   * saves this portfolio to an xml file with "<DM>" element as its root tag
   */
  public void save(String file) throws IOException{
    XmlDocument doc = new XmlDocument();
    doc.appendChild(doc.createElement("DM"));
    this.toXml(doc,doc.getDocumentElement());
    FileWriter writer = new FileWriter(file);
    doc.write(writer);
    writer.close();
    _needsSaving = false;
  }
  /**
   * loads from given file
   */
  public void load(String file) throws IOException {
    FileInputStream inps = new FileInputStream(file);
    XmlDocument doc = null;
    try {
      doc = XmlDocument.createXmlDocument(inps,false);
    } catch( SAXException exc ){
      throw new IOException( "Nested Exception is " + exc);
    } finally {
      inps.close();
    }
    TreeWalker walker = new TreeWalker(doc.getDocumentElement());
    Element el = walker.getNextElement("portfolio");
    this.fromXml(el);
    _needsSaving = true;
  }
  /**
   *
   */
  public boolean equals(Object obj){
    if( !(obj instanceof Portfolio) ) return false;
    Portfolio po = (Portfolio) obj;
    boolean isSame = po._proto.getClass().equals(_proto.getClass()) &&
      po.getNumberOfNamed() == getNumberOfNamed();
    if (!isSame) return isSame;
    String [] ponames = po.getNames();
    if ( ponames != null ){
      for(int i=0; i < ponames.length; i++){
	isSame = isSame && (getNamed(ponames[i]) != null);
	if (!isSame) break;
      }
    }else{
      return (getNames() == null);
    }
    return isSame;
  }
  /**
   * initializes this study from the given xml element
   */
  public void fromXml(Element xe){
    TreeWalker walker = new TreeWalker(xe);
    Element el = walker.getNextElement(_proto.getXmlTagName());
    while( el != null ){
      try {
	NamedLeaf leaf = _proto.create();
	leaf.fromXml(el);
	addNamedLeaf(leaf);
      }catch(RuntimeException re){
	System.out.println("Exception while reading XML: " + re);
      }
      el = walker.getNextElement(_proto.getXmlTagName());
    }
  }
  /**
   * build this study into the xml tree anchored at root and 
   * build from doc
   */
  public void toXml(XmlDocument doc, Element root){
    Element xe = doc.createElement("portfolio");
    //
    NamedLeaf [] leaves = getList();
    if ( leaves != null ) {
      for(int i=0; i < leaves.length; i++){
	leaves[i].toXml(doc,xe);
      }
    }
    //
    root.appendChild(xe);
  }
  /**
   * returns the name of this folder separated by Portfolio.SEPARATOR.
   */
  public static String getFolderName(DefaultMutableTreeNode node){
    TreeNode parentNode = node.getParent();
    String folderName = null;
    while( parentNode != null ){
      if ( folderName == null )
	folderName = parentNode.toString();
      else
	folderName = parentNode.toString()+Portfolio.SEPARATOR+folderName;
      parentNode = parentNode.getParent();
    }
    if ( folderName == null ) 
      return Portfolio.SEPARATOR + node.toString();
    else 
      return Portfolio.SEPARATOR + folderName + Portfolio.SEPARATOR + node.toString();
  }
  /**
   * this method returns the folder parent of this named leaf.
   * The assumption here is that the name of the named leaf consists
   * of Portfolio.SEPARATOR delimited names in which the last name is the local name and
   * all names above the last delimited name is the parent folder name.
   */
  public static String getFolderName(NamedLeaf leaf){
    String fullName = leaf.getName();
    int lastIndex = fullName.lastIndexOf('/');
    if ( lastIndex == -1 ) 
      return "";
    else 
      return fullName.substring(0,lastIndex);
  }
  /**
   * this method returns the last name for this leaf.
   */
  public static String getLocalName(NamedLeaf leaf){
    String fullName = leaf.getName();
    int lastIndex = fullName.lastIndexOf('/');
    if ( lastIndex == -1 ) 
      return fullName;
    else 
      return fullName.substring(lastIndex+1,fullName.length());
  }
  /**
   * sets the local name of a given to new name
   */
  public static void setLocalName(NamedLeaf leaf, String name){
    if ( name == null ) throw new RuntimeException("Attempt to set name to null");
    leaf.setName( getFolderName(leaf)+Portfolio.SEPARATOR+name);
  }
  /**
   * sets folder name to new name
   */
  public static void setFolderName(NamedLeaf leaf, String name){
    if ( name == null ) throw new RuntimeException("Attempt to set name to null");
    leaf.setName( name + Portfolio.SEPARATOR + getLocalName(leaf));
  }
  //!
  //! Following methods for TreeModel stuff
  //!

  /**
   * traps changes to user object changes. If leaf it sets the name of the
   * leaf to object's value else it sets the string representation to the user
   * object of the folder.
   */
  public void valueForPathChanged(TreePath path, Object newValue) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    if ( node.getAllowsChildren() ){
      node.setUserObject(newValue.toString().toUpperCase());
    } else {
      NamedLeaf leaf = (NamedLeaf) node.getUserObject();
      updateName(leaf.getName(), Portfolio.getFolderName(leaf)+Portfolio.SEPARATOR+newValue.toString());
    }
    // if name is changed rearrange alphabetically...
    DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node.getParent();
    node.removeFromParent();
    int index = Portfolio.getAlphaBeticalIndex(node,pnode);
    pnode.insert(node,index);
    // send the event off..
    nodeChanged(node);
    nodeStructureChanged(pnode);
    _needsSaving = true;
  }
  /**
   * updates a leaf with old name to new name, throwing exceptions along the way
   */
  public void updateName(String oldName, String newName){
    // convert to upper case...
    oldName = oldName.toUpperCase();
    newName = newName.toUpperCase();
    if (DEBUG) System.out.println("Updating name " + oldName + " to " + newName);
    // case1. old name = new name
    if ( oldName.equals(newName) ) return;
    // case2. old name exists and named is in portfolio
    // case2a. but new name exists as well
    // case2b. but new name does not exist
    if ( _leafList.containsKey(oldName) ){
      if ( _leafList.containsKey(newName) ){
	throw new RuntimeException("Name: " + newName + " is already taken in portfolio");
      }
      NamedLeaf leaf = (NamedLeaf) _leafList.get(oldName);
      _leafList.remove(oldName);
      leaf.setName(newName);
      _leafList.put(newName, leaf);
      _needsSaving = true;
    } else {
      // case3. old name does not exist or named is not in portfolio! 
      throw new RuntimeException("No such named " + oldName + " in portfolio");
    }
  }
  /**
   * adds folder to the container at specified row index
   */
  public void addFolder(String folder, MutableTreeNode parent, int index){
    folder = folder.toUpperCase();
    DefaultMutableTreeNode fn = new DefaultMutableTreeNode(folder);
    fn.setAllowsChildren(true);
    insertNodeInto(fn,parent,index);
    _needsSaving = true;
  }
  /**
   * creates leaf of given name and adds it to the parent at given index.
   * It throws an exception if name is already present.
   */
  public void addInFolder(String leafName, MutableTreeNode parent, int index){
    NamedLeaf leaf = _proto.create();
    leaf.setName(leafName.toUpperCase());
    addInFolder(leaf, parent, index);
  }
  /**
   * creates leaf of given name and adds it to the parent at given index.
   * It throws an exception if name is already present.
   */
  public void addInFolder(NamedLeaf leaf, MutableTreeNode parent, int index){
    if ( OVERRIDE_DEFINES || ! _leafList.containsKey(leaf.getName())){
      _leafList.put(leaf.getName(), leaf);
      DefaultMutableTreeNode leafn = new DefaultMutableTreeNode();
      leafn.setAllowsChildren(false);
      leafn.setUserObject(leaf);
      insertNodeInto(leafn, parent, index);
      _needsSaving = true;
    } else {
      throw new RuntimeException("Portfolio already has " + leaf);
    }
  }
  /**
   * Invoked this to insert newChild at location index in parents children.
   * This will then message nodesWereInserted to create the appropriate
   * event. This is the preferred way to add children as it will create
   * the appropriate event.
   */
  public void insertNodeInto(MutableTreeNode newChild,
			     MutableTreeNode parent, int index){
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) newChild;
    DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) parent;
    if ( ! node.getAllowsChildren() ){
      NamedLeaf leaf = (NamedLeaf) node.getUserObject();
      updateName(leaf.getName(), 
		 Portfolio.getFolderName(pnode)+
		 Portfolio.SEPARATOR+ Portfolio.getLocalName(leaf));
    } else {
      int count = pnode.getChildCount();
      for(int i=0; i < count; i++){
	if ( pnode.getChildAt(i).toString().equals(newChild.toString()) ){
	  throw new RuntimeException("Folder " + newChild + " already exists");
	}
      }
    }
    //
    index = getAlphaBeticalIndex(node,pnode);
    //
    super.insertNodeInto(newChild, parent, index);
    _needsSaving = true;
  }
  /**
   * place folders alphabetically first then all the leaves in alphabetical order
   */
  public static int getAlphaBeticalIndex(DefaultMutableTreeNode cnode, DefaultMutableTreeNode pnode){
    String cname = cnode.toString();
    String pname = pnode.toString();
    int count = pnode.getChildCount();
    int index = 0;
    for(index=0; index < count; index++){
      DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode) pnode.getChildAt(index);
      if ( cnode.getAllowsChildren() ){ // only compare with folders else exit
	if ( ! currentChild.getAllowsChildren() ) break;
      } else {
	if ( currentChild.getAllowsChildren() ) continue;
      }
      if( cname.compareTo(pnode.getChildAt(index).toString()) <= 0 ) break;
    }
    return index;
  }
  /**
   * removes node from tree. If node is root it is not deleted. If it is 
   * a folder containing children it throws an exception
   */
  public void removeNodeFromParent(MutableTreeNode node){
    if ( node == getRoot() ) return; // we don't want them deleting the root folder
    if ( node.getAllowsChildren() ){
      if ( node.getChildCount() != 0 ) {
	throw new RuntimeException("Attempt to delete non-empty folder");
      }
      super.removeNodeFromParent(node);
      _needsSaving = true;
    } else {
      NamedLeaf named = (NamedLeaf) ((DefaultMutableTreeNode)node).getUserObject();
      if ( _leafList.contains(named) ){
	_leafList.remove(named.getName());
	super.removeNodeFromParent(node);
	_needsSaving = true;
      } else {
	throw new RuntimeException("Attempt to delete non-existent item named " + named.getName());
      }
    }
  }
  /**
   *
   */
  private DefaultMutableTreeNode addPath(String folderPath){
    StringTokenizer stk = new StringTokenizer(folderPath,Portfolio.SEPARATOR);
    DefaultMutableTreeNode pnode = null;
    while(stk.hasMoreTokens()){
      String folder = stk.nextToken().trim().toUpperCase();
      if ( pnode == null ) {
	if ( ! folder.equals(getRoot().toString()) ) 
	  throw new RuntimeException("Mismatched type of root, expected " + 
				     getRoot().toString() + ", got " + folder + " for " + folderPath);
	pnode = (DefaultMutableTreeNode) getRoot();
	continue;
      }
      int count = pnode.getChildCount();
      boolean foundMatch = false;
      for(int i=0; i < count; i++){
	DefaultMutableTreeNode cnode = (DefaultMutableTreeNode) pnode.getChildAt(i);
	if ( cnode.getAllowsChildren() && cnode.toString().equals(folder) ) {
	  pnode = cnode;
	  foundMatch = true;
	  break;
	}
      }
      // if such a folder is not found then create one..
      if ( ! foundMatch ){
	DefaultMutableTreeNode fn = new DefaultMutableTreeNode(folder);
	fn.setAllowsChildren(true);
	insertNodeInto(fn,pnode,0);
	pnode = fn;
      } else {
	// parent node has already been assigned
	continue;
      }
    }
    // return the last folder thus created or found...
    return pnode;
  }
}
