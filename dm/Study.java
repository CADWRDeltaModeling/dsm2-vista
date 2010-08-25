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
import vista.set.DataReference;
import vista.set.Pathname;
import vista.set.Group;
import vista.db.dss.DSSUtil;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
/**
  * A study consists of dss files and associated set of data definitions. These
  * definitions are available in terms of MTS's and DTS's.
  * <p>
  * A study is uniquely identified by its name. The uniqueness property is the
  * responsibility of the project contaiing thes studies. Further more the
  * study name is not case sensitive
  * <p>
  * A study can contain an arbitrary number of DTS and MTS. It is responsible 
  * for managing name clashes between DTS and MTS. The name for any DTS/MTS 
  * must be unique in the study between all MTS and DTS. This name is again
  * case-insensitive.
  * <p>
  * An arbitray number of dss files may be attached to a study. These files are
  * then queried in the order that they are listed for retrieving a matching
  * reference. In other words this list of file is ordered.
  * <p>
  * When a file is initially added it is not opened till the first matching reference
  * is looked for. 
  * <p>
  * If the flag for MULTIPLE_MATCH_ERROR is set for all studies then
  * further matches are searched for and if another match is found an exception
  * is thrown. If no matches are found an exception is thrown.
  * <p>
  * Note: Overall memory saving strategy is to always get the data set for the given
  * time window if the current data set
  * @author Nicky Sandhu
  * @version $Id: Study.java,v 1.4 2000/03/21 18:16:23 nsandhu Exp $
  */
