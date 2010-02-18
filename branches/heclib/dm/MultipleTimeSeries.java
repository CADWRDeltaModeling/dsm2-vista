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
import vista.set.*;
import vista.time.*;
import java.util.*;
import java.io.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.*;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
/**
 * A multiple time series is a collection of time series which can be either a 
 * derived time series or a pathname mapping. The data references to be queried for
 * the desired mapping are specified in the context of a study. In other words the 
 * MTS only defines the mappings. The actual data is obtained by delegating to the 
 * dss files that make up a study.
 *
 * An MTS has a unique name amongst MTS's by which it is uniquely defined. The MTSUI
 * is a editable name field with a table view along with editing actions.
 *
 * An MTS can be saved and retrieved from a text format ( XML ).
 * The default MTS view has each row defining a mapping to a time series data. The 
 * time series may be a derived time series (DTS) or a B and C part mapping. The default
 * is to ignore other parts of the pathname. 
 *
 * The parts are either specified or implied global. If a user wants to ignore a specific
 * part they may specify the path part as an empty string. The D part is a time window
 * that is either derived from a global specification for the project or can be specified
 * individually specified on each defining row. These properties may be specified at
 * the MTSRow or Study or Project context. This information is captured by
 * the first available non-empty data.
 *
 * @author Nicky Sandhu
 * @version $Id: MultipleTimeSeries.java,v 1.4 2000/03/21 18:16:22 nsandhu Exp $
 */
public class MultipleTimeSeries implements NamedLeaf{
  public static boolean DEBUG = false;
  public boolean [] _ignoreParts = new boolean[Pathname.MAX_PARTS];
  private boolean _modified = false;
  private MTSUI _ui;
  private Study _study;
  /**
    * an array of rows, each defining a time series
    */
  private Vector _rows;
  /**
    * the number of rows
    */
  private int _count;
  /**
    * the name for this grouping
    */
  private String _name;
  /**
   * create an empty multiple time series
   */
  public MultipleTimeSeries(Study sty){
    _name = "";
    _rows = new Vector();
    _count = 0;
    _modified = true;
    for(int i=0; i < _ignoreParts.length; i++)
      _ignoreParts[i] = false;
    setStudy(sty);
  }
  /**
   * creates a copy of the given MTS. A typical copy
   * constructor
   */
  public MultipleTimeSeries(MultipleTimeSeries mts){
    this._name = new String(mts.getName());
    this._rows = new Vector();
    for(Enumeration e = mts._rows.elements(); e.hasMoreElements(); ){
      _rows.addElement(new MTSRow((MTSRow)e.nextElement()));
    }
    this._count = mts._count;
    this.setStudy(mts.getStudy());
  }
  /**
    *
    */
  void setStudy(Study st){
    _study = st;
    _modified = true;
  }
  /**
    * get the study to which this dts belongs
    */
  public Study getStudy(){
    return _study;
  }
  /**
   * gets name of this data reference
   */
  public String getName(){
    return _name;
  }
  /**
   * sets name of this MTS. The project is responsible for setting the name 
   * for the MTS and to resolve name conflicts.
   */
  public void setName(String name){
    if ( name == null ) throw new RuntimeException("Attempt to set MTS: "+ _name +"'s name to null");
    name = name.toUpperCase().trim();
    _name = name;
    _modified = true;
  }
  /**
   * returns the user interface panel for this object
   */
  public JPanel getUI(){
    if ( _ui == null ) _ui = new MTSUI(this);
    return _ui;
  }
  /**
   * remove the i'th data reference.
   */
  public void remove(int i){
    _rows.removeElementAt(i);
    _modified = true;
  }
  /**
   * expands all the vectors to the given index
   */
  private void expandTo(int i){
    while( _rows.size()-1 < i ){
      _rows.addElement(MTSDefaults.createDefaultRow());
    }
  }
  /**
   * inserts an empty row at given index
   */
  public void insertAt(MTSRow row, int i){
    if ( i >=  _rows.size() ) return;
    if ( i < 0 ) return;
    _rows.insertElementAt(row,i);
    _modified = true;
  }
  /**
    * adds or appends a row defining a time series to this group
    */
  public void add(MTSRow row){
    _rows.addElement(row);
    _modified = true;
  }
  /**
   * checks if access is ok
   */
  private void checkAccessAt(int i){
    if ( i >= _rows.size() ) 
      throw new RuntimeException("Element accessed beyond bounds: " + i);
  }
  /**
    * get the row at given index
    */
  public MTSRow getRowAt(int i){
    checkAccessAt(i);
    return (MTSRow) _rows.elementAt(i);
  }
  /**
    * get the number of data references in this group of time series
    */
  public int getNumberOfRows(){
    return _rows.size();
  }
  /**
    * returns the number of time-series in this grouping
    */
  public int getCount(){
    return _rows.size();
  }
  /**
    *
    */
  public boolean isModified(){
    return _modified;
  }
  /**
   *
   */
  public String getTypeName(){
    return "Multiple Time Series";
  }
  /**
   *
   */
  public String getXmlTagName(){
    return "MTS";
  }
  /**
   * a identifier string for this MTS
   */
  public String toString(){
    return Portfolio.getLocalName(this);
  }
  /**
    * initializes from an xml tag tree.
    */
  public void fromXml(Element de){
    if ( de == null ) return;
    _name = de.getAttribute("name");
    TreeWalker tw = new TreeWalker(de);
    int rindex=0;
    while(true){
      Element re = tw.getNextElement("row");
      if (re == null) break;
      // create a row and initialize from the row element
      MTSRow row = new MTSRow();
      row.fromXml(re);
      add(row);
      rindex++;
    }
  }
  /**
    * builds an xml tag tree
    */
  public void toXml(XmlDocument doc, Element ae){
    Element de = doc.createElement(getXmlTagName());
    de.setAttribute("name",_name);
    int count = getNumberOfRows();
    for(int i=0; i < count; i++){
      getRowAt(i).toXml(doc,de);
    }
    if ( ae != null ) 
      ae.appendChild(de);
    else 
      doc.appendChild(de);
  }
  /**
   *
   */
  public NamedLeaf create(){
    return new MultipleTimeSeries(this.getStudy());
  }
  /**
   *
   */
  public boolean equals(Object obj){
    if ( obj instanceof MultipleTimeSeries ){
      MultipleTimeSeries mts = (MultipleTimeSeries) obj;
      boolean isSame =  (mts.getStudy()==getStudy()) &&
	(mts.getName().equals(getName())) &&
	(mts.getCount() == getCount());
	 for(int i=0; i < _ignoreParts.length; i++){
	   isSame = isSame && ( mts._ignoreParts[i] == _ignoreParts[i] );
	 }
	 if (!isSame) {
	   return isSame;
	 }else{
	   for(int i=0; i < getCount(); i++){
	     isSame = isSame && (mts.getRowAt(i).equals(getRowAt(i)));
	   }
	   return isSame;
	 }
    } else {
      return false;
    }
  }
}
