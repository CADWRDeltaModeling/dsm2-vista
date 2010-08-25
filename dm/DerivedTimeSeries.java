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
import javax.swing.JPanel;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
/**
 * A time series representing a time series derived from math operations on other
 * time series.
 * <p>
 * The derived time series or DTS is defined by using an expression using relative
 * references to an associated MTS definition. This expression is parsed using JavaCC and
 * the resulting time series is then returned. 
 * 
 * Relative referencing in the expression is done by using the "$" sign followed
 * by the row number of the associated MTS definition. The expression can do all algebraic 
 * operations. It also is capable of understanding a limited number of functions
 * such as max,min,average,total or period (max|min|average|total), moving average
 * and statistical functions such as root mean square, mean, std. deviation etcetra.
 *
 * For a complete list @see vista.set.DTSExpression
 * 
 * @see DataReference, DTSExpression
 * @author Nicky Sandhu
 * @version $Id: DerivedTimeSeries.java,v 1.4 2000/03/21 18:16:20 nsandhu Exp $
 */
public class DerivedTimeSeries extends DataReference implements NamedLeaf{
  public static boolean DEBUG = false;
  private String _name;
  private String _expr;
  private MultipleTimeSeries _mts;
  private transient DataSet _dataSet;
  private boolean _modified;
  private String [] _parts;
  private Study _study;
  private DTSUI _ui;
  /**
   * create an empty derived time series
   */
  public DerivedTimeSeries(Study sty){
    setServername("local");
    setFilename("calculated");
    _name = "";
    _expr = "";
    setStudy(sty);
    _mts = new MultipleTimeSeries(sty);
    setPathname(new String[] {"","","","","",""});
  }
  /**
   * a copy constructor ( shallow copy )
   */
  public DerivedTimeSeries(DerivedTimeSeries dts){
    this._name = dts._name;
    this._expr = dts._expr;
    setStudy(dts._study);
    this.importMTS(dts._mts);
    Pathname path = dts.getPathname();
    String [] parts = new String[Pathname.MAX_PARTS];
    for(int i=0; i < parts.length; i++)
      parts[i] = path.getPart(i);
    setPathname(parts);
  }
  /**
    * creates a copy of this dts
    */
  public DataReference createClone(){
    return new DerivedTimeSeries(this);
  }
  /**
   * gets name of this data reference
   */
  public String getName(){
    return _name;
  }
  /**
   * sets name of this data reference. The responsibility of resolving naming conflicts
   * is that of the study.
   */
  public void setName(String name){
    if ( name == null ) 
      throw new RuntimeException("Attempt to set DTS: "+ _name +"'s name to null");
    name = name.toUpperCase().trim();
    _name = name;
    _modified = true;
  }
  /**
   * returns the user interface for this DTS
   */
  public JPanel getUI(){
    if ( _ui == null ) _ui = new DTSUI(this);
    return _ui;
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
    * get the associated mts;
    */
  public MultipleTimeSeries getMTS(){
    return _mts;
  }
  /**
    * imports the definition of this mts into this dts. This replaces the
    * existing definition
    */
  public void importMTS(MultipleTimeSeries mts){
    _mts = new MultipleTimeSeries(mts);
    _mts.setStudy(getStudy());
    _modified = true;
  }
  /**
    * gets the expression for this dts evaluation
    */
  public String getExpression(){
    return _expr;
  }
  /**
    * set the expression.
    */
  public void setExpression(String expr){
    _expr = expr;
    _modified = true;
  }
  /**
   * reloads data and recalculates its operations
   */
  public void reloadData(){
    _dataSet = null;
    _modified = _mts.isModified() | _modified;
  }
  /**
    * @return the result of the expression on the associated MTS's components
   */
  public DataSet getData(){
    if ( _dataSet != null ) return _dataSet;
    _dataSet = new DTSExpressionEvaluator(this).evaluate();
    _modified = false;
    return _dataSet;
  }
  /**
    * set the pathname parts
    */
  public void setPathname(String [] parts){
    _parts = parts;
    _modified = true;
  }
  /**
    * get the pathname. The pathname has empty A-F parts if no defaults are defined
    * else the parts are set to the specified path parts
    */
  public Pathname getPathname(){
    //    if ( _parts == null ) throw new RuntimeException("No pathname parts specified!");
    // get pathname 
    Pathname path = super.getPathname();
    // create pathname and set it.
    if ( path == null){
      path = Pathname.createPathname(new String [] {"","","","","",""} );
      super.setPathname(path);
    }
    if ( _parts == null ) return path;
    path.setPart(Pathname.A_PART,_parts[0]);
    path.setPart(Pathname.B_PART,_parts[1]);
    path.setPart(Pathname.C_PART,_parts[2]);
    if ( _parts[3].length() > 0 ) {
      try {
	TimeWindow tw = TimeFactory.getInstance().createTimeWindow(_parts[3]);
	super.setTimeWindow(tw);
      }catch(RuntimeException e){
	System.err.println("Incorrect time window specification: " + _parts[3]);
      }
    }
    //    path.setPart(Pathname.D_PART,_parts[3]);
    // path.setPart(Pathname.E_PART,_parts[4]);
    path.setPart(Pathname.F_PART,_parts[5]);
    return path;
  }
  /**
   * a prototype constructor. The created DTS has the same study as this one.
   */
  public NamedLeaf create(){
    return new DerivedTimeSeries(this.getStudy());
  }
  /**
   * get the name of this type
   */
  public String getTypeName(){
    return "Derived Time Series";
  }
  /**
   * a string represenation of this derived time series
   */
  public String toString(){
    return Portfolio.getLocalName(this);
  }
  /**
   * the xml tag id
   */
  public String getXmlTagName(){
    return "DTS";
  }
  /**
    * extracts from given element and replaces its state with the 
    * available state from the element
    */
  public void fromXml(Element de){
    if ( de == null ) return;
    setName(de.getAttribute("name"));
    setExpression(de.getAttribute("expr"));
    _mts = new MultipleTimeSeries(getStudy());
    _mts.fromXml((Element) de.getElementsByTagName(_mts.getXmlTagName()).item(0));
    Element pe = (Element) de.getElementsByTagName("path_parts").item(0);
    if ( pe != null ) {
      _parts = new String[6];
      for(int i=0; i < _parts.length; i++){
	_parts[i] = pe.getAttribute(Pathname.getPartName(i).substring(0,1));
      }
    } else {
      _parts = null;
    }
  }
  /**
    * writes out the representation of its state to the xmldocument and attaches
    * its element to the given element.
    */
  public void toXml(XmlDocument doc, Element ae){
    Element xe = doc.createElement(getXmlTagName());
    xe.setAttribute("name",getName());
    xe.setAttribute("expr",getExpression());
    _mts.toXml(doc,xe);
    Element pe = doc.createElement("path_parts");
    if ( _parts != null ){
      for(int i=0; i < _parts.length; i++){
	pe.setAttribute(Pathname.getPartName(i).substring(0,1),_parts[i] == null ? "": _parts[i]);
      }
    }
    xe.appendChild(pe);
    ae.appendChild(xe);
  }
  /**
   *
   */
  public boolean equals(Object obj){
    if ( obj instanceof DerivedTimeSeries ){
      DerivedTimeSeries dts = (DerivedTimeSeries) obj;
      return (dts.getName().equals(getName())) &&
	(dts.getStudy() == getStudy()) &&
	(dts.getMTS().equals(getMTS())) &&
	(dts.getExpression().equals(getExpression())) &&
	(dts.getPathname().equals(getPathname()));
    } else {
      return false;
    }
  }
  
}