public class Study{
  /**
   * true if multiple matches throws exception
   */
  public boolean MULTIPLE_MATCH_ERROR = false;
  /**
   * the dss files and their associated groups.
   */
  private Vector _dssFiles, _dssGroups;
  /**
   * if true then study must be saved to a file to preserve it
   */
  private boolean _modified;
  /**
   * if true then the groups must be reread.
   */
  private boolean _recacheGroups = true;
  /**
   * name of study  and name of file to which study is saved.
   */
  private String _name, _filename;
  /**
   * the mts and dts portfolios of this study.
   */
  private Portfolio _mtspt, _dtspt;
  /**
   *
   */
  private String [] _defaultParts = new String[Pathname.MAX_PARTS];
  /**
   * creates an empty study with the given name. If name is null an exception
   * is thrown
   */
  public Study(String name){
    setName(name);
    _dssFiles = new Vector();
    _modified = true;
    _defaultParts = new String[Pathname.MAX_PARTS];
    for(int i=0; i < Pathname.MAX_PARTS; i++) _defaultParts[i] = "";
    _mtspt = new Portfolio(new MultipleTimeSeries(this));
    _dtspt = new Portfolio(new DerivedTimeSeries(this));
  }
  /** 
   * returns the name of this study
   */
  public String getName(){
    return _name;
  }
  /**
   * sets the name of this study. If name is null an exception is thrown.
   */
  public void setName(String name){
    if ( name == null ) throw new RuntimeException("Attempt to set Study: "+ _name +"'s name to null");
    name = name.toUpperCase().trim();
    _name = name;
    _modified = true;
  }
  /**
   * get filename to which this study is saved or comes from.
   */
  public String getFilename(){
    return _filename == null ? "" : _filename;
  }
  /**
   * set filename to which this study is saved or comes from.
   */
  public void setFilename(String filename){
    _filename = filename;
    _modified = true;
  }
  /**
   * true if study is modified since last save
   */
  public boolean isModified(){
    return _modified;
  }
  /**
   * adds a dss file to the list of files in this study
   */
  public void addDSSFile(String file){
    _dssFiles.addElement(file);
    _recacheGroups = true;
    _modified = true;
  }
  /**
   * removes a dss file from this study
   */
  public void removeDSSFile(String file){
    _dssFiles.removeElement(file);
    _recacheGroups = true;
    _modified = true;
  }
  /**
   * removes a dss file from this study
   */
  public void removeDSSFile(int index){
    _dssFiles.removeElementAt(index);
    _recacheGroups = true;
    _modified = true;
  }
  /**
   *
   */
  public String getDSSFile(int i){
    return (String) _dssFiles.elementAt(i);
  }
  /**
   *
   */
  public void setDSSFile(String file, int i){
    _dssFiles.setElementAt(file,i);
    _recacheGroups = true;
    _modified = true;
  }
  /**
   * inserts a dss file into this study.
   */
  public void insertDSSFile(String file, int index){
    _dssFiles.insertElementAt(file,index);
    _recacheGroups = true;
    _modified = true;
  }
  /**
   * gets an array of the dss files in this study
   */
  public String [] getDSSFiles(){
    if ( _dssFiles.size() == 0 ) return null;
    String [] files = new String[_dssFiles.size()];
    _dssFiles.copyInto(files);
    return files;
  }
  /**
   *
   */
  public int getNumberOfDSSFiles(){
    return _dssFiles.size();
  }
  /**
   * removes al dss files from this study
   */
  public void removeAllDSSFiles(){
    _dssFiles.removeAllElements();
    _recacheGroups = true;
    _modified = true;
  }
  /**
   * get matching reference for the pathname parts. This match is an exact
   * case insensitive match
   */
  public DataReference getMatchingReference(String [] pathParts){
    // correct the path parts by adding ^ & $
    if ( pathParts == null ) throw new RuntimeException("Null path parts given for match!");
    if ( pathParts.length != 6) throw new RuntimeException("No. of path parts != 6");
    for(int i=0; i < pathParts.length; i++){
      if ( pathParts[i].trim().length() == 0 ) continue;
      pathParts[i] = "^"+pathParts[i].trim()+"$";
    }
    // recache the groups if out-of-sync
    DataReference match = null;
    boolean found = false;
    if ( _recacheGroups ){
      _dssGroups = new Vector();
      for(Enumeration e = _dssFiles.elements(); e.hasMoreElements(); ){
	_dssGroups.addElement(DSSUtil.createGroup("local",(String) e.nextElement()));
      }
      _recacheGroups = false;
    }
    // search thro' the files in the order of their appearance in the study for the first
    // match.
    for(Enumeration e = _dssGroups.elements(); e.hasMoreElements(); ){
      Group g = (Group) e.nextElement();
      // find all matching references.
      DataReference [] refs = g.find(pathParts);
      if ( refs != null ) {
	// if multiple matches maybe throw exeption ...
	if ( refs.length > 1 && MULTIPLE_MATCH_ERROR ) {
	  throw Study.MultipleMatchException(pathParts,true,e.toString());
	}
	// if found in a previous file maybe throw exception
	if ( found && MULTIPLE_MATCH_ERROR ) {
	  throw Study.MultipleMatchException(pathParts,false,e.toString());
	}
	//keep only the first match
	if ( !found) { 
	  match = refs[0];
	  found = true;
	}
      }
      // ?keep going or quit on first match?
      // if ( found && !MULTIPLE_MATCH_ERROR ) break; 
    }
    // return the first matched
    return match;
  }
  /**
   * throw exception if multiple matches are a problem
   */
  private static RuntimeException MultipleMatchException(String [] pathParts, 
							 boolean inSameFile,
							 String file){
    String msg = "Multiple matches ";
    if ( inSameFile ) msg += "in same file: " + file + " ";
    for(int i=0; i < pathParts.length; i++){
      msg += pathParts[i];
    }
    return new RuntimeException(msg);
  }
  /**
   * get matching reference for the given dts name
   */
  public DataReference getMatchingReference(String dtsName){
    return (DerivedTimeSeries) _dtspt.getNamed(dtsName);
  }
  /**
   * get the default path parts for this study.
   */
  public String [] getDefaultPathParts(){
    return _defaultParts;
  }
  /**
   * copies the parts contents to default parts array.
   */
  public void setDefaultRow(String [] parts){
    for(int i=0; i < _defaultParts.length; i++){
      _defaultParts[i] = parts[i];
    }
  }
  /**
   * set the dts portfolio
   * ?make this package private?
   */
  public void setDTSPortfolio(Portfolio dpt){
    _dtspt = dpt;
  }
  /**
   * set the mts portfolio
   * ?make this package private?
   */
  public void setMTSPortfolio(Portfolio mpt){
    _mtspt = mpt;
  }
  /**
   * get the DTS portfolio
   */
  public Portfolio getDTSPortfolio(){
    return _dtspt;
  }
  /**
   * get the MTS portfolio
   */
  public Portfolio getMTSPortfolio(){
    return _mtspt;
  }
  /**
   *
   */
  public void save() throws IOException{
    save(_filename);
  }
  /**
   *
   */
  public void save(String studyFile) throws IOException{
    XmlDocument xdoc = new XmlDocument();
    xdoc.appendChild(xdoc.createElement("root"));
    this.toXml(xdoc, xdoc.getDocumentElement());
    if ( _filename == null || !_filename.equals(studyFile) ) _filename = studyFile;
    FileWriter writer = new FileWriter(_filename);
    xdoc.write(writer);
    writer.close();
    _modified=false;
  }
  /**
   *
   */
  public void load(String studyFile) throws IOException, FileNotFoundException{
    FileInputStream fis = new FileInputStream(studyFile);
    if ( _filename == null ) _filename = studyFile;
    XmlDocument xdoc;
    try {
      xdoc = XmlDocument.createXmlDocument(fis,false);
    } catch(SAXException se){
      throw new IOException("Nested exception: " + se.getMessage());
    }
    TreeWalker tw = new TreeWalker(xdoc.getDocumentElement());
    // study element 
    Element el = tw.getNextElement("study");
    if (el==null) throw new RuntimeException("No study tag in file: " + studyFile);
    //
    this.fromXml(el);
    _modified = true;
  }
  /**
   * initializes this study from the given xml element
   */
  public void fromXml(Element xe){
    setName(xe.getAttribute("name"));
    // add dss files in the order listed
    TreeWalker tw = new TreeWalker(xe);
    while(true){
      Element el = tw.getNextElement("dssfile");
      if (el==null) break;
      addDSSFile(el.getAttribute("name"));
    }
    // add mts to study portfolio
    tw.reset();
    Element pl = tw.getNextElement("portfolio");
    if ( pl == null ) return;
    _mtspt.fromXml(pl);
    // add dts to study portfolio
    pl = tw.getNextElement("portfolio");
    if ( pl == null ) return;
    _dtspt.fromXml(pl);
  }
  /**
    * build this study into the xml tree anchored at root and 
    * build from doc
    */
  public void toXml(XmlDocument doc, Element root){
    Element xe = doc.createElement("study");
    // set name attribute
    xe.setAttribute("name",getName());
    // add dss files in study
    String [] files = getDSSFiles();
    if ( files != null ) {
      for(int i=0; i < files.length; i++){
	Element fe = doc.createElement("dssfile");
	fe.setAttribute("name",files[i]);
	xe.appendChild(fe);
      }
    }
    // add mts defs in study
    if ( _mtspt != null) _mtspt.toXml(doc,xe);
    // add dts defs in study
    if ( _dtspt != null ) _dtspt.toXml(doc,xe);
    //
    root.appendChild(xe);
  }
}
